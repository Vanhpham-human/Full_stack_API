package com.annam.shop.service;

import com.annam.shop.domain.Cart;
import com.annam.shop.domain.CartItem;
import com.annam.shop.domain.Customer;
import com.annam.shop.domain.OrderStatus;
import com.annam.shop.domain.Product;
import com.annam.shop.domain.Role;
import com.annam.shop.domain.ShopOrder;
import com.annam.shop.domain.ShopOrderItem;
import com.annam.shop.exception.ApiException;
import com.annam.shop.exception.ErrorCode;
import com.annam.shop.exception.FieldIssue;
import com.annam.shop.exception.InputRules;
import com.annam.shop.repo.CartRepository;
import com.annam.shop.repo.CustomerRepository;
import com.annam.shop.repo.ProductRepository;
import com.annam.shop.repo.ShopOrderRepository;
import com.annam.shop.security.AuthUser;
import com.annam.shop.service.view.Views.OrderItemView;
import com.annam.shop.service.view.Views.OrderView;
import com.annam.shop.service.view.Views.PageView;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        TRANSITIONS.put(OrderStatus.PENDING, EnumSet.of(OrderStatus.PAID, OrderStatus.CANCELLED));
        TRANSITIONS.put(OrderStatus.PAID, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        TRANSITIONS.put(OrderStatus.SHIPPED, EnumSet.noneOf(OrderStatus.class));
        TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    private final ShopOrderRepository orders;
    private final CustomerRepository customers;
    private final CartRepository carts;
    private final ProductRepository products;

    /**
     * Đặt hàng trong một giao dịch ghi: khóa sản phẩm, kiểm tra tồn kho, tính tiền,
     * tạo đơn PENDING rồi mới trừ kho. Thiếu hàng thì không sửa số lượng nào.
     */
    @Transactional
    public OrderView checkout(Long customerId, String shippingAddress, String note) {
        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new ApiException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy khách hàng"));
        Cart cart = carts.findDetailedByCustomerId(customerId)
                .orElseThrow(() -> new ApiException(ErrorCode.ERR_CART_EMPTY));
        if (cart.getItems().isEmpty()) {
            throw new ApiException(ErrorCode.ERR_CART_EMPTY);
        }
        String address = InputRules.optionalText(shippingAddress, "shippingAddress", 500);
        if (address == null) {
            address = customer.getAddress();
        }
        if (address == null || address.isBlank()) {
            throw new ApiException(ErrorCode.ERR_VALIDATION, ErrorCode.ERR_VALIDATION.defaultMessage(),
                    List.of(new FieldIssue("shippingAddress", "Cần địa chỉ giao hàng")));
        }
        String cleanNote = InputRules.optionalText(note, "note", 500);

        List<CartItem> lines = cart.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                .toList();
        List<ReservedLine> reserved = new ArrayList<>();
        List<FieldIssue> stockIssues = new ArrayList<>();
        for (CartItem line : lines) {
            Long productId = line.getProduct().getId();
            Product product = products.findByIdForUpdate(productId)
                    .orElseThrow(() -> new ApiException(ErrorCode.ERR_NOT_FOUND, "Sản phẩm trong giỏ không còn tồn tại"));
            if (product.getStockQuantity() < line.getQuantity()) {
                stockIssues.add(new FieldIssue("productId", shortageMessage(product, line.getQuantity())));
            } else {
                reserved.add(new ReservedLine(product, line.getQuantity()));
            }
        }
        if (!stockIssues.isEmpty()) {
            throw new ApiException(ErrorCode.ERR_STOCK_EMPTY, ErrorCode.ERR_STOCK_EMPTY.defaultMessage(), stockIssues);
        }

        Instant now = Instant.now();
        ShopOrder order = new ShopOrder();
        order.setCode(OrderCodes.next());
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(address);
        order.setNote(cleanNote);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        long total = 0;
        for (ReservedLine line : reserved) {
            Product product = line.product();
            ShopOrderItem item = new ShopOrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(line.quantity());
            order.getItems().add(item);
            total += product.getPrice() * line.quantity();
            product.setStockQuantity(product.getStockQuantity() - line.quantity());
        }
        order.setTotalAmount(total);
        cart.getItems().clear();
        ShopOrder saved = orders.save(order);
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public OrderView get(Long orderId, AuthUser actor) {
        ShopOrder order = orders.findDetailedById(orderId)
                .orElseThrow(() -> new ApiException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy đơn hàng"));
        assertCanView(order, actor);
        return toView(order);
    }

    @Transactional(readOnly = true)
    public PageView<OrderView> listForCustomer(Long customerId, Integer page, Integer size) {
        PageQuery query = PageQuery.of(page, size, 10);
        Page<ShopOrder> result = orders.findByCustomer_Id(
                customerId,
                PageRequest.of(query.page(), query.size(), Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));
        return toPage(result);
    }

    @Transactional(readOnly = true)
    public PageView<OrderView> listAll(OrderStatus status, Integer page, Integer size) {
        PageQuery query = PageQuery.of(page, size, 20);
        Page<ShopOrder> result = orders.search(
                status,
                PageRequest.of(query.page(), query.size(), Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));
        return toPage(result);
    }

    /**
     * Đổi trạng thái. Hủy đơn hoàn tồn kho trong cùng giao dịch với việc ghi trạng thái.
     */
    @Transactional
    public OrderView updateStatus(Long orderId, OrderStatus next, AuthUser actor) {
        ShopOrder order = orders.findDetailedById(orderId)
                .orElseThrow(() -> new ApiException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy đơn hàng"));
        if (actor.role() != Role.ADMIN) {
            boolean ownCancel = actor.role() == Role.CUSTOMER
                    && order.getCustomer().getId().equals(actor.id())
                    && next == OrderStatus.CANCELLED;
            if (!ownCancel) {
                throw new ApiException(ErrorCode.ERR_FORBIDDEN);
            }
        }
        if (!TRANSITIONS.get(order.getStatus()).contains(next)) {
            throw new ApiException(
                    ErrorCode.ERR_INVALID_STATUS,
                    "Không thể chuyển đơn từ " + order.getStatus() + " sang " + next,
                    List.of(new FieldIssue("status", "Luồng hợp lệ: PENDING → PAID → SHIPPED, hoặc hủy khi PENDING/PAID")));
        }
        if (next == OrderStatus.CANCELLED) {
            restoreStock(order);
        }
        order.setStatus(next);
        order.setUpdatedAt(Instant.now());
        return toView(order);
    }

    private void restoreStock(ShopOrder order) {
        List<ShopOrderItem> items = order.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                .toList();
        for (ShopOrderItem item : items) {
            Product product = products.findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new ApiException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy sản phẩm để hoàn kho"));
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        }
    }

    private PageView<OrderView> toPage(Page<ShopOrder> result) {
        List<OrderView> content = result.getContent().stream()
                .map(order -> orders.findDetailedById(order.getId()).orElseThrow())
                .map(OrderService::toView)
                .toList();
        return new PageView<>(content, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    private static void assertCanView(ShopOrder order, AuthUser actor) {
        if (actor.role() == Role.ADMIN) {
            return;
        }
        if (!order.getCustomer().getId().equals(actor.id())) {
            throw new ApiException(ErrorCode.ERR_FORBIDDEN);
        }
    }

    private static String shortageMessage(Product product, int requested) {
        int stock = product.getStockQuantity();
        if (stock <= 0) {
            return product.getName() + " đã hết hàng";
        }
        return product.getName() + " chỉ còn " + stock + " trong kho, bạn chọn " + requested;
    }

    static OrderView toView(ShopOrder order) {
        List<OrderItemView> items = order.getItems().stream()
                .map(item -> new OrderItemView(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getUnitPrice() * item.getQuantity()))
                .toList();
        return new OrderView(
                order.getId(),
                order.getCode(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getNote(),
                order.getCustomer().getId(),
                order.getCustomer().getFullName(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items);
    }

    private record ReservedLine(Product product, int quantity) {
    }
}

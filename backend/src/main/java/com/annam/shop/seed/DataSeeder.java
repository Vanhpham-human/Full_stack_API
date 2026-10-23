package com.annam.shop.seed;

import com.annam.shop.domain.Cart;
import com.annam.shop.domain.Customer;
import com.annam.shop.domain.Role;
import com.annam.shop.repo.CartRepository;
import com.annam.shop.repo.CategoryRepository;
import com.annam.shop.repo.CustomerRepository;
import com.annam.shop.repo.ProductRepository;
import com.annam.shop.repo.ShopOrderRepository;
import com.annam.shop.service.CartService;
import com.annam.shop.service.CategoryService;
import com.annam.shop.service.CustomerService;
import com.annam.shop.service.OrderService;
import com.annam.shop.service.ProductService;
import com.annam.shop.service.command.CategoryCommand;
import com.annam.shop.service.command.ProductCommand;
import com.annam.shop.service.view.Views.CustomerView;
import com.annam.shop.service.view.Views.ProductView;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import net.datafaker.service.RandomService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository customers;
    private final CartRepository carts;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ShopOrderRepository orders;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Faker faker = new Faker(Locale.forLanguageTag("vi"), new RandomService(new Random(20261023)));
        CustomerView admin = ensureUser(
                "admin@annam.vn",
                "0900000001",
                "Quản trị AnNam",
                "Admin@123",
                Role.ADMIN,
                "1 Tràng Tiền, Hoàn Kiếm, Hà Nội");
        CustomerView demo = ensureUser(
                "demo@annam.vn",
                "0900000002",
                "Khách demo",
                "Customer@123",
                Role.CUSTOMER,
                "12 Hàng Bông, Hoàn Kiếm, Hà Nội");

        if (productRepository.count() == 0) {
            Map<String, Long> categoryIds = new HashMap<>();
            for (SampleCatalog.CategorySeed seed : SampleCatalog.categories()) {
                var created = categoryService.create(new CategoryCommand(seed.name(), seed.description()));
                categoryIds.put(seed.name(), created.id());
            }
            int index = 0;
            ProductView firstInStock = null;
            for (SampleCatalog.ProductSeed seed : SampleCatalog.products()) {
                int stock = index % 10 == 9 ? 0 : faker.number().numberBetween(8, 36);
                String warranty = " Bảo hành " + faker.number().numberBetween(6, 24) + " tháng.";
                ProductView created = productService.create(new ProductCommand(
                        seed.name(),
                        seed.description() + warranty,
                        seed.price(),
                        "https://picsum.photos/seed/annam-" + (index + 1) + "/800/640",
                        stock,
                        categoryIds.get(seed.category())));
                if (firstInStock == null && stock > 0) {
                    firstInStock = created;
                }
                index++;
            }
            for (int i = 1; i <= 8; i++) {
                String phone = String.format("091%07d", i);
                String email = "khach" + i + "." + faker.number().numberBetween(100, 999) + "@annam.test";
                String address = faker.number().numberBetween(1, 180) + " "
                        + faker.address().streetAddress() + ", " + faker.address().city();
                ensureUser(email, phone, faker.name().fullName(), "Customer@123", Role.CUSTOMER, address);
            }
            if (orders.count() == 0 && firstInStock != null) {
                cartService.addItem(demo.id(), firstInStock.id(), 1);
                orderService.checkout(demo.id(), demo.address(), "Đơn mẫu khi khởi tạo hệ thống");
            }
            log.info("Đã nạp {} danh mục, {} sản phẩm bằng Faker và danh mục mẫu",
                    categoryRepository.count(), productRepository.count());
        } else {
            log.info("Dữ liệu mẫu đã có sẵn, bỏ qua nạp danh mục và sản phẩm");
        }
        log.info("Tài khoản quản trị: {} / Admin@123. Tài khoản khách: demo@annam.vn / Customer@123", admin.email());
    }

    private CustomerView ensureUser(
            String email,
            String phone,
            String fullName,
            String password,
            Role role,
            String address
    ) {
        return customers.findByEmailIgnoreCase(email)
                .map(CustomerService::toView)
                .orElseGet(() -> {
                    Customer customer = new Customer();
                    customer.setEmail(email.toLowerCase(Locale.ROOT));
                    customer.setPhone(phone);
                    customer.setFullName(fullName);
                    customer.setPasswordHash(passwordEncoder.encode(password));
                    customer.setRole(role);
                    customer.setAddress(address);
                    customer.setCreatedAt(Instant.now());
                    Customer saved = customers.save(customer);
                    Cart cart = new Cart();
                    cart.setCustomer(saved);
                    carts.save(cart);
                    return CustomerService.toView(saved);
                });
    }
}

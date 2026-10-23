package com.annam.shop.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.annam.shop.domain.Cart;
import com.annam.shop.domain.Customer;
import com.annam.shop.domain.Role;
import com.annam.shop.repo.CartRepository;
import com.annam.shop.repo.CustomerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShopFlowIntegrationTest {

    private static final AtomicInteger PHONES = new AtomicInteger(1_000_000);

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CustomerRepository customers;
    @Autowired
    private CartRepository carts;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void ensureAdmin() throws Exception {
        if (customers.findByEmailIgnoreCase("admin@annam.vn").isEmpty()) {
            Customer admin = new Customer();
            admin.setFullName("Quản trị AnNam");
            admin.setEmail("admin@annam.vn");
            admin.setPhone("0900000001");
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            admin.setAddress("1 Tràng Tiền, Hoàn Kiếm, Hà Nội");
            admin.setRole(Role.ADMIN);
            admin.setCreatedAt(Instant.now());
            Customer saved = customers.save(admin);
            Cart cart = new Cart();
            cart.setCustomer(saved);
            carts.save(cart);
        }
        adminToken = login("admin@annam.vn", "Admin@123");
    }

    @Test
    void rejectsInvalidEmailPhoneAndPassword() throws Exception {
        mockMvc.perform(json(post("/api/v1/customers"), """
                {"fullName":"An Nam","email":"khong-phai-email","phone":"0901234567","password":"Customer@123"}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR_VALIDATION"))
                .andExpect(jsonPath("$.details[0].field").value("email"));

        mockMvc.perform(json(post("/api/v1/customers"), """
                {"fullName":"An Nam","email":"a@b.vn","phone":"12345","password":"Customer@123"}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR_VALIDATION"));

        mockMvc.perform(json(post("/api/v1/customers"), """
                {"fullName":"An Nam","email":"a@b.vn","phone":"0901234567","password":"short"}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR_VALIDATION"));
    }

    @Test
    void registerLoginAndRejectsDuplicateEmail() throws Exception {
        String email = email();
        MvcResult created = mockMvc.perform(json(post("/api/v1/customers"), customerJson(email, phone(), "12 Hàng Bông")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();
        assertThat(created.getResponse().getContentAsString()).doesNotContain("passwordHash");

        mockMvc.perform(json(post("/api/v1/customers"), customerJson(email, phone(), "12 Hàng Bông")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ERR_EMAIL_EXISTS"));

        mockMvc.perform(json(post("/api/v1/sessions"), """
                {"email":"%s","password":"sai-mat-khau"}
                """.formatted(email)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ERR_INVALID_CREDENTIALS"));

        String token = login(email, "Customer@123");
        mockMvc.perform(get("/api/v1/customers/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void filtersProductsByPriceAndKeyword() throws Exception {
        long categoryId = createCategory("Danh mục " + UUID.randomUUID());
        String token = "LOC-" + UUID.randomUUID().toString().substring(0, 8);
        createProduct(categoryId, token + " re", 50_000, 4);
        createProduct(categoryId, token + " dat", 200_000, 4);

        mockMvc.perform(get("/api/v1/products")
                        .param("q", token)
                        .param("minPrice", "100000")
                        .param("maxPrice", "250000")
                        .param("sort", "priceAsc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value(containsString("dat")))
                .andExpect(jsonPath("$.content[0].price").value(200_000));

        mockMvc.perform(get("/api/v1/products").param("q", token).param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void checkoutDeductsStockAndShowsOrderCode() throws Exception {
        long categoryId = createCategory("Bếp " + UUID.randomUUID());
        long productId = createProduct(categoryId, "Ấm test", 100_000, 5);
        String email = email();
        register(email, "18 Hàng Gai, Hà Nội");
        String token = login(email, "Customer@123");

        mockMvc.perform(json(post("/api/v1/carts/me/items").header(HttpHeaders.AUTHORIZATION, bearer(token)), """
                {"productId":%d,"quantity":2}
                """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(200_000));

        mockMvc.perform(json(post("/api/v1/orders").header(HttpHeaders.AUTHORIZATION, bearer(token)), """
                {"note":"Giao giờ hành chính"}
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.matchesPattern("ORD-\\d{8}-[A-Z0-9]{8}")))
                .andExpect(jsonPath("$.totalAmount").value(200_000))
                .andExpect(jsonPath("$.shippingAddress").value("18 Hàng Gai, Hà Nội"));

        mockMvc.perform(get("/api/v1/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(3));

        mockMvc.perform(get("/api/v1/carts/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void checkoutFailsWhenStockIsInsufficientAndRollsBack() throws Exception {
        long categoryId = createCategory("Kho " + UUID.randomUUID());
        long enoughId = createProduct(categoryId, "Đủ hàng", 80_000, 5);
        long emptyId = createProduct(categoryId, "Hết hàng", 40_000, 0);
        String email = email();
        register(email, "20 Lý Thường Kiệt");
        String token = login(email, "Customer@123");
        addToCart(token, enoughId, 2);
        addToCart(token, emptyId, 1);

        mockMvc.perform(json(post("/api/v1/orders").header(HttpHeaders.AUTHORIZATION, bearer(token)), "{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ERR_STOCK_EMPTY"))
                .andExpect(jsonPath("$.message").value("Sản phẩm đã hết hàng"))
                .andExpect(jsonPath("$.details[0].field").value("productId"));

        mockMvc.perform(get("/api/v1/products/" + enoughId))
                .andExpect(jsonPath("$.stockQuantity").value(5));
        mockMvc.perform(get("/api/v1/products/" + emptyId))
                .andExpect(jsonPath("$.stockQuantity").value(0));
        mockMvc.perform(get("/api/v1/customers/me/orders").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void statusMovesForwardAndCancelRestoresStock() throws Exception {
        long categoryId = createCategory("Đơn " + UUID.randomUUID());
        long productId = createProduct(categoryId, "Đèn", 150_000, 4);
        String email = email();
        register(email, "5 Đinh Tiên Hoàng");
        String token = login(email, "Customer@123");
        addToCart(token, productId, 1);
        long orderId = checkout(token);

        mockMvc.perform(json(patch("/api/v1/orders/" + orderId).header(HttpHeaders.AUTHORIZATION, bearer(token)), """
                {"status":"PAID"}
                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));

        mockMvc.perform(json(patch("/api/v1/orders/" + orderId).header(HttpHeaders.AUTHORIZATION, bearer(adminToken)), """
                {"status":"SHIPPED"}
                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ERR_INVALID_STATUS"));

        mockMvc.perform(json(patch("/api/v1/orders/" + orderId).header(HttpHeaders.AUTHORIZATION, bearer(adminToken)), """
                {"status":"PAID"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(json(patch("/api/v1/orders/" + orderId).header(HttpHeaders.AUTHORIZATION, bearer(adminToken)), """
                {"status":"SHIPPED"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));

        mockMvc.perform(json(patch("/api/v1/orders/" + orderId).header(HttpHeaders.AUTHORIZATION, bearer(adminToken)), """
                {"status":"CANCELLED"}
                """))
                .andExpect(status().isUnprocessableEntity());

        String email2 = email();
        register(email2, "9 Hai Bà Trưng");
        String token2 = login(email2, "Customer@123");
        long secondProduct = createProduct(categoryId, "Gối", 90_000, 2);
        addToCart(token2, secondProduct, 2);
        long cancelId = checkout(token2);
        mockMvc.perform(get("/api/v1/products/" + secondProduct)).andExpect(jsonPath("$.stockQuantity").value(0));

        mockMvc.perform(json(patch("/api/v1/orders/" + cancelId).header(HttpHeaders.AUTHORIZATION, bearer(token2)), """
                {"status":"CANCELLED"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        mockMvc.perform(get("/api/v1/products/" + secondProduct)).andExpect(jsonPath("$.stockQuantity").value(2));
    }

    @Test
    void emptyCartAndMissingProductAndCartSecurity() throws Exception {
        mockMvc.perform(get("/api/v1/carts/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ERR_UNAUTHORIZED"));

        mockMvc.perform(get("/api/v1/products/999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ERR_NOT_FOUND"));

        String email = email();
        register(email, "1 Lê Duẩn");
        String token = login(email, "Customer@123");
        mockMvc.perform(json(post("/api/v1/orders").header(HttpHeaders.AUTHORIZATION, bearer(token)), """
                {"shippingAddress":"1 Lê Duẩn"}
                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ERR_CART_EMPTY"));

        long categoryId = createCategory("Xóa " + UUID.randomUUID());
        long productId = createProduct(categoryId, "Sổ", 95_000, 3);
        addToCart(token, productId, 1);
        JsonNode cart = read(mockMvc.perform(get("/api/v1/carts/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn());
        long itemId = cart.get("items").get(0).get("id").asLong();
        mockMvc.perform(delete("/api/v1/carts/me/items/" + itemId).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(not(containsString("{"))));

        mockMvc.perform(delete("/api/v1/products/" + productId).header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    void customerCanUpdateProfile() throws Exception {
        String email = email();
        register(email, "3 Nguyễn Huệ");
        String token = login(email, "Customer@123");
        String newPhone = phone();
        mockMvc.perform(json(put("/api/v1/customers/me").header(HttpHeaders.AUTHORIZATION, bearer(token)), """
                {"fullName":"Tên mới","phone":"%s","address":"88 Pasteur"}
                """.formatted(newPhone)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Tên mới"))
                .andExpect(jsonPath("$.phone").value(newPhone))
                .andExpect(jsonPath("$.address").value("88 Pasteur"));
    }

    private long checkout(String token) throws Exception {
        MvcResult result = mockMvc.perform(json(post("/api/v1/orders").header(HttpHeaders.AUTHORIZATION, bearer(token)), "{}"))
                .andExpect(status().isCreated())
                .andReturn();
        return read(result).get("id").asLong();
    }

    private void addToCart(String token, long productId, int quantity) throws Exception {
        mockMvc.perform(json(post("/api/v1/carts/me/items").header(HttpHeaders.AUTHORIZATION, bearer(token)), """
                {"productId":%d,"quantity":%d}
                """.formatted(productId, quantity)))
                .andExpect(status().isCreated());
    }

    private long createCategory(String name) throws Exception {
        MvcResult result = mockMvc.perform(json(post("/api/v1/categories").header(HttpHeaders.AUTHORIZATION, bearer(adminToken)), """
                {"name":"%s","description":"Mô tả"}
                """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn();
        return read(result).get("id").asLong();
    }

    private long createProduct(long categoryId, String name, long price, int stock) throws Exception {
        MvcResult result = mockMvc.perform(json(post("/api/v1/products").header(HttpHeaders.AUTHORIZATION, bearer(adminToken)), """
                {"name":"%s","description":"Mô tả sản phẩm kiểm thử","price":%d,"stockQuantity":%d,"categoryId":%d}
                """.formatted(name, price, stock, categoryId)))
                .andExpect(status().isCreated())
                .andReturn();
        return read(result).get("id").asLong();
    }

    private void register(String email, String address) throws Exception {
        mockMvc.perform(json(post("/api/v1/customers"), customerJson(email, phone(), address)))
                .andExpect(status().isCreated());
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(json(post("/api/v1/sessions"), """
                {"email":"%s","password":"%s"}
                """.formatted(email, password)))
                .andExpect(status().isCreated())
                .andReturn();
        return read(result).get("accessToken").asText();
    }

    private static String customerJson(String email, String phone, String address) {
        return """
                {"fullName":"Khách kiểm thử","email":"%s","phone":"%s","password":"Customer@123","address":"%s"}
                """.formatted(email, phone, address);
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder json(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder,
            String body
    ) {
        return builder.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private JsonNode read(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static String email() {
        return "khach." + UUID.randomUUID() + "@annam.test";
    }

    private static String phone() {
        return "098" + String.format("%07d", PHONES.getAndIncrement());
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}

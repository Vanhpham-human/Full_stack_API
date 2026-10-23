# AnNam Shop

Đồ án fullstack: cửa hàng đồ dùng nhà, làm theo quy trình API-first với Spring Boot 3.5, Java 21, MySQL và React.

## Ánh xạ yêu cầu

| Hạng mục | Nằm ở đâu |
| --- | --- |
| User story, luồng đặt hàng, hủy đơn, trạng thái | `docs/BA.md` |
| OpenAPI 3.0 | `api/shop-api.yaml` |
| Mock server | `npm run mock` (Prism, cổng 4010) |
| Contract test | `api/postman/AnNam-Shop.postman_collection.json`, kịch bản Gherkin ở `api/features/shop.feature` |
| Codegen server stub và record bất biến | `backend/target/generated-sources/openapi` sau `mvn compile` |
| `@Transactional(readOnly = true)` cho hàm đọc, giao dịch checkout | `backend/src/main/java/com/annam/shop/service` |
| Nạp dữ liệu bằng Faker | `backend/src/main/java/com/annam/shop/seed/DataSeeder.java` |
| Storefront và trang quản trị | `frontend/` |
| Unit, integration, E2E | `backend/src/test`, `e2e/checkout-flow.mjs`, `docs/TEST-REPORT.md` |

Đường dẫn chỉ dùng danh từ số nhiều: `/api/v1/customers`, `/api/v1/products`, `/api/v1/orders`, `/api/v1/carts`, `/api/v1/categories`, `/api/v1/sessions`.

## Chuẩn bị

- Java 21
- Maven 3.9+
- Node.js 20+ cho giao diện, Prism và Newman
- MySQL 8, hoặc Docker để chạy `docker compose up -d`

MySQL mặc định: database `shopdb`, user `shop`, mật khẩu `shop`, cổng `3306`.

Nếu máy chưa có MySQL:

```bash
docker compose up -d
```

Hoặc chạy không cần MySQL, dữ liệu lưu file H2:

```bash
./scripts/backend.sh -Dspring-boot.run.profiles=h2
```

## Chạy backend

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
cd backend
mvn test
mvn spring-boot:run
```

API: `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui.html`.

Nếu cổng 8080 đang bị chương trình khác chiếm, chạy `PORT=8082 mvn spring-boot:run` và đặt `VITE_API_BASE_URL=http://localhost:8082` trước `npm run dev`.

`mvn compile` sinh interface và các Java record vào `backend/target/generated-sources/openapi`. Thư mục đó là đầu ra build, không commit.

## Giai đoạn 1 — mock

```bash
export PATH="$PWD/.tools/node/bin:$PATH"   # nếu dùng Node đi kèm đồ án
npm install
npm run mock
```

Giao diện trỏ vào mock:

```bash
cd frontend
npm install
npm run dev:mock
```

Prism trả ví dụ tĩnh trong `shop-api.yaml`. Danh sách sản phẩm ghép được. Giỏ hàng chưa có trạng thái thật. Giai đoạn 2 đổi `VITE_API_BASE_URL` sang `http://localhost:8080` (file `frontend/.env.development`).

## Giai đoạn 2 — cửa hàng

```bash
cd frontend
npm install
npm run dev
```

Mở `http://127.0.0.1:5173`.

| Vai trò | Email | Mật khẩu |
| --- | --- | --- |
| Khách hàng | demo@annam.vn | Customer@123 |
| Quản trị | admin@annam.vn | Admin@123 |

Cửa hàng có tìm kiếm, lọc danh mục, lọc giá và phân trang. Quản trị nằm ở `/admin/orders` và `/admin/products`.

## Kiểm thử

```bash
cd backend && mvn test
node e2e/checkout-flow.mjs
npm run contract
```

`npm run contract` cần backend đang chạy và đã nạp dữ liệu mẫu. Kết quả ghi ở `docs/TEST-REPORT.md`.

## Biến môi trường

| Biến | Mặc định |
| --- | --- |
| `DB_USERNAME` / `DB_PASSWORD` | `shop` / `shop` |
| `JWT_SECRET` | chuỗi dev dài hơn 32 byte, nên đổi khi triển khai |
| `PORT` | `8080` |
| `VITE_API_BASE_URL` | `http://localhost:8080` |

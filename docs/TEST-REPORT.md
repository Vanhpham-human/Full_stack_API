# Báo cáo kiểm thử — AnNam Shop

Ngày chạy: 23/10/2026. Backend Spring Boot 3.5.16, Java 21, MySQL 8.4.11. Giao diện Vite tại `http://127.0.0.1:5173`.

Trên máy này cổng 8080 và 8081 đã có ứng dụng Java khác, nên API của đồ án chạy ở `http://localhost:8082`.

## Unit và integration

`mvn test` trong `backend/`: 16 ca, 0 lỗi.

| Lớp | Ca | Kết quả |
| --- | --- | --- |
| `OrderServiceTest` | 5 | Đặt hàng trừ kho, thiếu hàng không đổi số lượng, giỏ trống, hủy hoàn kho, đơn đã giao không hủy |
| `TransactionalBoundaryTest` | 3 | Hàm đọc `readOnly = true`, checkout và đổi trạng thái là giao dịch ghi |
| `ShopFlowIntegrationTest` | 8 | MockMvc: sai email/số điện thoại/mật khẩu, đăng ký và trùng email, lọc giá và từ khóa, đặt hàng, hết hàng không tạo đơn, chuyển trạng thái, hủy hoàn kho, 401/403/404/204 |

## Contract test

Newman, collection `api/postman/AnNam-Shop.postman_collection.json`, base URL `http://localhost:8082`.

- 28 request, 28 assertion, 0 lỗi
- Thời gian chạy 1081 ms
- Có `400 ERR_VALIDATION`, `422 ERR_EMAIL_EXISTS`, `401`, `403`, `404`, `204`, `422 ERR_STOCK_EMPTY` với message `Sản phẩm đã hết hàng`, và hoàn kho sau khi hủy

## E2E API

`API_BASE_URL=http://localhost:8082 node e2e/checkout-flow.mjs`

Đăng ký khách mới, thêm hàng, đặt đơn. Mã `ORD-20261023-8FE5BBD5`, trạng thái `PENDING`. Tồn kho Thảm ngồi picnic giảm từ 15 xuống 14.

## Giao diện

Đã thao tác trên trình duyệt:

- Lọc danh mục Nhà bếp, tìm `ấm` trong khoảng giá 100.000–900.000 ₫, phân trang sang trang 2
- Tìm từ không có kết quả: hiện “Không có sản phẩm phù hợp.”
- Khách `demo@annam.vn` thêm Ấm đun inox, đặt hàng, thấy mã `ORD-20261023-AB85C957`
- Hủy đơn đó, tồn kho ấm quay lại 16
- Quản trị chuyển một đơn `PENDING` sang `PAID`, nút tiếp theo là “Đã giao”
- Quản trị lưu tồn kho trên `/admin/products` và nhận thông báo đã cập nhật
- Khung 390×844 px: menu và bộ lọc nằm trong bề rộng màn hình, không tràn ngang

`npm run build` ở `frontend/` thành công.

# language: vi
Tính năng: Hợp đồng API cửa hàng AnNam

  Kịch bản: Từ chối dữ liệu đăng ký sai định dạng
    Khi khách gửi email hoặc số điện thoại hoặc mật khẩu không hợp lệ
    Thì API trả 400 với code ERR_VALIDATION và mảng details

  Kịch bản: Đăng ký và đăng nhập
    Khi khách đăng ký email chưa tồn tại
    Thì API trả 201 và không có mật khẩu trong phản hồi
    Khi đăng ký lại cùng email
    Thì API trả 422 ERR_EMAIL_EXISTS
    Khi đăng nhập sai mật khẩu
    Thì API trả 401 ERR_INVALID_CREDENTIALS

  Kịch bản: Tìm sản phẩm theo giá và từ khóa
    Khi khách lọc theo từ khóa, giá tối thiểu và giá tối đa
    Thì API trả trang sản phẩm đúng điều kiện, có page, size, totalElements, totalPages

  Kịch bản: Đặt hàng thành công
    Khi khách thêm sản phẩm còn hàng vào giỏ và đặt hàng
    Thì API tạo đơn PENDING, trả mã ORD-YYYYMMDD-XXXXXXXX, trừ tồn kho và làm trống giỏ

  Kịch bản: Đặt hàng khi hết tồn kho
    Khi một dòng trong giỏ vượt số lượng tồn
    Thì API trả 422 ERR_STOCK_EMPTY với message "Sản phẩm đã hết hàng"
    Và không tạo đơn, không trừ tồn kho dòng khác

  Kịch bản: Chuyển trạng thái và hủy đơn
    Khi quản trị chuyển PENDING sang SHIPPED
    Thì API trả 422 ERR_INVALID_STATUS
    Khi quản trị chuyển PENDING sang PAID rồi sang SHIPPED
    Thì đơn đi hết luồng hợp lệ
    Khi khách hủy đơn của mình lúc PENDING hoặc PAID
    Thì tồn kho được hoàn lại và trạng thái là CANCELLED
    Khi đơn đã SHIPPED
    Thì không thể hủy

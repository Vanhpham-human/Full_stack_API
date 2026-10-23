const baseUrl = process.env.API_BASE_URL || "http://localhost:8080";

async function request(path, { method = "GET", token, body } = {}) {
  const response = await fetch(`${baseUrl}${path}`, {
    method,
    headers: {
      Accept: "application/json",
      ...(body ? { "Content-Type": "application/json" } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  const text = await response.text();
  const payload = text ? JSON.parse(text) : null;
  if (!response.ok) {
    throw new Error(`${method} ${path} -> ${response.status} ${text}`);
  }
  return payload;
}

const stamp = Date.now().toString();
const email = `e2e.${stamp}@annam.test`;
const phone = `09${stamp.slice(-8)}`;

await request("/api/v1/customers", {
  method: "POST",
  body: {
    fullName: "Khách E2E",
    email,
    phone,
    password: "Customer@123",
    address: "18 Hàng Gai, Hoàn Kiếm, Hà Nội",
  },
});

const session = await request("/api/v1/sessions", {
  method: "POST",
  body: { email, password: "Customer@123" },
});
const token = session.accessToken;

const catalog = await request("/api/v1/products?size=60");
const product = catalog.content.find((item) => item.stockQuantity > 0);
if (!product) throw new Error("Không có sản phẩm còn hàng để đặt");

await request("/api/v1/carts/me/items", {
  method: "POST",
  token,
  body: { productId: product.id, quantity: 1 },
});

const order = await request("/api/v1/orders", {
  method: "POST",
  token,
  body: { note: "Đơn kiểm thử đầu cuối" },
});

if (!/^ORD-\d{8}-[A-Z0-9]{8}$/.test(order.code)) {
  throw new Error(`Mã đơn không đúng định dạng: ${order.code}`);
}
if (order.status !== "PENDING") throw new Error(`Trạng thái sai: ${order.status}`);

const after = await request(`/api/v1/products/${product.id}`);
if (after.stockQuantity !== product.stockQuantity - 1) {
  throw new Error(`Tồn kho không giảm: ${product.stockQuantity} -> ${after.stockQuantity}`);
}

const viewed = await request(`/api/v1/orders/${order.id}`, { token });
if (viewed.code !== order.code) throw new Error("Không đọc lại được đơn vừa tạo");

console.log(`E2E thành công. Mã đơn ${order.code}. Tồn kho ${product.name}: ${product.stockQuantity} -> ${after.stockQuantity}`);

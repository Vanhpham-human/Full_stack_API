const baseUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export class ApiError extends Error {
  constructor(status, body) {
    super(body?.message || "Có lỗi xảy ra");
    this.status = status;
    this.code = body?.code || "ERR_INTERNAL";
    this.details = body?.details || [];
  }
}

async function request(path, { method = "GET", body, token } = {}) {
  const headers = { Accept: "application/json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (token) headers.Authorization = `Bearer ${token}`;
  let response;
  try {
    response = await fetch(`${baseUrl}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(0, { code: "ERR_NETWORK", message: "Không kết nối được máy chủ", details: [] });
  }
  if (response.status === 204) return null;
  const text = await response.text();
  let payload = null;
  if (text) {
    try {
      payload = JSON.parse(text);
    } catch {
      payload = { message: text, details: [] };
    }
  }
  if (!response.ok) {
    if (response.status === 401 && token) {
      window.dispatchEvent(new Event("annam:unauthorized"));
    }
    throw new ApiError(response.status, payload);
  }
  return payload;
}

function query(params) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") search.set(key, String(value));
  });
  const text = search.toString();
  return text ? `?${text}` : "";
}

export const api = {
  baseUrl,
  register: (body) => request("/api/v1/customers", { method: "POST", body }),
  login: (body) => request("/api/v1/sessions", { method: "POST", body }),
  logout: (token) => request("/api/v1/sessions/current", { method: "DELETE", token }),
  me: (token) => request("/api/v1/customers/me", { token }),
  updateMe: (token, body) => request("/api/v1/customers/me", { method: "PUT", token, body }),
  categories: () => request("/api/v1/categories"),
  products: (params) => request(`/api/v1/products${query(params)}`),
  product: (id) => request(`/api/v1/products/${id}`),
  createProduct: (token, body) => request("/api/v1/products", { method: "POST", token, body }),
  updateStock: (token, id, stockQuantity) =>
    request(`/api/v1/products/${id}`, { method: "PATCH", token, body: { stockQuantity } }),
  deleteProduct: (token, id) => request(`/api/v1/products/${id}`, { method: "DELETE", token }),
  cart: (token) => request("/api/v1/carts/me", { token }),
  addCartItem: (token, body) => request("/api/v1/carts/me/items", { method: "POST", token, body }),
  updateCartItem: (token, itemId, quantity) =>
    request(`/api/v1/carts/me/items/${itemId}`, { method: "PUT", token, body: { quantity } }),
  removeCartItem: (token, itemId) => request(`/api/v1/carts/me/items/${itemId}`, { method: "DELETE", token }),
  checkout: (token, body) => request("/api/v1/orders", { method: "POST", token, body }),
  myOrders: (token, params) => request(`/api/v1/customers/me/orders${query(params)}`, { token }),
  orders: (token, params) => request(`/api/v1/orders${query(params)}`, { token }),
  order: (token, id) => request(`/api/v1/orders/${id}`, { token }),
  updateOrderStatus: (token, id, status) =>
    request(`/api/v1/orders/${id}`, { method: "PATCH", token, body: { status } }),
};

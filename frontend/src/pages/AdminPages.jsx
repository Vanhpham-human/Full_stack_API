import { useEffect, useState } from "react";
import { NavLink, useLocation } from "react-router-dom";
import { api } from "../api";
import { useAuth } from "../auth";
import { StatusBadge } from "../components/StatusBadge";
import { errorText, formatDate, formatVnd } from "../format";

function AdminNav() {
  return (
    <div className="admin-nav">
      <NavLink to="/admin/orders">Đơn hàng</NavLink>
      <NavLink to="/admin/products">Tồn kho</NavLink>
    </div>
  );
}

const NEXT = {
  PENDING: ["PAID", "CANCELLED"],
  PAID: ["SHIPPED", "CANCELLED"],
};

export function AdminOrdersPage() {
  const { token } = useAuth();
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(null);
  const [error, setError] = useState("");

  function load(nextStatus = status) {
    api.orders(token, { status: nextStatus || undefined, page: 0, size: 20 })
      .then(setPage)
      .catch((err) => setError(errorText(err)));
  }

  useEffect(() => {
    if (token) load("");
  }, [token]);

  async function change(order, next) {
    if (next === "CANCELLED" && !window.confirm(`Hủy ${order.code} và hoàn tồn kho?`)) return;
    setError("");
    try {
      await api.updateOrderStatus(token, order.id, next);
      load();
    } catch (err) {
      setError(errorText(err));
    }
  }

  return (
    <section>
      <AdminNav />
      <div className="title-row">
        <h1>Đơn hàng</h1>
        <select value={status} onChange={(event) => { setStatus(event.target.value); load(event.target.value); }}>
          <option value="">Mọi trạng thái</option>
          <option value="PENDING">Chờ xử lý</option>
          <option value="PAID">Đã thanh toán</option>
          <option value="SHIPPED">Đã giao</option>
          <option value="CANCELLED">Đã hủy</option>
        </select>
      </div>
      {error ? <p className="alert">{error}</p> : null}
      <div className="table-wrap">
        <table>
          <thead>
            <tr><th>Mã</th><th>Khách</th><th>Thời điểm</th><th>Tổng</th><th>Trạng thái</th><th>Thao tác</th></tr>
          </thead>
          <tbody>
            {page?.content.map((order) => (
              <tr key={order.id}>
                <td>{order.code}</td>
                <td>{order.customerName}</td>
                <td>{formatDate(order.createdAt)}</td>
                <td>{formatVnd(order.totalAmount)}</td>
                <td><StatusBadge status={order.status} /></td>
                <td className="actions">
                  {(NEXT[order.status] || []).map((next) => (
                    <button key={next} type="button" className={next === "CANCELLED" ? "text danger" : "text"} onClick={() => change(order, next)}>
                      {next === "PAID" ? "Đã thanh toán" : next === "SHIPPED" ? "Đã giao" : "Hủy"}
                    </button>
                  ))}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {page && page.content.length === 0 ? <p className="empty">Chưa có đơn.</p> : null}
    </section>
  );
}

export function AdminProductsPage() {
  const { token } = useAuth();
  const location = useLocation();
  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [drafts, setDrafts] = useState({});
  const [form, setForm] = useState({ name: "", description: "", price: "", stockQuantity: "10", categoryId: "", imageUrl: "" });
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  async function load() {
    const [cats, page] = await Promise.all([
      api.categories(),
      api.products({ page: 0, size: 60, sort: "name" }),
    ]);
    setCategories(cats);
    setProducts(page.content);
    setDrafts(Object.fromEntries(page.content.map((product) => [product.id, String(product.stockQuantity)])));
    setForm((current) => ({ ...current, categoryId: current.categoryId || String(cats[0]?.id || "") }));
  }

  useEffect(() => {
    if (token) load().catch((err) => setError(errorText(err)));
  }, [token, location.pathname]);

  async function saveStock(product) {
    setError("");
    setMessage("");
    try {
      await api.updateStock(token, product.id, Number(drafts[product.id]));
      setMessage(`Đã cập nhật tồn kho của ${product.name}.`);
      await load();
    } catch (err) {
      setError(errorText(err));
    }
  }

  async function create(event) {
    event.preventDefault();
    setError("");
    setMessage("");
    try {
      await api.createProduct(token, {
        name: form.name,
        description: form.description,
        price: Number(form.price),
        stockQuantity: Number(form.stockQuantity),
        categoryId: Number(form.categoryId),
        imageUrl: form.imageUrl || undefined,
      });
      setForm({ name: "", description: "", price: "", stockQuantity: "10", categoryId: form.categoryId, imageUrl: "" });
      setMessage("Đã thêm sản phẩm.");
      await load();
    } catch (err) {
      setError(errorText(err));
    }
  }

  async function remove(product) {
    if (!window.confirm(`Xóa ${product.name}?`)) return;
    setError("");
    try {
      await api.deleteProduct(token, product.id);
      await load();
    } catch (err) {
      setError(errorText(err));
    }
  }

  return (
    <section>
      <AdminNav />
      <h1>Tồn kho sản phẩm</h1>
      {error ? <p className="alert">{error}</p> : null}
      {message ? <p className="ok">{message}</p> : null}
      <form className="filters" onSubmit={create}>
        <label>Tên<input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} /></label>
        <label>Mô tả<input required value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} /></label>
        <label>Giá<input required inputMode="numeric" value={form.price} onChange={(event) => setForm({ ...form, price: event.target.value })} /></label>
        <label>Tồn<input required inputMode="numeric" value={form.stockQuantity} onChange={(event) => setForm({ ...form, stockQuantity: event.target.value })} /></label>
        <label>Danh mục
          <select value={form.categoryId} onChange={(event) => setForm({ ...form, categoryId: event.target.value })}>
            {categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
          </select>
        </label>
        <button className="button" type="submit">Thêm</button>
      </form>
      <div className="table-wrap">
        <table>
          <thead>
            <tr><th>Sản phẩm</th><th>Danh mục</th><th>Giá</th><th>Tồn kho</th><th></th></tr>
          </thead>
          <tbody>
            {products.map((product) => (
              <tr key={product.id}>
                <td>{product.name}</td>
                <td>{product.categoryName}</td>
                <td>{formatVnd(product.price)}</td>
                <td>
                  <input
                    className="stock-input"
                    inputMode="numeric"
                    value={drafts[product.id] ?? ""}
                    onChange={(event) => setDrafts({ ...drafts, [product.id]: event.target.value })}
                  />
                </td>
                <td className="actions">
                  <button type="button" className="text" onClick={() => saveStock(product)}>Lưu</button>
                  <button type="button" className="text danger" onClick={() => remove(product)}>Xóa</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

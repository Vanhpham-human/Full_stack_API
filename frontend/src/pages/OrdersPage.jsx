import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api";
import { useAuth } from "../auth";
import { StatusBadge } from "../components/StatusBadge";
import { errorText, formatDate, formatVnd } from "../format";

export function OrdersPage() {
  const { token } = useAuth();
  const [page, setPage] = useState(null);
  const [error, setError] = useState("");

  function load() {
    api.myOrders(token, { page: 0, size: 20 }).then(setPage).catch((err) => setError(errorText(err)));
  }

  useEffect(() => {
    if (token) load();
  }, [token]);

  async function cancel(order) {
    if (!window.confirm(`Hủy đơn ${order.code}? Tồn kho sẽ được hoàn lại.`)) return;
    setError("");
    try {
      await api.updateOrderStatus(token, order.id, "CANCELLED");
      load();
    } catch (err) {
      setError(errorText(err));
    }
  }

  if (!page) return <p className="muted">Đang tải đơn hàng...</p>;

  return (
    <section>
      <h1>Đơn hàng của tôi</h1>
      {error ? <p className="alert">{error}</p> : null}
      {page.content.length === 0 ? <p className="empty">Bạn chưa có đơn nào.</p> : null}
      <div className="stack">
        {page.content.map((order) => (
          <article key={order.id} className="panel row">
            <div>
              <Link to={`/orders/${order.id}`}><strong>{order.code}</strong></Link>
              <p className="muted">{formatDate(order.createdAt)}</p>
            </div>
            <StatusBadge status={order.status} />
            <strong>{formatVnd(order.totalAmount)}</strong>
            {order.status === "PENDING" || order.status === "PAID" ? (
              <button type="button" className="text danger" onClick={() => cancel(order)}>Hủy đơn</button>
            ) : <span />}
          </article>
        ))}
      </div>
    </section>
  );
}

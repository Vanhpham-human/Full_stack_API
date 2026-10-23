import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import { api } from "../api";
import { useAuth } from "../auth";
import { StatusBadge } from "../components/StatusBadge";
import { errorText, formatDate, formatVnd } from "../format";

export function OrderDetailPage() {
  const { id } = useParams();
  const location = useLocation();
  const { token } = useAuth();
  const [order, setOrder] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!token) return;
    api.order(token, id).then(setOrder).catch((err) => setError(errorText(err)));
  }, [token, id]);

  if (!token) return <p className="muted">Bạn cần đăng nhập.</p>;
  if (error) return <p className="alert">{error}</p>;
  if (!order) return <p className="muted">Đang tải đơn hàng...</p>;

  return (
    <section className="narrow">
      {location.state?.justPlaced ? (
        <div className="success">
          <p className="eyebrow">Đặt hàng thành công</p>
          <h1>Mã đơn {order.code}</h1>
          <p>Đơn đang ở trạng thái chờ xử lý. Tồn kho đã được trừ.</p>
        </div>
      ) : <h1>Đơn {order.code}</h1>}
      <p><StatusBadge status={order.status} /> <span className="muted">{formatDate(order.createdAt)}</span></p>
      <p>{order.shippingAddress}</p>
      {order.note ? <p>Ghi chú: {order.note}</p> : null}
      <ul className="plain">
        {order.items.map((item) => (
          <li key={item.id}>
            <span>{item.productName} × {item.quantity}</span>
            <span>{formatVnd(item.lineTotal)}</span>
          </li>
        ))}
      </ul>
      <p className="total"><span>Tổng</span><strong>{formatVnd(order.totalAmount)}</strong></p>
      <Link to="/orders">Về lịch sử đơn</Link>
    </section>
  );
}

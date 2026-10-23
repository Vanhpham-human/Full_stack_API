import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../api";
import { useAuth } from "../auth";
import { useCart } from "../cart";
import { errorText, formatVnd } from "../format";

export function CheckoutPage() {
  const { token, customer } = useAuth();
  const cartState = useCart();
  const cart = cartState.cart;
  const navigate = useNavigate();
  const [address, setAddress] = useState(customer?.address || "");
  const [note, setNote] = useState("");
  const [error, setError] = useState("");
  const [pending, setPending] = useState(false);

  if (!customer) return <p className="muted">Bạn cần đăng nhập.</p>;
  if (!cart) return <p className="muted">Đang tải...</p>;
  if (cart.items.length === 0) {
    return <section className="narrow"><h1>Đặt hàng</h1><p className="empty">Giỏ đang trống.</p></section>;
  }

  async function submit(event) {
    event.preventDefault();
    setPending(true);
    setError("");
    try {
      const order = await api.checkout(token, { shippingAddress: address, note });
      await cartState.refresh();
      navigate(`/orders/${order.id}`, { state: { justPlaced: true } });
    } catch (err) {
      setError(errorText(err));
    } finally {
      setPending(false);
    }
  }

  return (
    <section className="split">
      <form onSubmit={submit}>
        <h1>Hoàn tất đơn hàng</h1>
        <label>
          Địa chỉ giao hàng
          <textarea required maxLength={500} value={address} onChange={(event) => setAddress(event.target.value)} />
        </label>
        <label>
          Ghi chú
          <textarea maxLength={500} value={note} onChange={(event) => setNote(event.target.value)} />
        </label>
        {error ? <p className="alert">{error}</p> : null}
        <button className="button" type="submit" disabled={pending}>{pending ? "Đang đặt..." : "Đặt hàng"}</button>
      </form>
      <aside className="panel">
        <h2>Đơn của bạn</h2>
        <ul>
          {cart.items.map((item) => (
            <li key={item.id}><span>{item.productName} × {item.quantity}</span><span>{formatVnd(item.lineTotal)}</span></li>
          ))}
        </ul>
        <p className="total"><span>Tổng</span><strong>{formatVnd(cart.totalAmount)}</strong></p>
        <Link to="/cart">Sửa giỏ hàng</Link>
      </aside>
    </section>
  );
}

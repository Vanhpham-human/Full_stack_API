import { Link } from "react-router-dom";
import { useAuth } from "../auth";
import { useCart } from "../cart";
import { ProductImage } from "../components/ProductImage";
import { formatVnd } from "../format";

export function CartPage() {
  const { customer } = useAuth();
  const { cart, update, remove } = useCart();

  if (!customer) {
    return (
      <section className="narrow">
        <h1>Giỏ hàng</h1>
        <p>Đăng nhập để giữ giỏ hàng và đặt đơn.</p>
        <Link className="button" to="/login" state={{ from: "/cart" }}>Đăng nhập</Link>
      </section>
    );
  }
  if (!cart) return <p className="muted">Đang tải giỏ hàng...</p>;

  return (
    <section className="narrow">
      <h1>Giỏ hàng</h1>
      {cart.items.length === 0 ? (
        <p className="empty">Giỏ đang trống. <Link to="/">Xem sản phẩm</Link></p>
      ) : (
        <>
          <ul className="lines">
            {cart.items.map((item) => (
              <li key={item.id}>
                <div className="media tiny"><ProductImage src={item.imageUrl} name={item.productName} /></div>
                <div>
                  <Link to={`/products/${item.productId}`}>{item.productName}</Link>
                  <p>{formatVnd(item.unitPrice)}</p>
                  {item.quantity > item.stockQuantity ? (
                    <p className="alert inline">Vượt tồn kho, hiện còn {item.stockQuantity}. Đặt hàng sẽ bị từ chối nếu chưa được bổ sung.</p>
                  ) : null}
                </div>
                <label>
                  Số lượng
                  <input
                    type="number"
                    min="1"
                    max="99"
                    value={item.quantity}
                    onChange={(event) => update(item.id, Math.max(1, Math.min(99, Number(event.target.value) || 1)))}
                  />
                </label>
                <strong>{formatVnd(item.lineTotal)}</strong>
                <button type="button" className="text" onClick={() => remove(item.id)}>Xóa</button>
              </li>
            ))}
          </ul>
          <div className="summary">
            <span>Tạm tính</span>
            <strong>{formatVnd(cart.totalAmount)}</strong>
            <Link className="button" to="/checkout">Đặt hàng</Link>
          </div>
        </>
      )}
    </section>
  );
}

import { Link, NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../auth";
import { useCart } from "../cart";

export function Layout() {
  const { customer, logout } = useAuth();
  const { count, notice, clearNotice } = useCart();

  return (
    <>
      <header className="topbar">
        <Link to="/" className="brand">
          <span className="mark" aria-hidden="true" />
          AnNam
        </Link>
        <nav>
          <NavLink to="/" end>Cửa hàng</NavLink>
          <NavLink to="/cart">Giỏ hàng{count > 0 ? <em>{count}</em> : null}</NavLink>
          {customer ? <NavLink to="/orders">Đơn hàng</NavLink> : null}
          {customer?.role === "ADMIN" ? <NavLink to="/admin/orders">Quản trị</NavLink> : null}
        </nav>
        <div className="account">
          {customer ? (
            <>
              <Link to="/account">{customer.fullName}</Link>
              <button type="button" className="text" onClick={logout}>Đăng xuất</button>
            </>
          ) : (
            <>
              <Link to="/login">Đăng nhập</Link>
              <Link to="/register" className="button small">Đăng ký</Link>
            </>
          )}
        </div>
      </header>
      {notice ? (
        <div className={`toast toast-${notice.type}`} role="status">
          <span>{notice.text}</span>
          <button type="button" onClick={clearNotice} aria-label="Đóng">×</button>
        </div>
      ) : null}
      <main>
        <Outlet />
      </main>
      <footer>
        <strong>AnNam</strong>
        <span>Đồ dùng tử tế cho nhà của bạn.</span>
      </footer>
    </>
  );
}

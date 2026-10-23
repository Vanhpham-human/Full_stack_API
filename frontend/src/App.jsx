import { Navigate, Route, Routes, useLocation } from "react-router-dom";
import { useAuth } from "./auth";
import { Layout } from "./components/Layout";
import { AccountPage } from "./pages/AccountPage";
import { AdminOrdersPage, AdminProductsPage } from "./pages/AdminPages";
import { LoginPage, RegisterPage } from "./pages/AuthPages";
import { CartPage } from "./pages/CartPage";
import { CheckoutPage } from "./pages/CheckoutPage";
import { OrderDetailPage } from "./pages/OrderDetailPage";
import { OrdersPage } from "./pages/OrdersPage";
import { ProductPage } from "./pages/ProductPage";
import { StorePage } from "./pages/StorePage";

function RequireAuth({ role, children }) {
  const { customer, ready } = useAuth();
  const location = useLocation();
  if (!ready) return <p className="muted">Đang tải...</p>;
  if (!customer) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  if (role && customer.role !== role) return <Navigate to="/" replace />;
  return children;
}

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<StorePage />} />
        <Route path="products/:id" element={<ProductPage />} />
        <Route path="cart" element={<CartPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />
        <Route path="checkout" element={<RequireAuth><CheckoutPage /></RequireAuth>} />
        <Route path="account" element={<RequireAuth><AccountPage /></RequireAuth>} />
        <Route path="orders" element={<RequireAuth><OrdersPage /></RequireAuth>} />
        <Route path="orders/:id" element={<RequireAuth><OrderDetailPage /></RequireAuth>} />
        <Route path="admin/orders" element={<RequireAuth role="ADMIN"><AdminOrdersPage /></RequireAuth>} />
        <Route path="admin/products" element={<RequireAuth role="ADMIN"><AdminProductsPage /></RequireAuth>} />
        <Route path="admin" element={<Navigate to="/admin/orders" replace />} />
        <Route path="*" element={<section className="narrow"><h1>Không tìm thấy trang</h1></section>} />
      </Route>
    </Routes>
  );
}

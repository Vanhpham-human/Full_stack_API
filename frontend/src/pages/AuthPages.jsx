import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { api } from "../api";
import { useAuth } from "../auth";
import { errorText } from "../format";

export function LoginPage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState("demo@annam.vn");
  const [password, setPassword] = useState("Customer@123");
  const [error, setError] = useState("");

  async function submit(event) {
    event.preventDefault();
    setError("");
    try {
      const customer = await auth.login(email, password);
      const from = location.state?.from || (customer.role === "ADMIN" ? "/admin/orders" : "/");
      navigate(from, { replace: true });
    } catch (err) {
      setError(errorText(err));
    }
  }

  return (
    <section className="split auth">
      <form onSubmit={submit}>
        <h1>Đăng nhập</h1>
        <label>Email<input type="email" required value={email} onChange={(event) => setEmail(event.target.value)} /></label>
        <label>Mật khẩu<input type="password" required value={password} onChange={(event) => setPassword(event.target.value)} /></label>
        {error ? <p className="alert">{error}</p> : null}
        <button className="button" type="submit">Vào cửa hàng</button>
        <p>Chưa có tài khoản? <Link to="/register">Đăng ký</Link></p>
      </form>
      <aside className="panel">
        <h2>Tài khoản mẫu</h2>
        <p><strong>Khách hàng</strong><br />demo@annam.vn<br />Customer@123</p>
        <p><strong>Quản trị</strong><br />admin@annam.vn<br />Admin@123</p>
      </aside>
    </section>
  );
}

export function RegisterPage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ fullName: "", email: "", phone: "", password: "", address: "" });
  const [error, setError] = useState("");

  function set(field) {
    return (event) => setForm({ ...form, [field]: event.target.value });
  }

  async function submit(event) {
    event.preventDefault();
    setError("");
    try {
      await api.register(form);
      await auth.login(form.email, form.password);
      navigate("/", { replace: true });
    } catch (err) {
      setError(errorText(err));
    }
  }

  return (
    <section className="narrow">
      <form onSubmit={submit}>
        <h1>Đăng ký</h1>
        <label>Họ tên<input required minLength={2} value={form.fullName} onChange={set("fullName")} /></label>
        <label>Email<input type="email" required value={form.email} onChange={set("email")} /></label>
        <label>Số điện thoại<input required pattern="^(0|\+84)(3|5|7|8|9)[0-9]{8}$" placeholder="0901234567" value={form.phone} onChange={set("phone")} /></label>
        <label>Mật khẩu<input type="password" required minLength={8} value={form.password} onChange={set("password")} /></label>
        <label>Địa chỉ<textarea value={form.address} onChange={set("address")} /></label>
        {error ? <p className="alert">{error}</p> : null}
        <button className="button" type="submit">Tạo tài khoản</button>
      </form>
    </section>
  );
}

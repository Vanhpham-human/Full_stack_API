import { useState } from "react";
import { api } from "../api";
import { useAuth } from "../auth";
import { errorText } from "../format";

export function AccountPage() {
  const { token, customer, replaceCustomer } = useAuth();
  const [form, setForm] = useState({
    fullName: customer?.fullName || "",
    phone: customer?.phone || "",
    address: customer?.address || "",
  });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  function set(field) {
    return (event) => setForm({ ...form, [field]: event.target.value });
  }

  async function submit(event) {
    event.preventDefault();
    setError("");
    setMessage("");
    try {
      const updated = await api.updateMe(token, form);
      replaceCustomer(updated);
      setMessage("Đã lưu hồ sơ.");
    } catch (err) {
      setError(errorText(err));
    }
  }

  return (
    <section className="narrow">
      <form onSubmit={submit}>
        <h1>Hồ sơ</h1>
        <p className="muted">{customer?.email}</p>
        <label>Họ tên<input required minLength={2} value={form.fullName} onChange={set("fullName")} /></label>
        <label>Số điện thoại<input required pattern="^(0|\+84)(3|5|7|8|9)[0-9]{8}$" value={form.phone} onChange={set("phone")} /></label>
        <label>Địa chỉ<textarea value={form.address} onChange={set("address")} /></label>
        {error ? <p className="alert">{error}</p> : null}
        {message ? <p className="ok">{message}</p> : null}
        <button className="button" type="submit">Lưu</button>
      </form>
    </section>
  );
}

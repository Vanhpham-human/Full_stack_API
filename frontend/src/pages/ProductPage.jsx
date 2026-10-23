import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { api } from "../api";
import { useAuth } from "../auth";
import { useCart } from "../cart";
import { ProductImage } from "../components/ProductImage";
import { errorText, formatVnd } from "../format";

export function ProductPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { customer } = useAuth();
  const cart = useCart();
  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [error, setError] = useState("");

  useEffect(() => {
    setProduct(null);
    api.product(id).then(setProduct).catch((err) => setError(errorText(err)));
  }, [id]);

  async function add() {
    setError("");
    if (!customer) {
      navigate("/login", { state: { from: `/products/${id}` } });
      return;
    }
    try {
      await cart.add(Number(id), quantity);
    } catch (err) {
      setError(errorText(err));
    }
  }

  if (error && !product) return <p className="alert">{error}</p>;
  if (!product) return <p className="muted">Đang tải...</p>;

  return (
    <article className="detail">
      <div className="media large"><ProductImage src={product.imageUrl} name={product.name} /></div>
      <div>
        <Link to={`/?categoryId=${product.categoryId}`} className="eyebrow">{product.categoryName}</Link>
        <h1>{product.name}</h1>
        <p className="price">{formatVnd(product.price)}</p>
        <p className={product.stockQuantity === 0 ? "stock out" : "stock"}>
          {product.stockQuantity === 0 ? "Hết hàng" : `Còn ${product.stockQuantity} trong kho`}
        </p>
        <p className="prose">{product.description}</p>
        <div className="buy">
          <label>
            Số lượng
            <input
              type="number"
              min="1"
              max="99"
              value={quantity}
              onChange={(event) => setQuantity(Math.max(1, Math.min(99, Number(event.target.value) || 1)))}
            />
          </label>
          <button type="button" className="button" disabled={product.stockQuantity === 0} onClick={add}>
            Thêm vào giỏ
          </button>
        </div>
        {error ? <p className="alert">{error}</p> : null}
      </div>
    </article>
  );
}

import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { api } from "../api";
import { ProductImage } from "../components/ProductImage";
import { errorText, formatVnd } from "../format";

export function StorePage() {
  const [params, setParams] = useSearchParams();
  const [categories, setCategories] = useState([]);
  const [pageData, setPageData] = useState(null);
  const [error, setError] = useState("");
  const [draft, setDraft] = useState({
    q: params.get("q") || "",
    minPrice: params.get("minPrice") || "",
    maxPrice: params.get("maxPrice") || "",
  });

  const categoryId = params.get("categoryId") || "";
  const page = Number(params.get("page") || 0);
  const sort = params.get("sort") || "newest";

  useEffect(() => {
    api.categories().then(setCategories).catch((err) => setError(errorText(err)));
  }, []);

  useEffect(() => {
    setDraft({
      q: params.get("q") || "",
      minPrice: params.get("minPrice") || "",
      maxPrice: params.get("maxPrice") || "",
    });
    let ignore = false;
    api.products({
      q: params.get("q") || undefined,
      categoryId: categoryId || undefined,
      minPrice: params.get("minPrice") || undefined,
      maxPrice: params.get("maxPrice") || undefined,
      page,
      size: 12,
      sort,
    }).then((data) => {
      if (!ignore) setPageData(data);
    }).catch((err) => {
      if (!ignore) setError(errorText(err));
    });
    return () => {
      ignore = true;
    };
  }, [params]);

  function update(next) {
    const merged = new URLSearchParams(params);
    Object.entries(next).forEach(([key, value]) => {
      if (value === "" || value === null || value === undefined) merged.delete(key);
      else merged.set(key, String(value));
    });
    if (!("page" in next)) merged.delete("page");
    setParams(merged);
  }

  const showIntro = !params.get("q") && !categoryId && !params.get("minPrice") && page === 0;

  return (
    <div className="shop">
      {showIntro ? (
        <section className="hero">
          <p className="eyebrow">Cửa hàng đồ dùng nhà</p>
          <h1>Đồ dùng tử tế cho nhà của bạn</h1>
          <p>Gốm, gỗ, vải lanh và đồ bếp được chọn chậm. Giá bằng đồng, tồn kho cập nhật ngay khi đặt hàng.</p>
        </section>
      ) : <h1 className="page-title">Cửa hàng</h1>}

      <div className="catalog">
        <aside>
          <button type="button" className={!categoryId ? "chip on" : "chip"} onClick={() => update({ categoryId: "" })}>
            Tất cả
          </button>
          {categories.map((category) => (
            <button
              key={category.id}
              type="button"
              className={String(category.id) === categoryId ? "chip on" : "chip"}
              onClick={() => update({ categoryId: category.id })}
            >
              {category.name}
            </button>
          ))}
        </aside>
        <section>
          <form
            className="filters"
            onSubmit={(event) => {
              event.preventDefault();
              update({ q: draft.q.trim(), minPrice: draft.minPrice, maxPrice: draft.maxPrice });
            }}
          >
            <label>
              Tìm kiếm
              <input value={draft.q} onChange={(event) => setDraft({ ...draft, q: event.target.value })} placeholder="Tên sản phẩm" />
            </label>
            <label>
              Giá từ
              <input inputMode="numeric" value={draft.minPrice} onChange={(event) => setDraft({ ...draft, minPrice: event.target.value })} />
            </label>
            <label>
              Đến
              <input inputMode="numeric" value={draft.maxPrice} onChange={(event) => setDraft({ ...draft, maxPrice: event.target.value })} />
            </label>
            <label>
              Sắp xếp
              <select value={sort} onChange={(event) => update({ sort: event.target.value, page })}>
                <option value="newest">Mới nhất</option>
                <option value="priceAsc">Giá tăng</option>
                <option value="priceDesc">Giá giảm</option>
                <option value="name">Tên</option>
              </select>
            </label>
            <button className="button" type="submit">Lọc</button>
          </form>
          {error ? <p className="alert">{error}</p> : null}
          {!pageData ? <p className="muted">Đang tải sản phẩm...</p> : null}
          {pageData && pageData.content.length === 0 ? <p className="empty">Không có sản phẩm phù hợp.</p> : null}
          <div className="grid">
            {pageData?.content.map((product) => (
              <Link key={product.id} to={`/products/${product.id}`} className="card">
                <div className="media"><ProductImage src={product.imageUrl} name={product.name} /></div>
                <p className="eyebrow">{product.categoryName}</p>
                <h2>{product.name}</h2>
                <div className="card-foot">
                  <strong>{formatVnd(product.price)}</strong>
                  <span className={product.stockQuantity === 0 ? "stock out" : "stock"}>
                    {product.stockQuantity === 0 ? "Hết hàng" : `Còn ${product.stockQuantity}`}
                  </span>
                </div>
              </Link>
            ))}
          </div>
          {pageData && pageData.totalPages > 1 ? (
            <div className="pager">
              <button type="button" disabled={page <= 0} onClick={() => update({ page: page - 1 })}>Trước</button>
              <span>Trang {page + 1} / {pageData.totalPages}</span>
              <button type="button" disabled={page + 1 >= pageData.totalPages} onClick={() => update({ page: page + 1 })}>Sau</button>
            </div>
          ) : null}
        </section>
      </div>
    </div>
  );
}

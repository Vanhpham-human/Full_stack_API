import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { api } from "./api";
import { useAuth } from "./auth";

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const { token, customer } = useAuth();
  const [cart, setCart] = useState(null);
  const [notice, setNotice] = useState(null);

  function flash(type, text) {
    setNotice({ type, text });
    window.clearTimeout(flash.timer);
    flash.timer = window.setTimeout(() => setNotice(null), 3400);
  }

  async function refresh() {
    if (!token) {
      setCart(null);
      return;
    }
    setCart(await api.cart(token));
  }

  useEffect(() => {
    refresh().catch(() => setCart(null));
  }, [token, customer?.id]);

  const value = useMemo(() => ({
    cart,
    notice,
    clearNotice: () => setNotice(null),
    count: cart?.items?.reduce((sum, item) => sum + item.quantity, 0) || 0,
    flash,
    async add(productId, quantity) {
      const next = await api.addCartItem(token, { productId, quantity });
      setCart(next);
      flash("ok", "Đã thêm vào giỏ");
    },
    async update(itemId, quantity) {
      setCart(await api.updateCartItem(token, itemId, quantity));
    },
    async remove(itemId) {
      await api.removeCartItem(token, itemId);
      await refresh();
    },
    refresh,
  }), [cart, notice, token]);

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  return useContext(CartContext);
}

import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { api } from "./api";

const KEY = "annam.session";
const AuthContext = createContext(null);

function readSession() {
  try {
    return JSON.parse(localStorage.getItem(KEY) || "null");
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [session, setSession] = useState(readSession);
  const [ready, setReady] = useState(false);

  function persist(next) {
    setSession(next);
    if (next) localStorage.setItem(KEY, JSON.stringify(next));
    else localStorage.removeItem(KEY);
  }

  useEffect(() => {
    let ignore = false;
    async function hydrate() {
      const current = readSession();
      if (!current?.token) {
        if (!ignore) setReady(true);
        return;
      }
      try {
        const me = await api.me(current.token);
        if (!ignore) persist({ token: current.token, customer: me });
      } catch {
        if (!ignore) persist(null);
      } finally {
        if (!ignore) setReady(true);
      }
    }
    hydrate();
    function onUnauthorized() {
      persist(null);
    }
    window.addEventListener("annam:unauthorized", onUnauthorized);
    return () => {
      ignore = true;
      window.removeEventListener("annam:unauthorized", onUnauthorized);
    };
  }, []);

  const value = useMemo(() => ({
    ready,
    token: session?.token || "",
    customer: session?.customer || null,
    async login(email, password) {
      const result = await api.login({ email, password });
      persist({ token: result.accessToken, customer: result.customer });
      return result.customer;
    },
    async logout() {
      const token = session?.token;
      persist(null);
      if (token) {
        try {
          await api.logout(token);
        } catch {
          /* phiên đã xóa ở trình duyệt */
        }
      }
    },
    replaceCustomer(customer) {
      if (!session?.token) return;
      persist({ token: session.token, customer });
    },
  }), [ready, session]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}

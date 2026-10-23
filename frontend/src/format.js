export function formatVnd(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(value ?? 0);
}

export function formatDate(value) {
  if (!value) return "";
  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

export const STATUS_LABEL = {
  PENDING: "Chờ xử lý",
  PAID: "Đã thanh toán",
  SHIPPED: "Đã giao",
  CANCELLED: "Đã hủy",
};

export function errorText(error) {
  if (!error) return "";
  const details = (error.details || []).map((item) => item.issue).filter(Boolean);
  return details.length ? `${error.message}. ${details.join(". ")}` : error.message;
}

import { useState } from "react";

export function ProductImage({ src, name }) {
  const [failed, setFailed] = useState(!src);
  if (failed) {
    return (
      <div className="ph" aria-hidden="true">
        <span>{name}</span>
      </div>
    );
  }
  return <img src={src} alt={name} onError={() => setFailed(true)} />;
}

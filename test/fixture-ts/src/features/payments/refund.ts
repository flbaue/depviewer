import { pay } from "../../services/checkout";
import { Cart } from "../../model/cart";
import { formatMoney } from "@util/money"; // tsconfig path alias

export function refund(cart: Cart): Promise<boolean> {
  return pay(cart);
}

// Not exported: still a top-level element of its file.
function refundNote(cart: Cart): string {
  return formatMoney(cart.total());
}

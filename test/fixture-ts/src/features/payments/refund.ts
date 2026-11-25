import { pay } from "../../services/checkout";
import { Cart } from "../../model/cart";

export function refund(cart: Cart): Promise<boolean> {
  return pay(cart);
}

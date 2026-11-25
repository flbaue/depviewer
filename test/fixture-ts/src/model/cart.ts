import { Product } from "./product";
import { formatMoney } from "../util/money";

export class Cart {
  private items = new Map<string, Product>();

  add(product: Product): void {
    this.items.set(product.id, product);
  }

  total(): number {
    let sum = 0;
    for (const p of this.items.values()) sum += p.price();
    return sum;
  }

  describe(): string {
    return formatMoney(this.total()); // model -> util
  }
}

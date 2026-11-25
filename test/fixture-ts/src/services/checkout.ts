import { Emitter } from "@acme/events";
import { Cart } from "../model/cart";
import { formatMoney, roundTo } from "../util"; // through util/index.ts
import Logger from "./logger"; // default export

const events = new Emitter();

export namespace Checkout {
  export function receipt(cart: Cart): string {
    const total = roundTo(cart.total(), 2);
    new Logger().info(`total ${total}`);
    events.emit("receipt");
    return formatMoney(total);
  }
}

export async function pay(cart: Cart): Promise<boolean> {
  return cart.total() > 0;
}

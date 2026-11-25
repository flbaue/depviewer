import type { Cart } from "../model/cart"; // util -> model: a deliberate folder cycle

export const CURRENCY = "EUR";

export function formatMoney(amount: number): string {
  return `${amount.toFixed(2)} ${CURRENCY}`;
}

export const isEmpty = (cart: Cart): boolean => cart.total() === 0;

// Imports the sibling workspace package by its name, as npm workspaces link it into node_modules.
import { Price } from "@acme/core";

export class Cart {
  total: Price = new Price(0);

  add(price: Price): void {
    this.total = this.total.add(price);
  }
}

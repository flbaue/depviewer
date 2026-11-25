import { Cart } from "../model/cart";
import { Category, Product } from "../model/product";

const cart = new Cart();
cart.add(new Product("p1", 10, Category.Food));
if (cart.total() !== 10) throw new Error("wrong total");

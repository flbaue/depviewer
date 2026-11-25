export interface Priced {
  price(): number;
}

export enum Category {
  Food = "food",
  Books = "books",
}

export type ProductId = string;

export class Product implements Priced {
  static count = 0;
  #secret = 1;
  private readonly tags: string[] = [];

  constructor(public readonly id: ProductId, private readonly base: number, readonly category: Category) {
    Product.count++;
  }

  price(): number {
    return this.base * (this.category === Category.Books ? 0.93 : 1) + this.#secret - 1;
  }
}

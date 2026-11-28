export class Price {
  constructor(readonly cents: number) {}

  add(other: Price): Price {
    return new Price(this.cents + other.cents);
  }
}

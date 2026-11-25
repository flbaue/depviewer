part 'product_codes.dart';

abstract interface class Priced {
  double price();
}

enum Category {
  food,
  books;

  double get discount => this == Category.books ? 0.07 : 0.0;
}

typedef ProductId = String;

class Product implements Priced {
  static int count = 0;
  final ProductId id;
  final double _base;
  final Category category;

  Product(this.id, this._base, this.category) {
    count++;
  }

  Product.free(ProductId id) : this(id, 0, Category.food);

  factory Product.parse(String code) => Product(code, codeValue(code), Category.books);

  @override
  double price() => _base * (1 - category.discount);
}

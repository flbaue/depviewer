import '../util/money.dart';
import 'product.dart';

class Cart with Describable {
  final Map<ProductId, Product> _items = {};

  void add(Product product) {
    _items[product.id] = product;
  }

  double total() => _items.values.fold(0.0, (sum, p) => sum + p.price());

  @override
  String describe() => formatMoney(total()); // models -> util
}

mixin Describable {
  String describe();
}

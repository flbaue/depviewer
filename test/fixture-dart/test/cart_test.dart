import 'package:shop_app/shop_app.dart';

void main() {
  final cart = Cart();
  cart.add(Product.free('p1'));
  assert(cart.total() == 0);
}

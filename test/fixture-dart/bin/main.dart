import 'package:shop_app/shop_app.dart';
import 'package:shop_app/src/services/checkout.dart';

Future<void> main() async {
  final cart = Cart()..add(Product.parse('abc'));
  print(await Checkout().receipt(cart));
}

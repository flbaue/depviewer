import '../models/cart.dart'; // util -> models: a deliberate folder cycle
import 'package:shop_core/currency.dart'; // another package of the workspace

const currency = 'EUR';

String formatMoney(double amount) => '${Currency.eur.format(amount)} ($currency)';

extension CartChecks on Cart {
  bool get isEmpty => total() == 0;
}

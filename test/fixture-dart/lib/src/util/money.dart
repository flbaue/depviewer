import '../models/cart.dart'; // util -> models: a deliberate folder cycle

const currency = 'EUR';

String formatMoney(double amount) => '${amount.toStringAsFixed(2)} $currency';

extension CartChecks on Cart {
  bool get isEmpty => total() == 0;
}

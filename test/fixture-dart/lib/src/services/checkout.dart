import 'dart:async';

import 'package:shop_app/src/models/cart.dart';
import 'package:shop_app/src/util/money.dart';

class Checkout {
  final StreamController<String> _events = StreamController<String>();

  Future<String> receipt(Cart cart) async {
    if (cart.isEmpty) return 'empty';
    _events.add('receipt');
    return formatMoney(cart.total());
  }
}

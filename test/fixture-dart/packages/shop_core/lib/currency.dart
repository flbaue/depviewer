class Currency {
  final String code;
  const Currency(this.code);

  static const eur = Currency('EUR');

  String format(double value) => '${value.toStringAsFixed(2)} $code';
}

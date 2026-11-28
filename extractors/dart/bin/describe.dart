// --describe of the depviewer Dart extractor (spec/wiki/functional-requirements/FR-010-choose-the-toolchain-in-a-fixed-order.md).
//
// Usage: dart --packages=<extractor>/describe_packages.json describe.dart [<source>]
// Prints the extractor's name, version and contract and whether the Dart SDK running it is
// supported. It imports nothing, so an SDK too old for the analyzer can still run it and be told so.
// The core runs it with describe_packages.json, an empty package config: as part of no package, it
// gets the SDK's own language version instead of the package's minimum, which an old SDK rejects.

import 'dart:convert';
import 'dart:io';

/// The extractor's own version. Raise it when the fragment it prints changes.
const extractorVersion = '1.0.0';

/// Dart SDKs the pinned analyzer runs on, as pubspec.lock records them (`sdks: dart`).
const sdkMin = [3, 11, 0], sdkBelow = [4, 0, 0], sdkRange = '>=3.11.0 <4.0.0';

Map<String, Object?> describe(String source) {
  final m = RegExp(r'^(\d+)\.(\d+)\.(\d+)').firstMatch(Platform.version);
  final v = m == null ? null : [int.parse(m[1]!), int.parse(m[2]!), int.parse(m[3]!)];
  int cmp(List<int> a, List<int> b) => a[0] != b[0] ? a[0] - b[0] : a[1] != b[1] ? a[1] - b[1] : a[2] - b[2];
  final supported = v != null && cmp(v, sdkMin) >= 0 && cmp(v, sdkBelow) < 0;
  return {
    'name': 'depviewer-dart-extractor',
    'version': extractorVersion,
    'contract': 1,
    'languages': ['dart'],
    'exact': true,
    'toolchain': {
      'tool': 'dart',
      'version': v?.join('.'),
      'source': source,
      'path': Platform.resolvedExecutable,
      'supported': supported,
      'range': sdkRange,
      if (!supported) 'problem': 'version not supported',
    },
  };
}

void main(List<String> args) => stdout.writeln(jsonEncode(describe(args.isNotEmpty ? args.first : 'path')));

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:app_usage/app_usage.dart';

void main() {
  const MethodChannel channel = MethodChannel('app_usage');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

}

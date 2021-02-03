
import 'dart:async';

import 'package:flutter/services.dart';

class AppUsage {
  static const MethodChannel _channel =
      const MethodChannel('app_usage');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<List<Map<String, Object>>> getAppUsages(List<String> packageNames, int beginTime, int endTime) async {
    try {
      final List<dynamic> results = await _channel.invokeMethod<List<dynamic>>('getAppUsages', {
        "packageNames": packageNames,
        "beginTime": beginTime,
        "endTime": endTime,
      });
      if (results == null) return null;
      final List<Map<String, Object>> appUsages = results.cast<Map<dynamic, dynamic>>().map((result) => result.cast<String, Object>()).toList();
      return appUsages;
    } on PlatformException {
      return null;
    }
  }

}

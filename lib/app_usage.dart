
import 'dart:async';

import 'package:flutter/services.dart';

class AppUsage {
  static const MethodChannel _channel =
      const MethodChannel('app_usage');

  static Future<List<Map<String, dynamic>>> getAppUsages(List<String> packageNames, int beginTime, int endTime) async {
    List<Map<String, dynamic>> appUsages = [];
    try {
      var res = await _channel.invokeMethod<List<dynamic>>('getAppUsages', {
        "packageNames": packageNames,
        "beginTime": beginTime,
        "endTime": endTime,
      });
      if (res != null) {
        appUsages = res.cast<Map<dynamic, dynamic>>().map((result) => result.cast<String, dynamic>()).toList();
      }
      return appUsages;
    } on PlatformException {
      return appUsages;
    }
  }

}

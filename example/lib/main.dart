import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:app_usage/app_usage.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  List<Map<String, Object>> _appUsages = [];

  @override
  void initState() {
    super.initState();
    initPlatformState();
    _getAppsAccessTime();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await AppUsage.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  Future<void> _getAppsAccessTime() async {
    var packageNames = ["com.google.android.googlequicksearchbox", "com.android.chrome"];
    var endTime = DateTime.now().millisecondsSinceEpoch;
    var beginTime = endTime - 24 * 60 * 60 * 1000;
    List<Map<String, Object>> appUsages;
    try {
      final List<dynamic> results = await AppUsage.getAppUsages(packageNames, beginTime, endTime);
      appUsages = results.cast<Map<dynamic, dynamic>>().map((result) => result.cast<String, Object>()).toList();
    } on PlatformException {
      appUsages = [];
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _appUsages = appUsages;
    });
  }

  String _convertTime(int seconds) {
    int day = seconds ~/ 86400;
    int hour = (seconds - day * 86400) ~/ 3600;
    int minute = (seconds - day * 86400 - hour * 3600) ~/ 60;
    int second = seconds - day * 86400 - hour * 3600 - minute * 60;
    String converted = "";
    if (day > 0) converted += "$day""d";
    if (hour > 0) converted += "$hour""h";
    if (minute > 0) converted += "$minute""m";
    if (second > 0) converted += "$second""s";
    return converted;
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('App usage example'),
        ),
        body: Center(
          // child: Text('Running on: $_platformVersion\n'),
          child: ListView.builder(
            itemCount: _appUsages.length,
            itemBuilder: (BuildContext context, int index) {
              String converted = _convertTime(_appUsages[index]['openTime']);
              int openCount = _appUsages[index]['openCount'];
              return ListTile(
                title: Text(
                  _appUsages[index]['packageName']
                ),
                subtitle: Text(
                  "Usage time: $converted\nNumber of use: $openCount"
                ),
              );
            },
          ),
        ),
      ),
    );
  }
}

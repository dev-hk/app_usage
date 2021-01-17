# app_usage

A flutter plugin for getting the usage information of Android applications.

## Getting Started

After importing this plugin to your project as usual, add the following to your `AndroidManifest.xml` within the `<manifest></manifest>`tags:
```
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" tools:ignore="ProtectedPermissions" />
```

Next, you have to import the package in your dart file with:
```
import 'package:app_usage/app_usage.dart';
```

## Get application usages
You can get the usage information of applications as below.
This process may takes a little long time if you pass bigger size of packageNames or longer term.
```
var packageNames = ["com.android.chrome", "com.google.android.googlequicksearchbox"];
var endTime = DateTime.now().millisecondsSinceEpoch;
var beginTime = endTime - 24 * 60 * 60 * 1000;
// Returns a list of only those app usages that have used between beginTime and endTime
List<Map<String, Object>> appUsages;
    try {
      final List<dynamic> results = await AppUsage.getAppUsages(packageNames, beginTime, endTime);
      appUsages = results.cast<Map<dynamic, dynamic>>().map((result) => result.cast<String, Object>()).toList();
      for (Map<String, Object> appUsage in appUsages) {
            print(appUsage['packageName']); // ex) com.android.chrome
            print(appUsage['openTime']); // ex) 241 (sec)
            print(appUsage['openCount']); // ex) 10
        }
    } on PlatformException {
      appUsages = [];
    }
```

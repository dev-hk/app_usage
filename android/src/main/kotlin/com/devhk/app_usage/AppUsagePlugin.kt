package com.devhk.app_usage

import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Context.USAGE_STATS_SERVICE
import android.os.Build
import android.os.Process
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** AppUsagePlugin */
class AppUsagePlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel

  private lateinit var context : Context

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "app_usage")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getAppUsages") {
      val packageNames = call.argument<List<String>>("packageNames")
      val beginTime = call.argument<Long>("beginTime")
      val endTime = call.argument<Long>("endTime")
      val timeSpent = getTimeSpent(packageNames, beginTime, endTime)
      result.success(timeSpent)
    } else {
      result.notImplemented()
    }
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
  private fun getTimeSpent(@NonNull packageNames: List<String>?, @NonNull beginTime: Long?, @NonNull endTime: Long?): List<Map<String, Any>>? {
    if (!checkForPermission(context)) {
      return null
    } else {
      var appUsageMaps: MutableList<MutableMap<String, Any>> = mutableListOf()
      val usageStatsManager = context.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager

      if (packageNames != null && beginTime != null && endTime != null) {
        val usageEvents: UsageEvents = usageStatsManager.queryEvents(beginTime, endTime)
        val allEvents: MutableList<UsageEvents.Event> = mutableListOf()
        while (usageEvents.hasNextEvent()) {
          val currentEvent = UsageEvents.Event()
          usageEvents.getNextEvent(currentEvent)
          if (currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED || currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
            allEvents.add(currentEvent)
          }
        }
        appUsageMaps = getOpenTime(allEvents, packageNames)
      }
      return appUsageMaps
    }
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
  private fun checkForPermission(context: Context): Boolean {
    val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOpsManager.checkOpNoThrow(OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    return mode == MODE_ALLOWED
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
  private fun getOpenTime(events: MutableList<UsageEvents.Event>, packageNames: List<String>) : MutableList<MutableMap<String, Any>> {
    val appUsageMaps: MutableList<MutableMap<String, Any>> = mutableListOf()
    for (i in 0 until events.size - 1) {
      val e0 = events[i]
      val e1 = events[i + 1]
      if (e0.eventType == UsageEvents.Event.ACTIVITY_RESUMED
              && e1.eventType == UsageEvents.Event.ACTIVITY_PAUSED
              && e0.className == e1.className
              && packageNames.contains(e0.packageName)) {
                if (!appUsageMaps.any { it["packageName"] == e0.packageName }) {
                  appUsageMaps.add(mutableMapOf("packageName" to e0.packageName, "openTime" to 0, "openCount" to 0))
                }
        var diff: Int = (e1.timeStamp - e0.timeStamp).toInt()
        diff /= 1000
        appUsageMaps.forEach {
          if (it["packageName"] == e0.packageName && diff > 0) {
            it["openTime"] = it["openTime"] as Int + diff
            it["openCount"] = it["openCount"] as Int + 1
          }
        }
      }
    }
    return appUsageMaps
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

}

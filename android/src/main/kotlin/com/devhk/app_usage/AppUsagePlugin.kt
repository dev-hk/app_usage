package com.devhk.app_usage

import android.app.Activity
import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Context.USAGE_STATS_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** AppUsagePlugin */
class AppUsagePlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel

  private lateinit var context : Context
  private lateinit var activity : Activity

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "app_usage")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  private fun getTimeSpent(@NonNull packageNames: List<String>?, @NonNull beginTime: Long?, @NonNull endTime: Long?): List<Map<String, Any>>? {
    if (!checkForPermission(context)) {
      activity.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
      return null
    } else {
      val appUsageMaps: MutableList<Map<String, Any>> = mutableListOf()
      val usageStatsManager = context.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager

      if (packageNames != null && beginTime != null && endTime != null) {
        for (packageName in packageNames) {
          val usageEvents: UsageEvents = usageStatsManager.queryEvents(beginTime, endTime)
          val allEvents: MutableList<UsageEvents.Event> = mutableListOf()
          while (usageEvents.hasNextEvent()) {
            val currentEvent = UsageEvents.Event()
            usageEvents.getNextEvent(currentEvent)
            if (currentEvent.packageName == packageName) {
              if (currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED || currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                allEvents.add(currentEvent)
              }
            }
          }

          if (allEvents.size > 0) {
            appUsageMaps.add(getOpenTime(allEvents, packageName))
          }
        }
      }
      return appUsageMaps
    }
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  private fun checkForPermission(context: Context): Boolean {
    val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOpsManager.checkOpNoThrow(OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    return mode == MODE_ALLOWED
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  private fun getOpenTime(events: MutableList<UsageEvents.Event>, packageName: String) : Map<String, Any> {
    var openTime = 0
    var openCount = 0
    for (i in 0 until events.size - 1) {
      val e0 = events[i]
      val e1 = events[i + 1]
      if (e0.eventType == UsageEvents.Event.ACTIVITY_RESUMED
              && e1.eventType == UsageEvents.Event.ACTIVITY_PAUSED
              && e0.className == e1.className) {
        var diff: Int = (e1.timeStamp - e0.timeStamp).toInt()
        diff /= 1000
        openTime += diff
        openCount ++
      }
    }
    return mutableMapOf("packageName" to packageName, "openTime" to openTime, "openCount" to openCount)
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onDetachedFromActivity() {
    TODO("Not yet implemented")
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    TODO("Not yet implemented")
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
    TODO("Not yet implemented")
  }
}

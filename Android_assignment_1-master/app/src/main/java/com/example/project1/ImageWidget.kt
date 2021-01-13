package com.example.project1

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews


/**
 * Implementation of App Widget functionality.
 */
class ImageWidget : AppWidgetProvider() {
    private val LOGO1 = "logo1"
    private val LOGO2 = "logo2"
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val remoteViews = RemoteViews(context.packageName, R.layout.image_widget)
        remoteViews.setOnClickPendingIntent(R.id.button6, getPendingSelfIntent(context, LOGO1))
        remoteViews.setOnClickPendingIntent(R.id.button7, getPendingSelfIntent(context, LOGO2))

        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    private fun onUpdate(context: Context, remoteViews: RemoteViews) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisAppWidgetComponentName = ComponentName(context.packageName, javaClass.name)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName)

        for (appWidgetId in appWidgetIds) {
            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, remoteViews)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val remoteViews = RemoteViews(context.packageName, R.layout.image_widget)
        if (intent.action == LOGO1) {
            remoteViews.setImageViewResource(R.id.imageView, R.drawable.logo1)
        } else if (intent.action == LOGO2) {
            remoteViews.setImageViewResource(R.id.imageView, R.drawable.logo2)
        }
        onUpdate(context, remoteViews)
    }

    protected fun getPendingSelfIntent(context: Context?, action: String?): PendingIntent? {
        val intent = Intent(context, javaClass)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}


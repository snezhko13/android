package com.example.project1

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [WebWidgetConfigureActivity]
 */
class WebWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            startBrowsing(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deleteTitlePref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

private fun startBrowsing(ctx: Context, appWidgetManager: AppWidgetManager, widgetID: Int) {
    val widgetView = RemoteViews(ctx.packageName, R.layout.web_widget)
    val widgetText = loadTitlePref(ctx, widgetID)
    val uri: Uri = Uri.parse(widgetText)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    val pIntent = PendingIntent.getActivity(ctx, widgetID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    widgetView.setOnClickPendingIntent(R.id.button4, pIntent)
    appWidgetManager.updateAppWidget(widgetID, widgetView)
}
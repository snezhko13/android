package com.example.project1

import android.app.Activity
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RemoteViews

/**
 * The configuration screen for the [WebWidget] AppWidget.
 */
class WebWidgetConfigureActivity : Activity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var appWidgetText: EditText
    private var onClickListener = View.OnClickListener {
        val context = this@WebWidgetConfigureActivity
        val widgetText = appWidgetText.text.toString()
        saveTitlePref(context, appWidgetId, widgetText)
        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
        RemoteViews(context.packageName, R.layout.web_widget).also { views ->
            val widgetView = RemoteViews(context.packageName, R.layout.web_widget)
            val uri: Uri = Uri.parse(widgetText)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            val pIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            widgetView.setOnClickPendingIntent(R.id.button4, pIntent)
            appWidgetManager.updateAppWidget(appWidgetId, widgetView)
        }
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultValue)
        finish()
    }

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.web_widget_configure)
        appWidgetText = findViewById<View>(R.id.appwidget_text) as EditText
        findViewById<View>(R.id.add_button).setOnClickListener(onClickListener)

        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        appWidgetText.setText(loadTitlePref(this@WebWidgetConfigureActivity, appWidgetId))
    }

}

private const val PREFS_NAME = "com.example.project1.NewAppWidget"
private const val PREF_PREFIX_KEY = "appwidget_"

// Write the prefix to the SharedPreferences object for this widget
internal fun saveTitlePref(context: Context, appWidgetId: Int, text: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
    prefs.apply()
}

// Read the prefix from the SharedPreferences object for this widget.
// If there is no preference saved, get the default from a resource
internal fun loadTitlePref(context: Context, appWidgetId: Int): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
    Log.d("xxxZ", titleValue.toString())
    return titleValue ?: context.getString(R.string.webwidget_url)
}

internal fun deleteTitlePref(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.remove(PREF_PREFIX_KEY + appWidgetId)
    prefs.apply()
}
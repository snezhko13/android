package com.example.project1

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.example.project1.models.Shop
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private var context: Context? = null
    private lateinit var databaseRef: DatabaseReference


    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        this.context = context
        if (geofencingEvent.hasError()) {
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
            Log.d("xxx",geofenceTransition.toString())
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            getGeofenceTransitionDetails(geofenceTransition, triggeringGeofences)
        }
    }

    private fun getGeofenceTransitionDetails(geofenceTransition: Int, triggeringGeofences: List<Geofence>) {
        if (!::databaseRef.isInitialized) {
            databaseRef = FirebaseDatabase.getInstance()
                .getReference("users/" + FirebaseAuth.getInstance().currentUser!!.uid + "/shops")
        }
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val p = dataSnapshot.children.mapNotNull { child -> child.getValue(Shop::class.java) }
                val map = HashMap<String, String>()
                p.forEach {
                    map[it.id] = it.title.orEmpty()
                }
                val geofenceTransitionString = getTransitionString(geofenceTransition)
                val triggeringGeofencesIdsList: ArrayList<String?> = ArrayList()
                for (geofence in triggeringGeofences) {
                    triggeringGeofencesIdsList.add(map[geofence.requestId])
                }
                val triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList)
                sendNotification("$geofenceTransitionString $triggeringGeofencesIdsString")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("DATABASE", "Failed to read value.", error.toException())
            }
        })
    }

    private fun sendNotification(notificationDetails: String) {
        val mNotificationManager = context!!.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val name: CharSequence = context!!.getString(R.string.app_name)
        val mChannel = NotificationChannel("channel_01", name, NotificationManager.IMPORTANCE_DEFAULT)
        mNotificationManager.createNotificationChannel(mChannel)
        val notificationIntent = Intent(context, MainActivity::class.java)
        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(context!!)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(notificationIntent)
        val notificationPendingIntent: PendingIntent? =
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context)
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(BitmapFactory.decodeResource(context!!.resources, R.drawable.ic_launcher_foreground))
            .setColor(Color.RED)
            .setContentTitle(notificationDetails)
            .setContentText("Miłego dnia!")
            .setContentIntent(notificationPendingIntent)
        builder.setChannelId("channel_01") // Channel ID
        builder.setAutoCancel(true)
        mNotificationManager.notify(0, builder.build())
    }

    private fun getTransitionString(transitionType: Int): String {
        return when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Jesteś w: "
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Opuściłeś: "
            else -> "Błąd"
        }
    }
}

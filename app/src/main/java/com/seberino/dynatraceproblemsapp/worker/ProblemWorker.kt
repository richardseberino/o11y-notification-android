package com.seberino.dynatraceproblemsapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.seberino.dynatraceproblemsapp.MainActivity
import com.seberino.dynatraceproblemsapp.data.Graph
import com.seberino.dynatraceproblemsapp.data.model.DynatraceInstance
import kotlinx.coroutines.flow.first

class ProblemWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): androidx.work.ListenableWorker.Result {
        val repository = Graph.repository
        val instances = repository.allInstances.first()

        for (instance in instances) {
            if (!instance.notificationsEnabled) continue

            val response = repository.getProblemsForInstance(instance)
            val latestProblem = response.problems.firstOrNull()

            if (latestProblem != null && latestProblem.displayId != instance.lastSeenProblemId) {
                // New problem detected
                sendNotification(instance.name, latestProblem.title)
                
                // Update last seen problem
                repository.updateInstance(instance.copy(lastSeenProblemId = latestProblem.displayId))
            }
        }

        return androidx.work.ListenableWorker.Result.success()
    }

    private fun sendNotification(instanceName: String, problemTitle: String) {
        val channelId = "problem_notifications"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Problem Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("New Problem in $instanceName")
            .setContentText(problemTitle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

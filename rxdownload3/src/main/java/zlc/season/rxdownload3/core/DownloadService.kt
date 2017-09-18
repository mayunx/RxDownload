package zlc.season.rxdownload3.core

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import java.io.File


class DownloadService : Service() {
    private val missionBox = LocalMissionBox()
    private val binder = DownloadBinder()
    lateinit var notificationManager: NotificationManager

    private val enableForeService = DownloadConfig.enableForegroundService
    private val foreServiceNotificationFactory = DownloadConfig.foreServiceNotificationFactory

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (enableForeService) {
            startForeground(Int.MAX_VALUE, foreServiceNotificationFactory.build(this))
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        stopForeground(true)
        missionBox.stopAll()
        super.onDestroy()
    }

    inner class DownloadBinder : Binder() {
        fun create(callback: StatusCallback, mission: Mission) {
            missionBox.create(mission).subscribe({
                callback.apply(it)
            })
        }

        fun start(mission: Mission) {
            missionBox.start(mission).subscribe()
        }

        fun stop(mission: Mission) {
            missionBox.stop(mission).subscribe()
        }

        fun startAll() {
            missionBox.startAll().subscribe()
        }

        fun stopAll() {
            missionBox.stopAll().subscribe()
        }

        fun getFile(fileCallback: FileCallback, mission: Mission) {
            missionBox.getFile(mission).subscribe({
                fileCallback.apply(it)
            })
        }
    }


    interface StatusCallback {
        fun apply(status: Status)
    }

    interface FileCallback {
        fun apply(file: File)
    }
}
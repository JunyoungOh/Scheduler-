package com.cutener.raising.game.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 기기 부팅 시 WorkManager 스케줄링을 다시 설정
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            PetCareWorker.schedule(context)
        }
    }
}

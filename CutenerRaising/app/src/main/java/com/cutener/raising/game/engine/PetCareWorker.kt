package com.cutener.raising.game.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.cutener.raising.MainActivity
import com.cutener.raising.R
import com.cutener.raising.data.model.ActionEffects
import com.cutener.raising.data.model.EvolutionChecker
import com.cutener.raising.data.repository.PetRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * 백그라운드에서 펫 상태를 업데이트하는 Worker
 */
@HiltWorker
class PetCareWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val petRepository: PetRepository
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            val pet = petRepository.getActivePetOnce() ?: return Result.success()
            
            // 시간 경과 적용
            val now = System.currentTimeMillis()
            val elapsedMinutes = (now - pet.lastUpdatedAt) / (1000 * 60)
            
            if (elapsedMinutes < 5) {
                return Result.success()
            }
            
            var updatedPet = ActionEffects.applyTimePassage(pet, elapsedMinutes)
            
            // 진화 체크
            if (EvolutionChecker.canEvolve(updatedPet)) {
                updatedPet = EvolutionChecker.evolve(updatedPet)
                sendEvolutionNotification(updatedPet.name, updatedPet.growthStage.displayName)
            }
            
            // 위험 상태 알림
            if (updatedPet.isInDanger && !pet.isInDanger) {
                sendDangerNotification(updatedPet)
            }
            
            petRepository.updatePet(updatedPet)
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    private fun sendEvolutionNotification(petName: String, stageName: String) {
        createNotificationChannel()
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.star_big_on)
            .setContentTitle("🌟 진화!")
            .setContentText("$petName 이(가) ${stageName}(으)로 진화했어요!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(EVOLUTION_NOTIFICATION_ID, notification)
    }
    
    private fun sendDangerNotification(pet: com.cutener.raising.data.model.Pet) {
        createNotificationChannel()
        
        val message = when {
            pet.conditionStats.isSick -> "${pet.name}이(가) 아파요! 빨리 치료해주세요!"
            pet.conditionStats.hunger >= 80 -> "${pet.name}이(가) 배고파요! 밥을 주세요!"
            pet.conditionStats.cleanliness <= 20 -> "${pet.name}의 방이 더러워요! 청소해주세요!"
            pet.conditionStats.happiness <= 20 -> "${pet.name}이(가) 슬퍼해요! 놀아주세요!"
            else -> "${pet.name}이(가) 당신을 기다려요!"
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ ${pet.name}에게 관심이 필요해요!")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(DANGER_NOTIFICATION_ID, notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "펫 케어 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "펫의 상태 변화를 알려드립니다"
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    companion object {
        const val CHANNEL_ID = "pet_care_channel"
        const val EVOLUTION_NOTIFICATION_ID = 1001
        const val DANGER_NOTIFICATION_ID = 1002
        const val WORK_NAME = "pet_care_work"
        
        /**
         * WorkManager 스케줄링
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
            
            val workRequest = PeriodicWorkRequestBuilder<PetCareWorker>(
                15, TimeUnit.MINUTES  // 최소 15분 간격
            )
                .setConstraints(constraints)
                .setInitialDelay(5, TimeUnit.MINUTES)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
        
        /**
         * 즉시 실행 (앱 시작 시)
         */
        fun runOnce(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<PetCareWorker>()
                .build()
            
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}

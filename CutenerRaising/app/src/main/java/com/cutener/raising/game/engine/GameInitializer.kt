package com.cutener.raising.game.engine

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 게임 초기화 및 백그라운드 작업 관리
 */
@Singleton
class GameInitializer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * 앱 시작 시 호출
     */
    fun initialize() {
        // 백그라운드 펫 케어 워커 스케줄링
        PetCareWorker.schedule(context)
        
        // 즉시 한 번 실행 (오프라인 동안의 시간 경과 적용)
        PetCareWorker.runOnce(context)
    }
}

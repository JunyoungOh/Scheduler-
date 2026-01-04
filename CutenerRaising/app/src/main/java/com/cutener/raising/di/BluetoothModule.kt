package com.cutener.raising.di

import android.content.Context
import com.cutener.raising.bluetooth.NearbyConnectionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BluetoothModule {
    
    @Provides
    @Singleton
    fun provideNearbyConnectionManager(
        @ApplicationContext context: Context
    ): NearbyConnectionManager {
        return NearbyConnectionManager(context)
    }
}

package com.scheduleapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class with Hilt dependency injection
 */
@HiltAndroidApp
class ScheduleApplication : Application()

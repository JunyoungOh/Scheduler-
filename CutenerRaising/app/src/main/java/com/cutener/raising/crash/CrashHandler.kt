package com.cutener.raising.crash

import android.content.Context
import android.content.Intent
import android.util.Log

object CrashHandler {
    private const val PREFS_NAME = "crash_prefs"
    private const val KEY_STACKTRACE = "crash_stacktrace"
    private const val KEY_THREAD = "crash_thread"
    private const val KEY_TIMESTAMP = "crash_timestamp"

    fun install(context: Context) {
        val appContext = context.applicationContext
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            saveCrash(appContext, thread, throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    fun recordCrashIfPresent(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stacktrace = prefs.getString(KEY_STACKTRACE, null) ?: return
        val threadName = prefs.getString(KEY_THREAD, "unknown") ?: "unknown"
        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)

        val intent = Intent(context, CrashActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .putExtra(CrashActivity.EXTRA_STACKTRACE, stacktrace)
            .putExtra(CrashActivity.EXTRA_THREAD, threadName)
            .putExtra(CrashActivity.EXTRA_TIMESTAMP, timestamp)

        context.startActivity(intent)
    }

    fun clearCrash(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    private fun saveCrash(context: Context, thread: Thread, throwable: Throwable) {
        val stacktrace = Log.getStackTraceString(throwable)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_STACKTRACE, stacktrace)
            .putString(KEY_THREAD, thread.name)
            .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }
}

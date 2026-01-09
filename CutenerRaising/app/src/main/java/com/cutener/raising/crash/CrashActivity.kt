package com.cutener.raising.crash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stacktrace = intent.getStringExtra(EXTRA_STACKTRACE).orEmpty()
        val threadName = intent.getStringExtra(EXTRA_THREAD).orEmpty()
        val timestamp = intent.getLongExtra(EXTRA_TIMESTAMP, 0L)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CrashScreen(
                        stacktrace = stacktrace,
                        threadName = threadName,
                        timestamp = timestamp
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_STACKTRACE = "extra_stacktrace"
        const val EXTRA_THREAD = "extra_thread"
        const val EXTRA_TIMESTAMP = "extra_timestamp"
    }
}

@Composable
private fun CrashScreen(
    stacktrace: String,
    threadName: String,
    timestamp: Long
) {
    val context = LocalContext.current
    val formattedTime = if (timestamp > 0L) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
    } else {
        "unknown"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "앱이 예기치 않게 종료되었습니다",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "스레드: ${threadName.ifBlank { "unknown" }}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "시간: $formattedTime",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "스택 트레이스",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = stacktrace.ifBlank { "스택 트레이스가 없습니다." },
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            onClick = {
                CrashHandler.clearCrash(context)
                (context as? ComponentActivity)?.finish()
            }
        ) {
            Text(text = "닫기")
        }
    }

    LaunchedEffect(Unit) {
        CrashHandler.clearCrash(context)
    }
}

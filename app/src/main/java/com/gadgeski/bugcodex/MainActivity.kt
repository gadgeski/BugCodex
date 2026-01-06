package com.gadgeski.bugcodex

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.gadgeski.bugcodex.ui.AppScaffold
import com.gadgeski.bugcodex.ui.NotesViewModel
import com.gadgeski.bugcodex.ui.theme.BugCodexTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val vm: NotesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )

        enableStrictModeInDebug()
        seedDebugDataOnce()

        // ★ Fix: savedInstanceState の有無に関わらず、Intentにデータがあれば処理を試みる
        // (画面回転などの再生成時は、下の handleIntent 内で action が消されているため重複しない)
        handleIntent(intent)

        setContent {
            BugCodexTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent,
                ) {
                    AppScaffold(vm = vm)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // ★ Fix: 新しいIntentを受け取ったら、ActivityのIntentを更新する（重要）
        // これをしないと、getIntent() が古いままになり、整合性が取れなくなることがある
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        Log.d("BugCodex", "handleIntent: action=${intent?.action}, type=${intent?.type}")

        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("text/") == true) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            Log.d("BugCodex", "Shared Text received: ${sharedText?.take(20)}...")

            if (!sharedText.isNullOrBlank()) {
                vm.handleSharedText(sharedText)

                // ★ Fix: 処理済みIntentを「消費」する
                // これにより、画面回転などで onCreate が再走しても、同じテキストが再度処理されるのを防ぐ
                intent.action = ""
                intent.removeExtra(Intent.EXTRA_TEXT)
            }
        }
    }

    private fun enableStrictModeInDebug() {
        if (!BuildConfig.DEBUG) return
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectLeakedClosableObjects().detectActivityLeaks().penaltyLog().build())
    }

    private fun seedDebugDataOnce() {
        if (!BuildConfig.DEBUG) return
        val prefs = getSharedPreferences("debug_prefs", MODE_PRIVATE)
        val flagKey = "seed_done_v1"
        if (prefs.getBoolean(flagKey, false)) return

        lifecycleScope.launch {
            vm.addFolder("Inbox")
            vm.newNote()
            vm.setEditingTitle("サンプル: BugCodex へようこそ")
            vm.setEditingContent(
                """
                これはデバッグ用に自動投入されたサンプルノートです。
                - 進化したTech-LuxuryなUIをお楽しみください
                - 下部ナビから「Search / Folders」を試せます
                """.trimIndent(),
            )
            vm.saveEditing()
            prefs.edit { putBoolean(flagKey, true) }
        }
    }
}

@file:Suppress("ktlint:standard:function-naming")
@file:OptIn(ExperimentalMaterial3Api::class)

package com.gadgeski.bugcodex.ui.screens

import android.app.Activity
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gadgeski.bugcodex.R
import com.gadgeski.bugcodex.core.AppLocaleManager
import com.gadgeski.bugcodex.ui.SettingsViewModel
import com.gadgeski.bugcodex.ui.theme.*
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import kotlin.math.abs

/**
 * 設定画面（Iceberg Tech Edition）
 * [SECURITY FIX] BiometricPrompt を使用したセキュアなトークン表示を実装
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val ctx = LocalContext.current
    // BiometricPrompt には FragmentActivity が必須。MainActivity が継承している必要があります。
    val activity = ctx as? FragmentActivity
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- データの監視 ---
    val languageTag by AppLocaleManager.languageTagFlow(ctx)
        .collectAsStateWithLifecycle(initialValue = "")

    val editorFontScale by AppLocaleManager.editorFontScaleFlow(ctx)
        .collectAsStateWithLifecycle(initialValue = 1.0f)

    val githubToken by viewModel.githubToken.collectAsStateWithLifecycle()

    // --- UI 状態 ---
    var selected by remember(languageTag) { mutableStateOf(languageTag) }
    var tempScale by rememberSaveable(editorFontScale) { mutableFloatStateOf(editorFontScale) }
    var tempToken by remember(githubToken) { mutableStateOf(githubToken) }
    var isTokenVisible by remember { mutableStateOf(false) }

    // --- 生体認証処理 ---
    val authenticate: () -> Unit = {
        activity?.let {
            val executor: Executor = ContextCompat.getMainExecutor(it)
            val biometricPrompt = BiometricPrompt(it, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        isTokenVisible = true
                    }
                })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("SECURITY_VERIFICATION")
                .setSubtitle("Authenticate to reveal sensitive data")
                .setNegativeButtonText(it.getString(android.R.string.cancel))
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }

    val backgroundBrush = remember {
        Brush.verticalGradient(
            colors = listOf(IceHorizon, IceSlate, IceDeepNavy),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "SYSTEM_CONFIG",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = IceTextPrimary,
                    ),
                    modifier = Modifier.statusBarsPadding(),
                )
            },
        ) { inner ->
            Column(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // ===== 言語設定 =====
                SettingsGlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(title = "LANGUAGE_SETTINGS")
                        listOf(
                            "" to R.string.pref_language_system,
                            "ja" to R.string.pref_language_ja,
                            "en" to R.string.pref_language_en
                        ).forEach { (code, resId) ->
                            LanguageOptionRow(
                                selected = selected == code,
                                label = stringResource(resId),
                                onClick = { selected = code },
                            )
                        }
                    }
                }

                // ===== エディタ外観 =====
                SettingsGlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(title = "EDITOR_APPEARANCE")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.pref_editor_font_size),
                                style = MaterialTheme.typography.bodyMedium,
                                color = IceTextPrimary,
                            )
                            Text(
                                text = "${(tempScale * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = IceCyan,
                            )
                        }
                        Slider(
                            value = tempScale,
                            onValueChange = { tempScale = it.coerceIn(0.5f, 2.0f) },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = IceCyan,
                                activeTrackColor = IceCyan,
                                inactiveTrackColor = IceGlassBorder,
                                activeTickColor = IceDeepNavy,
                                inactiveTickColor = IceSilver,
                            ),
                        )
                    }
                }

                // ===== GitHub連携 (Secure) =====
                SettingsGlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(title = "GITHUB_INTEGRATION")

                        Text(
                            text = "Personal Access Token (gist scope)",
                            style = MaterialTheme.typography.bodySmall,
                            color = IceTextSecondary,
                        )

                        OutlinedTextField(
                            value = tempToken,
                            onValueChange = { tempToken = it },
                            placeholder = {
                                Text("ghp_xxxxxxxx...", color = IceTextSecondary.copy(alpha = 0.5f))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (isTokenVisible) isTokenVisible = false else authenticate()
                                }) {
                                    Icon(
                                        imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Visibility",
                                        tint = IceCyan
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = IceTextPrimary,
                                unfocusedTextColor = IceTextPrimary,
                                cursorColor = IceCyan,
                                focusedBorderColor = IceCyan,
                                unfocusedBorderColor = IceGlassBorder,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ===== アクションボタン =====
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f).height(50.dp),
                        border = BorderStroke(1.dp, IceSilver.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = IceTextPrimary,
                        ),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(stringResource(R.string.action_close))
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                if (selected != languageTag) {
                                    AppLocaleManager.setLanguage(ctx, selected)
                                    (ctx as? Activity)?.recreate()
                                }
                                if (abs(tempScale - editorFontScale) > 0.0001f) {
                                    AppLocaleManager.setEditorFontScale(ctx, tempScale)
                                }
                                if (tempToken != githubToken) {
                                    viewModel.updateGithubToken(tempToken)
                                }
                                snackbarHostState.showSnackbar("SYSTEM_UPDATED: Changes applied securely.")
                            }
                        },
                        enabled = (selected != languageTag) || (abs(tempScale - editorFontScale) > 0.0001f) || (tempToken != githubToken),
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IceCyan,
                            contentColor = IceDeepNavy,
                            disabledContainerColor = IceGlassSurface.copy(alpha = 0.3f),
                            disabledContentColor = IceTextSecondary.copy(alpha = 0.5f),
                        ),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(stringResource(R.string.action_apply), fontWeight = FontWeight.Bold)
                    }
                }

                val density = LocalDensity.current
                Spacer(Modifier.height(WindowInsets.statusBars.getTop(density).dp + 24.dp))
            }
        }
    }
}

@Composable
private fun SettingsGlassCard(
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IceGlassSurface),
        border = BorderStroke(1.dp, IceGlassBorder),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
            ),
            color = IceTextSecondary,
        )
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(IceGlassBorder),
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun LanguageOptionRow(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = IceCyan,
                unselectedColor = IceSilver,
            ),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) IceCyan else IceTextPrimary,
        )
    }
}
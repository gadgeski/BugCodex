@file:Suppress("ktlint:standard:function-naming")
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gadgeski.bugcodex.ui.screens

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gadgeski.bugcodex.R
import com.gadgeski.bugcodex.core.AppLocaleManager
import com.gadgeski.bugcodex.data.prefs.SettingsRepository
import com.gadgeski.bugcodex.ui.theme.IceCyan
import com.gadgeski.bugcodex.ui.theme.IceDeepNavy
import com.gadgeski.bugcodex.ui.theme.IceGlassBorder
import com.gadgeski.bugcodex.ui.theme.IceGlassSurface
import com.gadgeski.bugcodex.ui.theme.IceHorizon
import com.gadgeski.bugcodex.ui.theme.IceSilver
import com.gadgeski.bugcodex.ui.theme.IceSlate
import com.gadgeski.bugcodex.ui.theme.IceTextPrimary
import com.gadgeski.bugcodex.ui.theme.IceTextSecondary
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * 設定画面（Iceberg Tech Edition）
 * - GitHub Token 設定を追加
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
) {
    val ctx = LocalContext.current
    val activity = ctx as? Activity

    val languageTag by AppLocaleManager.languageTagFlow(ctx)
        .collectAsStateWithLifecycle(initialValue = "")

    val editorFontScale by AppLocaleManager.editorFontScaleFlow(ctx)
        .collectAsStateWithLifecycle(initialValue = 1.0f)

    val githubToken by SettingsRepository.get(ctx).githubToken.collectAsStateWithLifecycle()

    var selected by remember(languageTag) { mutableStateOf(languageTag) }
    var tempScale by rememberSaveable(editorFontScale) { mutableFloatStateOf(editorFontScale) }
    var tempToken by remember(githubToken) { mutableStateOf(githubToken) }

    val scope = rememberCoroutineScope()

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
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "SYSTEM_CONFIG",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
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
                // ===== 言語設定セクション =====
                SettingsGlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(title = "LANGUAGE_SETTINGS")
                        LanguageOptionRow(
                            selected = selected == "",
                            label = stringResource(R.string.pref_language_system),
                            onClick = { selected = "" },
                        )
                        LanguageOptionRow(
                            selected = selected == "ja",
                            label = stringResource(R.string.pref_language_ja),
                            onClick = { selected = "ja" },
                        )
                        LanguageOptionRow(
                            selected = selected == "en",
                            label = stringResource(R.string.pref_language_en),
                            onClick = { selected = "en" },
                        )
                    }
                }

                // ===== エディタ外観セクション =====
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
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
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

                // ===== GitHub連携セクション (New) =====
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
                            visualTransformation = PasswordVisualTransformation(), // パスワード形式で表示
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
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
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
                                    activity?.recreate()
                                }
                                if (abs(tempScale - editorFontScale) > 0.0001f) {
                                    AppLocaleManager.setEditorFontScale(ctx, tempScale)
                                }
                                // トークン保存
                                if (tempToken != githubToken) {
                                    SettingsRepository.get(ctx).setGithubToken(tempToken)
                                }
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
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
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

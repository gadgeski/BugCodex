// app/src/main/java/com/gadgeski/bugmemo/core/FeatureFlags.kt

package com.gadgeski.bugcodex.core

import com.gadgeski.bugcodex.BuildConfig

// ★ Added: app の namespace に合わせた BuildConfig を import
// ★ keep: 共有フラグを一箇所にまとめるためのオブジェクト
object FeatureFlags {
    // ★ keep: BuildConfig.DEBUG はコンパイル時定数ではないため const は使わない
    // ★ keep: ここを true/false に固定しないことで、debug/release で自動的に切り替わる
    val ENABLE_MIND_MAP_DEBUG: Boolean = BuildConfig.DEBUG
}

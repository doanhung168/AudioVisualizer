package com.doan_hung.visual_audio_player_lib

import android.content.Context

fun dpToPx(dp: Float, context: Context): Int {
    return (dp * context.resources.displayMetrics.density).toInt()
}
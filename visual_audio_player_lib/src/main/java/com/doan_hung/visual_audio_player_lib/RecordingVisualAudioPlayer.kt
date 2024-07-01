package com.doan_hung.visual_audio_player_lib

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * RecordingVisualAudioPlayer class.
 * Displays a simple audio visualizer with straight waves.
 *
 * Author: Nguyễn Doãn Hùng
 */
class RecordingVisualAudioPlayer : View {

    companion object {
        const val WAVE_PERCENT = 1f
        const val WAVE_COLOR = "#FF2667"
        var WAVE_OFFSET = 0f
        var WAVE_WIDTH = 0f
    }

    private var waveValues: MutableList<Int> = mutableListOf()

    private var waveOffset = 0f
    private var waveWidth = 0f
    private var wavePercent = WAVE_PERCENT

    private var verticalWaveOffer = 0f

    private var startY = 0f
    private var stopY = 0f
    private var startX = 0f

    private val wavePaint = Paint().apply {
        color = Color.parseColor(WAVE_COLOR)
        strokeCap = Paint.Cap.ROUND
        strokeWidth = waveWidth
        style = Paint.Style.FILL_AND_STROKE
    }

    fun addData(newValue: Int) {
        waveValues.add(0, newValue)
        invalidate()
    }

    fun clearData() {
        waveValues.clear()
        invalidate()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
        loadAttributeSets(attrs)
    }

    private fun init() {
        WAVE_OFFSET = dpToPx(6f, context).toFloat()
        WAVE_WIDTH = dpToPx(2f, context).toFloat()
        waveOffset = WAVE_OFFSET
        waveWidth = WAVE_WIDTH
        wavePaint.strokeWidth = waveWidth
    }

    private fun loadAttributeSets(attrs: AttributeSet) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RecordingVisualAudioPlayer,
            0,
            0
        ).apply {
            try {
                waveOffset = getDimension(
                    R.styleable.RecordingVisualAudioPlayer_vap_waveOffset,
                    WAVE_OFFSET
                )

                waveWidth = getDimension(
                    R.styleable.RecordingVisualAudioPlayer_vap_waveWidth,
                    WAVE_WIDTH
                )

                wavePercent = getFloat(
                    R.styleable.RecordingVisualAudioPlayer_vap_wavePercent,
                    WAVE_PERCENT
                )

                wavePaint.color = getColor(
                    R.styleable.RecordingVisualAudioPlayer_vap_waveColor,
                    Color.parseColor(WAVE_COLOR)
                )

            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        verticalWaveOffer = (height / 2).toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        verticalWaveOffer = (h / 2).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        for (i in waveValues.indices) {
            startY = verticalWaveOffer - (waveValues[i] / 2 * wavePercent)
            stopY = (waveValues[i] / 2 * wavePercent) + verticalWaveOffer
            startX = width - (i * (waveOffset + waveWidth))

            if (startX < 0) {
                return
            }

            canvas.drawLine(startX, startY, startX, stopY, wavePaint)
        }
    }

}
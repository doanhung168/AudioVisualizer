package com.doan_hung.visual_audio_player_lib

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller
import androidx.core.content.ContextCompat

/**
 * CutVisualAudioPlayer class.
 * Displays a simple audio visualizer with time line that can be scrolled and seek to a specific time.
 * Support for cut file feature also.
 *
 * Author: Nguyễn Doãn Hùng
 */
class CutVisualAudioPlayer : View {

    companion object {
        const val WAVE_PERCENT = 1f
        const val WAVE_COLOR = "#FF2667"
        const val INACTIVE_WAVE_COLOR = "#858585"
        const val TIME_LINE_COLOR = "#FF9DBB"
        var WAVE_OFFSET = 0f
        var WAVE_WIDTH = 0f

    }

    private var onSelectTimeLine: () -> Unit = {}

    private lateinit var scroller: Scroller

    private val TAG: String = "TAG"
    private var waveValues: MutableList<Int> = mutableListOf()
    private var totalTime: Long = 0

    private var waveOffset = 0f
    private var waveWidth = 0f
    private var wavePercent = WAVE_PERCENT

    private var verticalWaveOffer = 0f

    private var startY = 0f
    private var stopY = 0f
    private var startX = 0f

    private var contentWidth = 0f


    private val wavePaint = Paint().apply {
        color = Color.parseColor(WAVE_COLOR)
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.FILL_AND_STROKE
    }

    private val inactiveWavePaint = Paint().apply {
        color = Color.parseColor(INACTIVE_WAVE_COLOR)
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.FILL_AND_STROKE
    }

    private val headDrawable = ContextCompat.getDrawable(context, R.drawable.ic_cut_pre)!!
    private val headWidth = headDrawable.intrinsicWidth

    private val tailDrawable = ContextCompat.getDrawable(context, R.drawable.ic_cut_next)!!
    private val tailWidth = tailDrawable.intrinsicWidth

    private var head = headWidth.toFloat()
    private var keepHead = false
    private var tail = (headWidth + tailWidth).toFloat()
    private var keepTail = false


    private var timeLine = 0f
    private var timeWidth = 0f
    private var keepTimeLine = false
    private val timeLinePaint = Paint().apply {
        color = Color.parseColor(TIME_LINE_COLOR)
        strokeCap = Paint.Cap.ROUND
        timeWidth = dpToPx(2f, context).toFloat()
        strokeWidth = timeWidth
        style = Paint.Style.FILL_AND_STROKE
    }

    private var lastX = 0f

    fun setData(newValue: List<Int>) {
        waveValues = newValue.toMutableList()
        calculateContentWidth()
        invalidate()
    }

    fun setTotalTime(time: Long) {
        Log.i(TAG, "setTotalTime: $totalTime")
        totalTime = time
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
        inactiveWavePaint.strokeWidth = waveWidth

        timeLinePaint.strokeWidth = dpToPx(4f, context).toFloat()

        scroller = Scroller(context)
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
        drawWave(canvas)
        drawHead(canvas)
        drawTail(canvas)
        drawLineTime(canvas)
    }

    private fun drawLineTime(canvas: Canvas) {
        canvas.drawLine(
            timeLine,
            height.toFloat() * 0.05f,
            timeLine,
            height.toFloat() * 0.95f,
            timeLinePaint
        )
    }


    private fun drawHead(canvas: Canvas) {
        drawCutBarLine(canvas)
        headDrawable.setBounds(head.toInt() - headWidth, 0, head.toInt(), height)
        headDrawable.draw(canvas)
    }


    private fun drawTail(canvas: Canvas) {
        drawCutBarLine(canvas)
        tailDrawable.setBounds(tail.toInt(), 0, tail.toInt() + tailWidth, height)
        tailDrawable.draw(canvas)

    }

    private fun drawWave(canvas: Canvas) {
        for (i in waveValues.indices) {
            startX = (i * (waveOffset + waveWidth)) + getHeadInsert()
            startY = verticalWaveOffer - (waveValues[i] / 2 * wavePercent)
            stopY = (waveValues[i] / 2 * wavePercent) + verticalWaveOffer

            if (startX in head..tail) {
                canvas.drawLine(startX, startY, startX, stopY, wavePaint)
            } else {
                canvas.drawLine(startX, startY, startX, stopY, inactiveWavePaint)
            }
        }
        drawTailInsert(canvas)
    }

    private fun drawTailInsert(canvas: Canvas) {
        val left = (waveValues.size - 1) * (waveOffset + waveWidth) + getTailInsert()
        val right = left + tailWidth + (waveOffset + waveWidth)
        canvas.drawRect(left, 0f, right, height.toFloat(), Paint().apply {
            color = Color.TRANSPARENT
        })
    }

    private fun getHeadInsert(): Float {
        return headWidth + waveWidth
    }

    private fun getTailInsert(): Float {
        return tailWidth + waveWidth
    }

    private fun drawCutBarLine(canvas: Canvas) {
        canvas.drawLine((head - (headWidth / 2)), 0f, (tail + (tailWidth / 2)), 0f, wavePaint)
        canvas.drawLine(
            (head - (headWidth / 2)),
            height.toFloat(),
            (tail + (tailWidth / 2)),
            height.toFloat(),
            wavePaint
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!scroller.isFinished) {
                    scroller.abortAnimation();
                }
                lastX = event.x

                keepHead = selectHead(event.x)
                keepTail = selectTail(event.x)
                keepTimeLine = selectTimeLine(event.x)

            }

            MotionEvent.ACTION_MOVE -> {
                if (keepHead) {
                    head = event.x + scrollX
                    if (head <= headWidth) {
                        head = headWidth.toFloat()
                    }

                    if (head >= tail) {
                        head = tail
                    }

                    invalidate()
                }

                if (keepTail) {

                    tail = event.x + scrollX

// auto scroll feature, not yet complete implementation
//                    if((event.x) >= (width - tailWidth) ) {
//                        val dx = lastX - event.x
//                        lastX = event.x
//                        tail += 50
//                        scrollBy(50, 0)
//                    }

                    if ((tail + tailWidth) >= contentWidth) {
                        tail = contentWidth - tailWidth
                    }

                    if (tail <= head) {
                        tail = head
                    }
                    invalidate()
                }

                if (!keepTail && !keepHead) {
                    val dx = lastX - event.x
                    lastX = event.x
                    scrollBy(dx.toInt(), 0)
                }

                keepTimeLine = selectTimeLine(event.x)

            }

            MotionEvent.ACTION_UP -> {
                if (keepHead) {
                    keepHead = false
                }

                if (keepTail) {
                    keepTail = false
                }

                if (keepTimeLine) {
                    val timeLinePosition = event.x + scrollX
                    if (timeLinePosition > head) {
                        tail = timeLinePosition
                    } else {
                        head = timeLinePosition
                    }
                    invalidate()
                    onSelectTimeLine.invoke()

                }
            }
        }

        return true
    }

    private fun selectTimeLine(x: Float): Boolean {
        return x >= (timeLine - scrollX - (tailWidth / 2)) && x <= (timeLine - scrollX + (tailWidth / 2))
    }

    private fun selectTail(x: Float): Boolean {
        return x >= (tail - scrollX) && x <= (tail + tailWidth - scrollX)
    }

    private fun selectHead(x: Float): Boolean {
        return x <= (head - scrollX) && x >= (head - headWidth - scrollX)
    }

    private fun calculateContentWidth() {
        contentWidth =
            getHeadInsert() + (waveValues.size - 1) * (waveOffset + waveWidth) + getTailInsert()
    }

    override fun scrollBy(x: Int, y: Int) {
        var newX = scrollX + x
        if (newX < 0) {
            newX = 0
        } else if (newX > contentWidth - width) {
            newX = (contentWidth - width).toInt()
        }
        super.scrollTo(newX, y)
    }

    fun seekOver(time: Long, onSeek: (Long) -> Unit) {
        val process = (timeLine / (contentWidth - getHeadInsert()))
        val currentTime = process * totalTime
        var expectedTime = currentTime + time
        if (expectedTime >= totalTime) {
            expectedTime = totalTime.toFloat()
        }

        updateTime(expectedTime.toLong())
        onSeek(expectedTime.toLong())
    }

    fun updateTime(time: Long) {
        val process = time.toFloat() / totalTime.toFloat()
        timeLine = (process * contentWidth) - getHeadInsert()

        if (timeLine < (width / 2)) {
            scrollTo(0, 0)
        }

        if (timeLine > (width / 2) && timeLine < contentWidth - (width / 2)) {
            scrollTo((timeLine - (width / 2)).toInt(), 0)
        }

        invalidate()
    }

    fun setOnSelectTimeLine(onSelectTimeLine: () -> Unit) {
        this.onSelectTimeLine = onSelectTimeLine
    }


}
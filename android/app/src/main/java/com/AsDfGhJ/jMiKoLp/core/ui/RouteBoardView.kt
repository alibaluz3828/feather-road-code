package com.AsDfGhJ.jMiKoLp.core.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.AsDfGhJ.jMiKoLp.R
import com.AsDfGhJ.jMiKoLp.domain.model.Cell
import com.AsDfGhJ.jMiKoLp.domain.model.Direction
import com.AsDfGhJ.jMiKoLp.domain.model.RoutePlan
import kotlin.math.min

class RouteBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density
    private val framePx = (BOARD_PAD_DP + BORDER_DP) * density
    private val maxWidthPx = BOARD_MAX_DP * density

    private var cellPx = 0f
    private var boardSize = 5

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f * density
        pathEffect = DashPathEffect(floatArrayOf(7f * density, 5f * density), 0f)
    }
    private val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f * density
        strokeCap = Paint.Cap.ROUND
    }
    private val rect = RectF()

    private var cartDrawable: Drawable? = null
    private var signDrawable: Drawable? = null
    private var checkpointDrawable: Drawable? = null
    private var flagDrawable: Drawable? = null
    private var coneDrawable: Drawable? = null

    private var plan: RoutePlan? = null
    private var placements: Map<Cell, Direction> = emptyMap()
    private var trail: List<Cell> = emptyList()
    private var passedCheckpoints: Set<Cell> = emptySet()
    private var cartCell: Cell? = null
    private var cartHeading: Direction = Direction.UP
    private var selectedSocket: Cell? = null
    private var highContrast = false

    var onSocketTapped: ((Cell) -> Unit)? = null

    init {
        isClickable = true
        isFocusable = true
    }

    fun submitPlan(plan: RoutePlan) {
        this.plan = plan
        if (boardSize != plan.boardSize) {
            boardSize = plan.boardSize
            requestLayout()
        }
        cartCell = plan.entry
        cartHeading = plan.entryHeading
        invalidate()
    }

    fun submitState(
        placements: Map<Cell, Direction>,
        trail: List<Cell>,
        passedCheckpoints: Set<Cell>,
        cartCell: Cell?,
        cartHeading: Direction,
        selectedSocket: Cell?,
        highContrast: Boolean
    ) {
        this.placements = placements
        this.trail = trail
        this.passedCheckpoints = passedCheckpoints
        this.cartCell = cartCell
        this.cartHeading = cartHeading
        this.selectedSocket = selectedSocket
        this.highContrast = highContrast
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val available = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        val verticalMode = MeasureSpec.getMode(heightMeasureSpec)
        val vertical = if (verticalMode == MeasureSpec.UNSPECIFIED) {
            maxWidthPx
        } else {
            MeasureSpec.getSize(heightMeasureSpec).toFloat()
        }
        val limit = min(min(available, vertical), maxWidthPx)
        val usable = limit - 2f * framePx
        val cell = if (usable > 0f) (usable / boardSize).toInt().toFloat() else 0f
        cellPx = cell
        val total = (cell * boardSize + 2f * framePx).toInt()
        setMeasuredDimension(total, total)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val current = plan ?: return
        if (cellPx <= 0f) {
            return
        }
        drawFrame(canvas)
        drawPlates(canvas, current)
        drawTrail(canvas)
        drawGate(canvas, current)
        drawProps(canvas, current)
        drawSockets(canvas, current)
        drawCart(canvas)
    }

    private fun drawFrame(canvas: Canvas) {
        val shadow = 5f * density
        fillPaint.color = color(R.color.ink)
        rect.set(shadow, shadow, width.toFloat(), height.toFloat())
        canvas.drawRect(rect, fillPaint)
        fillPaint.color = color(R.color.bg_mid)
        rect.set(0f, 0f, width - shadow, height - shadow)
        canvas.drawRect(rect, fillPaint)
        strokePaint.color = color(R.color.outline)
        strokePaint.strokeWidth = BORDER_DP * density
        rect.set(
            BORDER_DP * density / 2f,
            BORDER_DP * density / 2f,
            width - shadow - BORDER_DP * density / 2f,
            height - shadow - BORDER_DP * density / 2f
        )
        canvas.drawRect(rect, strokePaint)
    }

    private fun drawPlates(canvas: Canvas, current: RoutePlan) {
        val joint = if (highContrast) 2f * density else 1f * density
        for (col in 0 until current.boardSize) {
            for (row in 0 until current.boardSize) {
                val even = (col + row) % 2 == 0
                fillPaint.color = when {
                    highContrast && even -> color(R.color.surface_raised)
                    highContrast -> color(R.color.bg_deep)
                    even -> color(R.color.plate_light)
                    else -> color(R.color.plate_dark)
                }
                cellRect(col, row, joint)
                canvas.drawRect(rect, fillPaint)
            }
        }
    }

    private fun drawTrail(canvas: Canvas) {
        if (trail.size < 2) {
            return
        }
        trailPaint.color = color(R.color.accent_primary)
        for (index in 0 until trail.size - 1) {
            val from = trail[index]
            val to = trail[index + 1]
            if (from == to) {
                continue
            }
            canvas.drawLine(
                centerX(from.col),
                centerY(from.row),
                centerX(to.col),
                centerY(to.row),
                trailPaint
            )
        }
    }

    private fun drawGate(canvas: Canvas, current: RoutePlan) {
        fillPaint.color = color(R.color.accent_go)
        val bar = 6f * density
        val left = framePx + current.entry.col * cellPx + cellPx * 0.2f
        val right = framePx + current.entry.col * cellPx + cellPx * 0.8f
        val bottom = framePx + (current.entry.row + 1) * cellPx - 2f * density
        rect.set(left, bottom - bar, right, bottom)
        canvas.drawRect(rect, fillPaint)
    }

    private fun drawProps(canvas: Canvas, current: RoutePlan) {
        val flag = flagDrawable ?: ContextCompat.getDrawable(context, R.drawable.sprite_flag_goal)
            ?.also { flagDrawable = it }
        if (flag != null) {
            strokePaint.color = color(R.color.accent_go)
            strokePaint.strokeWidth = BORDER_DP * density
            cellRect(current.exit.col, current.exit.row, 2f * density)
            canvas.drawRect(rect, strokePaint)
            drawSprite(canvas, flag, current.exit, 0.74f, 1f)
        }
        val checkpoint = checkpointDrawable
            ?: ContextCompat.getDrawable(context, R.drawable.sprite_checkpoint)
                ?.also { checkpointDrawable = it }
        if (checkpoint != null) {
            for (cell in current.checkpoints) {
                val passed = passedCheckpoints.contains(cell)
                drawSprite(canvas, checkpoint, cell, 0.62f, if (passed) 1f else 0.45f)
            }
        }
        val cone = coneDrawable ?: ContextCompat.getDrawable(context, R.drawable.sprite_cone)
            ?.also { coneDrawable = it }
        if (cone != null) {
            for (cell in current.obstacles) {
                drawSprite(canvas, cone, cell, 0.54f, 1f)
            }
        }
    }

    private fun drawSockets(canvas: Canvas, current: RoutePlan) {
        val sign = signDrawable ?: ContextCompat.getDrawable(context, R.drawable.sprite_sign_arrow)
            ?.also { signDrawable = it }
        for (socket in current.sockets) {
            val placed = placements[socket]
            if (placed == null) {
                dashPaint.color = color(R.color.accent_primary)
                dashPaint.alpha = if (selectedSocket == socket) 255 else 190
                cellRect(socket.col, socket.row, cellPx * 0.14f)
                canvas.drawRect(rect, dashPaint)
            } else {
                if (sign != null) {
                    drawSprite(canvas, sign, socket, 0.82f, 1f)
                }
                fillPaint.color = color(R.color.accent_primary)
                cellRect(socket.col, socket.row, cellPx * 0.22f)
                canvas.drawRect(rect, fillPaint)
                strokePaint.color = color(R.color.ink)
                strokePaint.strokeWidth = BORDER_DP * density
                canvas.drawRect(rect, strokePaint)
                val glyph = ContextCompat.getDrawable(context, CodeChipStripView.glyphFor(placed))
                if (glyph != null) {
                    glyph.setTint(color(R.color.ink))
                    val half = cellPx * 0.18f
                    glyph.setBounds(
                        (centerX(socket.col) - half).toInt(),
                        (centerY(socket.row) - half).toInt(),
                        (centerX(socket.col) + half).toInt(),
                        (centerY(socket.row) + half).toInt()
                    )
                    glyph.draw(canvas)
                }
            }
        }
    }

    private fun drawCart(canvas: Canvas) {
        val cell = cartCell ?: return
        val cart = cartDrawable ?: ContextCompat.getDrawable(context, R.drawable.sprite_cart)
            ?.also { cartDrawable = it }
        if (cart == null) {
            return
        }
        canvas.save()
        canvas.rotate(cartHeading.degrees(), centerX(cell.col), centerY(cell.row))
        drawSprite(canvas, cart, cell, 0.78f, 1f)
        canvas.restore()
    }

    private fun drawSprite(
        canvas: Canvas,
        drawable: Drawable,
        cell: Cell,
        scale: Float,
        alpha: Float
    ) {
        val half = cellPx * scale / 2f
        drawable.alpha = (alpha * 255).toInt().coerceIn(0, 255)
        drawable.setBounds(
            (centerX(cell.col) - half).toInt(),
            (centerY(cell.row) - half).toInt(),
            (centerX(cell.col) + half).toInt(),
            (centerY(cell.row) + half).toInt()
        )
        drawable.draw(canvas)
    }

    private fun cellRect(col: Int, row: Int, inset: Float) {
        val left = framePx + col * cellPx + inset
        val top = framePx + row * cellPx + inset
        rect.set(left, top, left + cellPx - 2f * inset, top + cellPx - 2f * inset)
    }

    private fun centerX(col: Int): Float = framePx + (col + 0.5f) * cellPx

    private fun centerY(row: Int): Float = framePx + (row + 0.5f) * cellPx

    private fun color(resId: Int): Int = ContextCompat.getColor(context, resId)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked != MotionEvent.ACTION_UP) {
            return super.onTouchEvent(event)
        }
        val current = plan
        if (current == null || cellPx <= 0f) {
            return super.onTouchEvent(event)
        }
        val col = ((event.x - framePx) / cellPx).toInt()
        val row = ((event.y - framePx) / cellPx).toInt()
        val cell = Cell(col, row)
        if (cell.isInside(current.boardSize) && current.sockets.contains(cell)) {
            performClick()
            onSocketTapped?.invoke(cell)
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun clearSprites() {
        cartDrawable = null
        signDrawable = null
        checkpointDrawable = null
        flagDrawable = null
        coneDrawable = null
    }

    private companion object {
        const val BOARD_MAX_DP = 380f
        const val BOARD_PAD_DP = 8f
        const val BORDER_DP = 3f
    }
}

package com.example.layout

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec.AT_MOST
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.UNSPECIFIED
import android.view.View.MeasureSpec.getMode
import android.view.View.MeasureSpec.getSize
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes


class StraightLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private var orientation = Orientation.HORIZONTAL
    private var weightSum = 0f

    @ColorInt
    private var borderColor = Color.WHITE
    private var borderWidth = 0f
    private var cornerRadius = 0f

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val rectF = RectF()
    private val path = Path()

    init {
        context.withStyledAttributes(
            attrs,
            R.styleable.StraightLayout,
            defStyleAttr,
            defStyleRes
        ) {
            orientation = Orientation.values()[getInt(
                R.styleable.StraightLayout_android_orientation,
                0
            )]
            weightSum = getFloat(R.styleable.StraightLayout_android_weightSum, 0f)
            cornerRadius = getDimension(R.styleable.StraightLayout_cornerRadius, 0f)
            borderWidth = getDimension(R.styleable.StraightLayout_borderWidth, 0f)
            borderColor = getColor(R.styleable.StraightLayout_borderColor, Color.TRANSPARENT)
            borderPaint.strokeWidth = borderWidth
            borderPaint.color = borderColor
        }
        setWillNotDraw(false)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        for (i in 0..childCount) {
            getChildAt(i)?.let {
                measureChild(it, widthMeasureSpec, heightMeasureSpec)
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (orientation == Orientation.VERTICAL) {
            layoutVertical()
        } else {
            layoutHorizontal()
        }
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        rectF.set(
            child.x,
            child.y,
            child.x + child.width,
            child.y + child.height
        )
        path.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW)
        canvas.save()
        canvas.clipPath(path)
        return super.drawChild(canvas, child, drawingTime).also {
            canvas.restore()
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        for (i in 0..childCount) {
            getChildAt(i)?.let {
                rectF.set(
                    it.x + borderWidth / 2,
                    it.y + borderWidth / 2,
                    it.x + it.width - borderWidth / 2,
                    it.y + it.height - borderWidth / 2
                )
                if (borderWidth > 0) {
                    canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, borderPaint)
                }
            }
        }
    }

    override fun measureChild(child: View, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wSpec = when (getMode(widthMeasureSpec)) {
            UNSPECIFIED -> widthMeasureSpec
            AT_MOST -> widthMeasureSpec
            EXACTLY -> makeMeasureSpec(
                getSize(widthMeasureSpec),
                AT_MOST
            )
            else -> throw IllegalStateException()
        }


        val hSpec = when (getMode(heightMeasureSpec)) {
            UNSPECIFIED -> heightMeasureSpec
            AT_MOST -> heightMeasureSpec
            EXACTLY -> makeMeasureSpec(
                getSize(heightMeasureSpec),
                AT_MOST
            )
            else -> throw IllegalStateException()
        }

        child.measure(wSpec, hSpec)
    }

    override fun generateDefaultLayoutParams(): LayoutParams = when (orientation) {
        Orientation.HORIZONTAL -> LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        Orientation.VERTICAL -> LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams =
        LayoutParams(context, attrs)

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): LayoutParams =
        LayoutParams(p)

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    private fun layoutVertical() {
        var resultHeight = 0
        if (weightSum == 0f) {
            for (i in 0..childCount) {
                getChildAt(i)?.let {
                    val weight = (it.layoutParams as LayoutParams).weight
                    if (weight == 0f) weightSum += 1 else weightSum += weight
                }
            }
        }
        for (i in 0..childCount) {
            getChildAt(i)?.let {
                val lp = it.layoutParams as LayoutParams
                val partOfLayout = when (lp.height) {
                    MATCH_PARENT -> lp.weight / weightSum + (it.paddingTop + it.paddingBottom) / height.toFloat()
                    WRAP_CONTENT -> (it.paddingTop + it.paddingBottom + it.measuredHeight) / height.toFloat()
                    else -> (it.paddingTop + it.paddingBottom + lp.width) / width.toFloat() * lp.weight
                }
                resultHeight += lp.topMargin + lp.bottomMargin
                resultHeight += if (lp.weight == 0f)
                    when (lp.height) {
                        WRAP_CONTENT -> it.measuredHeight
                        MATCH_PARENT -> height
                        else -> lp.height
                    }
                else (partOfLayout * height).toInt()
            }
        }
        val scaleMultiplier = if (resultHeight <= height) 1f else height / resultHeight.toFloat()
        var layoutHeight = 0
        for (i in 0..childCount) {
            val child = getChildAt(i)
            child?.let {
                val lp = it.layoutParams as LayoutParams
                val partOfLayout = when (lp.height) {
                    MATCH_PARENT -> lp.weight / weightSum + (it.paddingTop + it.paddingBottom) / height.toFloat()
                    WRAP_CONTENT -> (it.paddingTop + it.paddingBottom + it.measuredHeight) / height.toFloat()
                    else -> (it.paddingTop + it.paddingBottom + lp.width) / width.toFloat() * lp.weight
                }
                var childHeight =
                    if (lp.weight == 0f)
                        when (lp.height) {
                            WRAP_CONTENT -> it.measuredHeight
                            MATCH_PARENT -> height
                            else -> lp.height
                        }
                    else (partOfLayout * height).toInt()
                val childWidth = when (lp.width) {
                    WRAP_CONTENT -> it.measuredWidth
                    MATCH_PARENT -> width
                    else -> lp.width
                }
                childHeight = (childHeight * scaleMultiplier).toInt()
                val marginLeft = lp.leftMargin
                val marginRight = lp.rightMargin
                val marginTop = ((lp.topMargin) * scaleMultiplier).toInt()
                val marginBottom = ((lp.bottomMargin) * scaleMultiplier).toInt()
                layoutHeight += marginTop
                it.layout(
                    marginLeft,
                    layoutHeight,
                    childWidth + marginRight,
                    layoutHeight + childHeight - marginTop
                )
                layoutHeight += childHeight - marginTop + marginBottom
            }
        }
    }

    private fun layoutHorizontal() {
        var resultWidth = 0
        if (weightSum == 0f)
            for (i in 0..childCount) {
                getChildAt(i)?.let {
                    val weight = (it.layoutParams as LayoutParams).weight
                    if (weight == 0f) weightSum += 1 else weightSum += weight
                }
            }
        for (i in 0..childCount) {
            getChildAt(i)?.let {
                val lp = it.layoutParams as LayoutParams
                val partOfLayout = when (lp.width) {
                    MATCH_PARENT -> lp.weight / weightSum + (it.paddingRight + it.paddingLeft) / width.toFloat()
                    WRAP_CONTENT -> (it.paddingRight + it.paddingLeft + it.measuredWidth) / width.toFloat()
                    else -> (it.paddingRight + it.paddingLeft + lp.width) / width.toFloat() * lp.weight
                }
                resultWidth += lp.marginStart + lp.marginEnd
                resultWidth += if (lp.weight == 0f)
                    when (lp.width) {
                        WRAP_CONTENT -> it.measuredWidth
                        MATCH_PARENT -> width
                        else -> lp.width
                    }
                else
                    (partOfLayout * width).toInt()
            }
        }
        val scaleMultiplier = if (resultWidth <= width) 1f else width / resultWidth.toFloat()
        var layoutWidth = 0
        for (i in 0..childCount) {
            val child = getChildAt(i)
            child?.let {
                val lp = it.layoutParams as LayoutParams
                val partOfLayout = when (lp.width) {
                    MATCH_PARENT -> lp.weight / weightSum + (it.paddingRight + it.paddingLeft) / width.toFloat()
                    WRAP_CONTENT -> (it.paddingRight + it.paddingLeft + it.measuredWidth) / width.toFloat()
                    else -> (it.paddingRight + it.paddingLeft + lp.width) / width.toFloat() * lp.weight
                }
                var childWidth =
                    if (lp.weight == 0f)
                        when (lp.width) {
                            WRAP_CONTENT -> it.measuredWidth
                            MATCH_PARENT -> width
                            else -> lp.width
                        }
                    else
                        (partOfLayout * width).toInt()
                val childHeight = when (lp.height) {
                    WRAP_CONTENT -> it.measuredHeight
                    MATCH_PARENT -> height
                    else -> lp.height
                }
                childWidth = (childWidth * scaleMultiplier).toInt()
                val marginLeft = ((lp.leftMargin) * scaleMultiplier).toInt()
                val marginRight = ((lp.rightMargin) * scaleMultiplier).toInt()
                val marginTop = lp.topMargin
                val marginBottom = lp.bottomMargin
                layoutWidth += marginLeft
                it.layout(
                    layoutWidth,
                    marginTop,
                    layoutWidth + childWidth - marginLeft,
                    childHeight + marginBottom
                )
                layoutWidth += childWidth + marginRight
            }
        }
    }

    inner class LayoutParams : MarginLayoutParams {
        var weight = 0f

        @SuppressLint("CustomViewStyleable")
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            context.withStyledAttributes(attrs, R.styleable.StraightLayout) {
                weight = getFloat(R.styleable.StraightLayout_android_layout_weight, 0f)
            }
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(source: ViewGroup.LayoutParams) : super(source)
    }

    enum class Orientation {
        HORIZONTAL,
        VERTICAL
    }
}
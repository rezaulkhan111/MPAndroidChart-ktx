package com.github.mikephil.charting.renderer

import android.graphics.*
import android.graphics.Paint.Align
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.buffer.BarBuffer
import com.github.mikephil.charting.buffer.HorizontalBarBuffer
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.dataprovider.ChartInterface
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.*
import com.github.mikephil.charting.utils.MPPointF.Companion.getInstance
import com.github.mikephil.charting.utils.MPPointF.Companion.recycleInstance
import com.github.mikephil.charting.utils.Utils.calcTextHeight
import com.github.mikephil.charting.utils.Utils.calcTextWidth
import com.github.mikephil.charting.utils.Utils.convertDpToPixel
import com.github.mikephil.charting.utils.Utils.drawImage

/**
 * Renderer for the HorizontalBarChart.
 *
 * @author Philipp Jahoda
 */
class HorizontalBarChartRenderer : BarChartRenderer {

    constructor(
        chart: BarDataProvider, animator: ChartAnimator,
        viewPortHandler: ViewPortHandler
    ) : super(chart, animator, viewPortHandler) {
        mValuePaint!!.textAlign = Align.LEFT
    }

    override fun initBuffers() {
        val barData = mChart!!.getBarData()
        mBarBuffers = arrayOfNulls(barData!!.getDataSetCount())
        for (i in 0 until mBarBuffers!!.size) {
            val set = barData.getDataSetByIndex(i)
            mBarBuffers!![i] = HorizontalBarBuffer(
                set!!.getEntryCount() * 4 * if (set.isStacked()) set.getStackSize() else 1,
                barData.getDataSetCount(), set.isStacked()
            )
        }
    }

    private val mBarShadowRectBuffer = RectF()

    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet?, index: Int) {
        val trans = mChart!!.getTransformer(dataSet!!.getAxisDependency())!!
        mBarBorderPaint!!.color = dataSet.getBarBorderColor()
        mBarBorderPaint!!.strokeWidth = convertDpToPixel(dataSet.getBarBorderWidth())
        val drawBorder = dataSet.getBarBorderWidth() > 0f
        val phaseX = mAnimator!!.getPhaseX()
        val phaseY = mAnimator!!.getPhaseY()

        // draw the bar shadow before the values
        if (mChart!!.isDrawBarShadowEnabled()) {
            mShadowPaint!!.color = dataSet.getBarShadowColor()
            val barData = mChart!!.getBarData()
            val barWidth = barData!!.getBarWidth()
            val barWidthHalf = barWidth / 2.0f
            var x: Float
            var i = 0
            val count = Math.min(
                Math.ceil((dataSet.getEntryCount().toFloat() * phaseX).toDouble()).toInt(),
                dataSet.getEntryCount()
            )
            while (i < count) {
                val e = dataSet.getEntryForIndex(i)!!
                x = e.getX()
                mBarShadowRectBuffer.top = x - barWidthHalf
                mBarShadowRectBuffer.bottom = x + barWidthHalf
                trans.rectValueToPixel(mBarShadowRectBuffer)
                if (!mViewPortHandler!!.isInBoundsTop(mBarShadowRectBuffer.bottom)) {
                    i++
                    continue
                }
                if (!mViewPortHandler!!.isInBoundsBottom(mBarShadowRectBuffer.top)) break
                mBarShadowRectBuffer.left = mViewPortHandler!!.contentLeft()
                mBarShadowRectBuffer.right = mViewPortHandler!!.contentRight()
                c.drawRect(mBarShadowRectBuffer, mShadowPaint!!)
                i++
            }
        }

        // initialize the buffer
        val buffer = mBarBuffers!![index]
        buffer!!.setPhases(phaseX, phaseY)
        buffer.setDataSet(index)
        buffer.setInverted(mChart!!.isInverted(dataSet.getAxisDependency()))
        buffer.setBarWidth(mChart!!.getBarData()!!.getBarWidth())
        buffer.feed(dataSet)
        trans!!.pointValuesToPixel(buffer.buffer)
        val isCustomFill = dataSet.getFills() != null && dataSet.getFills()!!.isNotEmpty()
        val isSingleColor = dataSet.getColors()!!.size == 1
        val isInverted = mChart!!.isInverted(dataSet.getAxisDependency())
        if (isSingleColor) {
            mRenderPaint!!.color = dataSet.getColor()
        }
        var j = 0
        var pos = 0
        while (j < buffer.size()) {
            if (!mViewPortHandler!!.isInBoundsTop(buffer.buffer[j + 3])) break
            if (!mViewPortHandler!!.isInBoundsBottom(buffer.buffer[j + 1])) {
                j += 4
                pos++
                continue
            }
            if (!isSingleColor) {
                // Set the color for the currently drawn value. If the index
                // is out of bounds, reuse colors.
                mRenderPaint!!.color = dataSet.getColor(j / 4)
            }
            if (isCustomFill) {
                dataSet.getFill(pos)!!.fillRect(
                    c, mRenderPaint!!,
                    buffer.buffer[j],
                    buffer.buffer[j + 1],
                    buffer.buffer[j + 2],
                    buffer.buffer[j + 3],
                    if (isInverted) Fill.Direction.LEFT else Fill.Direction.RIGHT
                )
            } else {
                c.drawRect(
                    buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                    buffer.buffer[j + 3], mRenderPaint!!
                )
            }
            if (drawBorder) {
                c.drawRect(
                    buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                    buffer.buffer[j + 3], mBarBorderPaint!!
                )
            }
            j += 4
            pos++
        }
    }

    override fun drawValues(c: Canvas?) {
        // if values are drawn
        if (isDrawingValuesAllowed(mChart!!)) {
            val dataSets: MutableList<IBarDataSet?>? = mChart!!.getBarData()!!.getDataSets()
            val valueOffsetPlus = convertDpToPixel(5f)
            var posOffset = 0f
            var negOffset = 0f
            val drawValueAboveBar = mChart!!.isDrawValueAboveBarEnabled()
            for (i in 0 until mChart!!.getBarData()!!.getDataSetCount()) {
                val dataSet = dataSets!![i]
                if (!shouldDrawValues(dataSet!!)) continue
                val isInverted = mChart!!.isInverted(dataSet.getAxisDependency())

                // apply the text-styling defined by the DataSet
                applyValueTextStyle(dataSet)
                val halfTextHeight = calcTextHeight(
                    mValuePaint!!, "10"
                ) / 2f
                val formatter = dataSet.getValueFormatter()

                // get the buffer
                val buffer = mBarBuffers!![i]
                val phaseY = mAnimator!!.getPhaseY()
                val iconsOffset = getInstance(dataSet.getIconsOffset()!!)
                iconsOffset.x = convertDpToPixel(iconsOffset.x)
                iconsOffset.y = convertDpToPixel(iconsOffset.y)

                // if only single values are drawn (sum)
                if (!dataSet.isStacked()) {
                    var j = 0
                    while (j < buffer!!.buffer.size * mAnimator!!.getPhaseX()) {
                        val y = (buffer.buffer[j + 1] + buffer.buffer[j + 3]) / 2f
                        if (!mViewPortHandler!!.isInBoundsTop(buffer.buffer[j + 1])) break
                        if (!mViewPortHandler!!.isInBoundsX(buffer.buffer[j])) {
                            j += 4
                            continue
                        }
                        if (!mViewPortHandler!!.isInBoundsBottom(buffer.buffer[j + 1])) {
                            j += 4
                            continue
                        }
                        val entry = dataSet.getEntryForIndex(j / 4)!!
                        val `val` = entry.getY()
                        val formattedValue =
                            formatter?.getFormattedValue(`val`, entry, i, mViewPortHandler)

                        // calculate the correct offset depending on the draw position of the value
                        val valueTextWidth = calcTextWidth(
                            mValuePaint!!, formattedValue
                        ).toFloat()
                        posOffset =
                            if (drawValueAboveBar) valueOffsetPlus else -(valueTextWidth + valueOffsetPlus)
                        negOffset =
                            ((if (drawValueAboveBar) -(valueTextWidth + valueOffsetPlus) else valueOffsetPlus)
                                    - (buffer.buffer[j + 2] - buffer.buffer[j]))
                        if (isInverted) {
                            posOffset = -posOffset - valueTextWidth
                            negOffset = -negOffset - valueTextWidth
                        }
                        if (dataSet.isDrawValuesEnabled()) {
                            drawValue(
                                c!!,
                                formattedValue,
                                buffer.buffer[j + 2] + if (`val` >= 0) posOffset else negOffset,
                                y + halfTextHeight,
                                dataSet.getValueTextColor(j / 2)!!
                            )
                        }
                        if (entry.getIcon() != null && dataSet.isDrawIconsEnabled()) {
                            val icon = entry.getIcon()
                            var px = buffer.buffer[j + 2] + if (`val` >= 0) posOffset else negOffset
                            var py = y
                            px += iconsOffset.x
                            py += iconsOffset.y
                            drawImage(
                                c!!,
                                icon!!, px.toInt(), py.toInt(),
                                icon.intrinsicWidth,
                                icon.intrinsicHeight
                            )
                        }
                        j += 4
                    }

                    // if each value of a potential stack should be drawn
                } else {
                    val trans = mChart!!.getTransformer(dataSet.getAxisDependency())
                    var bufferIndex = 0
                    var index = 0
                    while (index < dataSet.getEntryCount() * mAnimator!!.getPhaseX()) {
                        val entry = dataSet.getEntryForIndex(index)!!
                        val color = dataSet.getValueTextColor(index)!!
                        val vals = entry.getYVals()

                        // we still draw stacked bars, but there is one
                        // non-stacked
                        // in between
                        if (vals == null) {
                            if (!mViewPortHandler!!.isInBoundsTop(buffer!!.buffer[bufferIndex + 1])) break
                            if (!mViewPortHandler!!.isInBoundsX(buffer.buffer[bufferIndex])) continue
                            if (!mViewPortHandler!!.isInBoundsBottom(buffer.buffer[bufferIndex + 1])) continue
//                            val `val` = entry.getY()
                            val formattedValue = formatter?.getFormattedValue(
                                entry.getY(),
                                entry, i, mViewPortHandler
                            )

                            // calculate the correct offset depending on the draw position of the value
                            val valueTextWidth = calcTextWidth(
                                mValuePaint!!, formattedValue
                            ).toFloat()
                            posOffset =
                                if (drawValueAboveBar) valueOffsetPlus else -(valueTextWidth + valueOffsetPlus)
                            negOffset =
                                if (drawValueAboveBar) -(valueTextWidth + valueOffsetPlus) else valueOffsetPlus
                            if (isInverted) {
                                posOffset = -posOffset - valueTextWidth
                                negOffset = -negOffset - valueTextWidth
                            }
                            if (dataSet.isDrawValuesEnabled()) {
                                drawValue(
                                    c!!, formattedValue, buffer.buffer[bufferIndex + 2]
                                            + if (entry.getY() >= 0) posOffset else negOffset,
                                    buffer.buffer[bufferIndex + 1] + halfTextHeight, color
                                )
                            }
                            if (entry.getIcon() != null && dataSet.isDrawIconsEnabled()) {
                                val icon = entry.getIcon()
                                var px = (buffer.buffer[bufferIndex + 2]
                                        + if (entry.getY() >= 0) posOffset else negOffset)
                                var py = buffer.buffer[bufferIndex + 1]
                                px += iconsOffset.x
                                py += iconsOffset.y
                                drawImage(
                                    c!!,
                                    icon!!, px.toInt(), py.toInt(),
                                    icon.intrinsicWidth,
                                    icon.intrinsicHeight
                                )
                            }
                        } else {
                            val transformed = FloatArray(vals.size * 2)
                            var posY = 0f
                            var negY = -entry.getNegativeSum()
                            run {
                                var k = 0
                                var idx = 0
                                while (k < transformed.size) {
                                    val value = vals[idx]
                                    var y: Float
                                    if (value == 0.0f && (posY == 0.0f || negY == 0.0f)) {
                                        // Take care of the situation of a 0.0 value, which overlaps a non-zero bar
                                        y = value
                                    } else if (value >= 0.0f) {
                                        posY += value
                                        y = posY
                                    } else {
                                        y = negY
                                        negY -= value
                                    }
                                    transformed[k] = y * phaseY
                                    k += 2
                                    idx++
                                }
                            }
                            trans?.pointValuesToPixel(transformed)
                            var k = 0
                            while (k < transformed.size) {
                                val valueK = vals[k / 2]
                                val formattedValue = formatter?.getFormattedValue(
                                    valueK,
                                    entry, i, mViewPortHandler
                                )

                                // calculate the correct offset depending on the draw position of the value
                                val valueTextWidth = calcTextWidth(
                                    mValuePaint!!, formattedValue
                                ).toFloat()
                                posOffset =
                                    if (drawValueAboveBar) valueOffsetPlus else -(valueTextWidth + valueOffsetPlus)
                                negOffset =
                                    if (drawValueAboveBar) -(valueTextWidth + valueOffsetPlus) else valueOffsetPlus
                                if (isInverted) {
                                    posOffset = -posOffset - valueTextWidth
                                    negOffset = -negOffset - valueTextWidth
                                }
                                val drawBelow = valueK == 0.0f && negY == 0.0f && posY > 0.0f ||
                                        valueK < 0.0f
                                val x = (transformed[k]
                                        + if (drawBelow) negOffset else posOffset)
                                val y =
                                    (buffer!!.buffer[bufferIndex + 1] + buffer.buffer[bufferIndex + 3]) / 2f
                                if (!mViewPortHandler!!.isInBoundsTop(y)) break
                                if (!mViewPortHandler!!.isInBoundsX(x)) {
                                    k += 2
                                    continue
                                }
                                if (!mViewPortHandler!!.isInBoundsBottom(y)) {
                                    k += 2
                                    continue
                                }
                                if (dataSet.isDrawValuesEnabled()) {
                                    drawValue(c!!, formattedValue, x, y + halfTextHeight, color)
                                }
                                if (entry.getIcon() != null && dataSet.isDrawIconsEnabled()) {
                                    val icon = entry.getIcon()
                                    drawImage(
                                        c!!,
                                        icon!!,
                                        (x + iconsOffset.x).toInt(),
                                        (y + iconsOffset.y).toInt(),
                                        icon.intrinsicWidth,
                                        icon.intrinsicHeight
                                    )
                                }
                                k += 2
                            }
                        }
                        bufferIndex =
                            if (vals == null) bufferIndex + 4 else bufferIndex + 4 * vals.size
                        index++
                    }
                }
                recycleInstance(iconsOffset)
            }
        }
    }

    protected fun drawValue(c: Canvas, valueText: String?, x: Float, y: Float, color: Int) {
        mValuePaint!!.color = color
        c.drawText(valueText!!, x, y, mValuePaint!!)
    }

    override fun prepareBarHighlight(
        x: Float,
        y1: Float,
        y2: Float,
        barWidthHalf: Float,
        trans: Transformer
    ) {
        val top = x - barWidthHalf
        val bottom = x + barWidthHalf
        mBarRect[y1, top, y2] = bottom
        trans.rectToPixelPhaseHorizontal(mBarRect, mAnimator!!.getPhaseY())
    }

    override fun setHighlightDrawPos(high: Highlight, bar: RectF) {
        high.setDraw(bar.centerY(), bar.right)
    }

    override fun isDrawingValuesAllowed(chart: ChartInterface): Boolean {
        return chart.getData()!!
            .getEntryCount() < chart.getMaxVisibleCount() * mViewPortHandler!!.getScaleY()
    }
}
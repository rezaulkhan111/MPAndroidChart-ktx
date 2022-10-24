package com.github.mikephil.charting.components

import android.content.Context
import android.graphics.Canvas
import com.github.mikephil.charting.utils.Utils.convertDpToPixel
import com.github.mikephil.charting.utils.Utils.calcTextWidth
import com.github.mikephil.charting.utils.Utils.calcTextHeight
import com.github.mikephil.charting.utils.Utils.getLineHeight
import com.github.mikephil.charting.utils.Utils.getLineSpacing
import com.github.mikephil.charting.utils.ViewPortHandler.contentWidth
import com.github.mikephil.charting.utils.Utils.calcTextSize
import com.github.mikephil.charting.utils.FSize.Companion.getInstance
import com.github.mikephil.charting.formatter.IAxisValueFormatter.getFormattedValue
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter.decimalDigits
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.components.ComponentBase
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment
import com.github.mikephil.charting.components.Legend.LegendVerticalAlignment
import com.github.mikephil.charting.components.Legend.LegendOrientation
import com.github.mikephil.charting.components.Legend.LegendDirection
import com.github.mikephil.charting.components.Legend.LegendForm
import android.graphics.DashPathEffect
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.FSize
import com.github.mikephil.charting.utils.ViewPortHandler
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition
import android.widget.RelativeLayout
import com.github.mikephil.charting.components.IMarker
import com.github.mikephil.charting.charts.Chart
import android.view.LayoutInflater
import android.view.View.MeasureSpec
import android.graphics.Paint.Align
import android.graphics.drawable.Drawable
import android.os.Build
import android.graphics.Typeface
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import java.lang.ref.WeakReference

/**
 * View that can be displayed when selecting values in the chart. Extend this class to provide custom layouts for your
 * markers.
 *
 * @author Philipp Jahoda
 */
open class MarkerView(context: Context?, layoutResource: Int) : RelativeLayout(context), IMarker {
    private var mOffset: MPPointF? = MPPointF()
    private val mOffset2 = MPPointF()
    private var mWeakChart: WeakReference<Chart<*>>? = null

    /**
     * Sets the layout resource for a custom MarkerView.
     *
     * @param layoutResource
     */
    private fun setupLayoutResource(layoutResource: Int) {
        val inflated = LayoutInflater.from(context).inflate(layoutResource, this)
        inflated.layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        inflated.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )

        // measure(getWidth(), getHeight());
        inflated.layout(0, 0, inflated.measuredWidth, inflated.measuredHeight)
    }

    fun setOffset(offsetX: Float, offsetY: Float) {
        mOffset!!.x = offsetX
        mOffset!!.y = offsetY
    }

    override var offset: MPPointF?
        get() = mOffset
        set(offset) {
            mOffset = offset
            if (mOffset == null) {
                mOffset = MPPointF()
            }
        }

    fun setChartView(chart: Chart<*>) {
        mWeakChart = WeakReference(chart)
    }

    val chartView: Chart<*>?
        get() = if (mWeakChart == null) null else mWeakChart!!.get()

    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
        val offset = offset
        mOffset2.x = offset!!.x
        mOffset2.y = offset.y
        val chart = chartView
        val width = width.toFloat()
        val height = height.toFloat()
        if (posX + mOffset2.x < 0) {
            mOffset2.x = -posX
        } else if (chart != null && posX + width + mOffset2.x > chart.getWidth()) {
            mOffset2.x = chart.getWidth() - posX - width
        }
        if (posY + mOffset2.y < 0) {
            mOffset2.y = -posY
        } else if (chart != null && posY + height + mOffset2.y > chart.getHeight()) {
            mOffset2.y = chart.getHeight() - posY - height
        }
        return mOffset2
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        layout(0, 0, measuredWidth, measuredHeight)
    }

    override fun draw(canvas: Canvas, posX: Float, posY: Float) {
        val offset = getOffsetForDrawingAtPoint(posX, posY)
        val saveId = canvas.save()
        // translate to the correct position and draw
        canvas.translate(posX + offset.x, posY + offset.y)
        draw(canvas)
        canvas.restoreToCount(saveId)
    }

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    init {
        setupLayoutResource(layoutResource)
    }
}
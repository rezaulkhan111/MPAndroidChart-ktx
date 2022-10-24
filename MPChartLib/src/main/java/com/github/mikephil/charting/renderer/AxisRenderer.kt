package com.github.mikephil.charting.renderer

import android.graphics.*
import com.github.mikephil.charting.utils.Utils.convertDpToPixel
import com.github.mikephil.charting.utils.ViewPortHandler.contentWidth
import com.github.mikephil.charting.utils.ViewPortHandler.isFullyZoomedOutY
import com.github.mikephil.charting.utils.Transformer.getValuesByTouchPoint
import com.github.mikephil.charting.utils.ViewPortHandler.contentLeft
import com.github.mikephil.charting.utils.ViewPortHandler.contentTop
import com.github.mikephil.charting.utils.ViewPortHandler.contentBottom
import com.github.mikephil.charting.utils.MPPointD.Companion.recycleInstance
import com.github.mikephil.charting.utils.Utils.roundToNextSignificant
import com.github.mikephil.charting.utils.Utils.nextUp
import com.github.mikephil.charting.utils.ViewPortHandler.scaleX
import com.github.mikephil.charting.utils.ViewPortHandler.isFullyZoomedOutX
import com.github.mikephil.charting.utils.ViewPortHandler.contentRight
import com.github.mikephil.charting.utils.Utils.calcTextSize
import com.github.mikephil.charting.utils.Utils.calcTextHeight
import com.github.mikephil.charting.utils.Utils.getSizeOfRotatedRectangleByDegrees
import com.github.mikephil.charting.utils.FSize.Companion.recycleInstance
import com.github.mikephil.charting.utils.MPPointF.Companion.recycleInstance
import com.github.mikephil.charting.utils.Transformer.pointValuesToPixel
import com.github.mikephil.charting.utils.ViewPortHandler.isInBoundsX
import com.github.mikephil.charting.utils.Utils.calcTextWidth
import com.github.mikephil.charting.utils.ViewPortHandler.offsetRight
import com.github.mikephil.charting.utils.ViewPortHandler.chartWidth
import com.github.mikephil.charting.utils.Utils.drawXAxisValue
import com.github.mikephil.charting.utils.ViewPortHandler.contentRect
import com.github.mikephil.charting.utils.ViewPortHandler.offsetLeft
import com.github.mikephil.charting.utils.Transformer.getPixelForValues
import com.github.mikephil.charting.utils.Utils.getLineHeight
import com.github.mikephil.charting.utils.Utils.getLineSpacing
import com.github.mikephil.charting.utils.ViewPortHandler.chartHeight
import com.github.mikephil.charting.utils.Transformer.rectValueToPixel
import com.github.mikephil.charting.utils.ViewPortHandler.isInBoundsLeft
import com.github.mikephil.charting.utils.ViewPortHandler.isInBoundsRight
import com.github.mikephil.charting.utils.Fill.fillRect
import com.github.mikephil.charting.utils.Transformer.rectToPixelPhase
import com.github.mikephil.charting.utils.ViewPortHandler.isInBoundsY
import com.github.mikephil.charting.utils.Utils.drawImage
import com.github.mikephil.charting.utils.ViewPortHandler.smallestContentExtension
import com.github.mikephil.charting.utils.Transformer.pathValueToPixel
import com.github.mikephil.charting.utils.ViewPortHandler.isInBoundsTop
import com.github.mikephil.charting.utils.ViewPortHandler.isInBoundsBottom
import com.github.mikephil.charting.utils.Transformer.generateTransformedValuesLine
import com.github.mikephil.charting.utils.Utils.sDKInt
import com.github.mikephil.charting.utils.Utils.getPosition
import com.github.mikephil.charting.utils.ColorTemplate.colorWithAlpha
import com.github.mikephil.charting.utils.Transformer.generateTransformedValuesBubble
import com.github.mikephil.charting.utils.Transformer.generateTransformedValuesScatter
import com.github.mikephil.charting.utils.Transformer.generateTransformedValuesCandle
import com.github.mikephil.charting.utils.Transformer.rectToPixelPhaseHorizontal
import com.github.mikephil.charting.utils.ViewPortHandler.scaleY
import com.github.mikephil.charting.utils.ViewPortHandler.contentHeight
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.github.mikephil.charting.renderer.scatter.IShapeRenderer
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.interfaces.dataprovider.ChartInterface
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.formatter.IValueFormatter
import android.graphics.Paint.Align
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.renderer.AxisRenderer
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet
import com.github.mikephil.charting.components.Legend.LegendOrientation
import com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment
import com.github.mikephil.charting.components.Legend.LegendVerticalAlignment
import com.github.mikephil.charting.components.Legend.LegendDirection
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.renderer.BarLineScatterCandleBubbleRenderer
import com.github.mikephil.charting.buffer.BarBuffer
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarEntry
import android.graphics.drawable.Drawable
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.renderer.DataRenderer
import android.text.TextPaint
import android.text.StaticLayout
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet.ValuePosition
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.PieDataSet
import android.os.Build
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.renderer.LineRadarRenderer
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.renderer.BarLineScatterCandleBubbleRenderer.XBounds
import com.github.mikephil.charting.renderer.LineChartRenderer.DataSetImageCache
import com.github.mikephil.charting.renderer.LineScatterCandleRadarRenderer
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.interfaces.dataprovider.BubbleDataProvider
import com.github.mikephil.charting.data.BubbleData
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet
import com.github.mikephil.charting.data.BubbleEntry
import com.github.mikephil.charting.interfaces.dataprovider.ScatterDataProvider
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.renderer.BubbleChartRenderer
import com.github.mikephil.charting.renderer.LineChartRenderer
import com.github.mikephil.charting.renderer.CandleStickChartRenderer
import com.github.mikephil.charting.renderer.ScatterChartRenderer
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.renderer.XAxisRenderer
import com.github.mikephil.charting.renderer.YAxisRenderer
import com.github.mikephil.charting.interfaces.dataprovider.CandleDataProvider
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.buffer.HorizontalBarBuffer
import com.github.mikephil.charting.interfaces.datasets.ILineScatterCandleRadarDataSet
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import com.github.mikephil.charting.interfaces.dataprovider.BarLineScatterCandleBubbleDataProvider
import com.github.mikephil.charting.data.DataSet
import com.github.mikephil.charting.utils.*

/**
 * Baseclass of all axis renderers.
 *
 * @author Philipp Jahoda
 */
abstract class AxisRenderer(
    viewPortHandler: ViewPortHandler?,
    /** transformer to transform values to screen pixels and return  */
    var transformer: Transformer?,
    /** base axis this axis renderer works with  */
    protected var mAxis: AxisBase
) : Renderer(viewPortHandler) {
    /**
     * Returns the Transformer object used for transforming the axis values.
     *
     * @return
     */
    /**
     * Returns the Paint object that is used for drawing the grid-lines of the
     * axis.
     *
     * @return
     */
    /**
     * paint object for the grid lines
     */
    var paintGrid: Paint? = null
        protected set
    /**
     * Returns the Paint object used for drawing the axis (labels).
     *
     * @return
     */
    /**
     * paint for the x-label values
     */
    var paintAxisLabels: Paint? = null
        protected set
    /**
     * Returns the Paint object that is used for drawing the axis-line that goes
     * alongside the axis.
     *
     * @return
     */
    /**
     * paint for the line surrounding the chart
     */
    var paintAxisLine: Paint? = null
        protected set

    /**
     * paint used for the limit lines
     */
    protected var mLimitLinePaint: Paint? = null

    /**
     * Computes the axis values.
     *
     * @param min - the minimum value in the data object for this axis
     * @param max - the maximum value in the data object for this axis
     */
    open fun computeAxis(min: Float, max: Float, inverted: Boolean) {

        // calculate the starting and entry point of the y-labels (depending on
        // zoom / contentrect bounds)
        var min = min
        var max = max
        if (mViewPortHandler != null && mViewPortHandler.contentWidth() > 10 && !mViewPortHandler.isFullyZoomedOutY) {
            val p1 = transformer!!.getValuesByTouchPoint(
                mViewPortHandler.contentLeft(),
                mViewPortHandler.contentTop()
            )
            val p2 = transformer!!.getValuesByTouchPoint(
                mViewPortHandler.contentLeft(),
                mViewPortHandler.contentBottom()
            )
            if (!inverted) {
                min = p2.y.toFloat()
                max = p1.y.toFloat()
            } else {
                min = p1.y.toFloat()
                max = p2.y.toFloat()
            }
            recycleInstance(p1)
            recycleInstance(p2)
        }
        computeAxisValues(min, max)
    }

    /**
     * Sets up the axis values. Computes the desired number of labels between the two given extremes.
     *
     * @return
     */
    protected open fun computeAxisValues(min: Float, max: Float) {
        val labelCount = mAxis.labelCount
        val range = Math.abs(max - min).toDouble()
        if (labelCount == 0 || range <= 0 || java.lang.Double.isInfinite(range)) {
            mAxis.mEntries = floatArrayOf()
            mAxis.mCenteredEntries = floatArrayOf()
            mAxis.mEntryCount = 0
            return
        }

        // Find out how much spacing (in y value space) between axis values
        val rawInterval = range / labelCount
        var interval = roundToNextSignificant(rawInterval).toDouble()

        // If granularity is enabled, then do not allow the interval to go below specified granularity.
        // This is used to avoid repeated values when rounding values for display.
        if (mAxis.isGranularityEnabled) interval =
            if (interval < mAxis.granularity) mAxis.granularity
                .toDouble() else interval

        // Normalize interval
        val intervalMagnitude = roundToNextSignificant(
            Math.pow(
                10.0,
                Math.log10(interval).toInt().toDouble()
            )
        ).toDouble()
        val intervalSigDigit = (interval / intervalMagnitude).toInt()
        if (intervalSigDigit > 5) {
            // Use one order of magnitude higher, to avoid intervals like 0.9 or 90
            // if it's 0.0 after floor(), we use the old value
            interval =
                if (Math.floor(10.0 * intervalMagnitude) == 0.0) interval else Math.floor(10.0 * intervalMagnitude)
        }
        var n = if (mAxis.isCenterAxisLabelsEnabled) 1 else 0

        // force label count
        if (mAxis.isForceLabelsEnabled) {
            interval = (range.toFloat() / (labelCount - 1).toFloat()).toDouble()
            mAxis.mEntryCount = labelCount
            if (mAxis.mEntries.size < labelCount) {
                // Ensure stops contains at least numStops elements.
                mAxis.mEntries = FloatArray(labelCount)
            }
            var v = min
            for (i in 0 until labelCount) {
                mAxis.mEntries[i] = v
                v += interval.toFloat()
            }
            n = labelCount

            // no forced count
        } else {
            var first = if (interval == 0.0) 0.0 else Math.ceil(min / interval) * interval
            if (mAxis.isCenterAxisLabelsEnabled) {
                first -= interval
            }
            val last = if (interval == 0.0) 0.0 else nextUp(
                Math.floor(
                    max / interval
                ) * interval
            )
            var f: Double
            var i: Int
            if (interval != 0.0 && last != first) {
                f = first
                while (f <= last) {
                    ++n
                    f += interval
                }
            } else if (last == first && n == 0) {
                n = 1
            }
            mAxis.mEntryCount = n
            if (mAxis.mEntries.size < n) {
                // Ensure stops contains at least numStops elements.
                mAxis.mEntries = FloatArray(n)
            }
            f = first
            i = 0
            while (i < n) {
                if (f == 0.0) // Fix for negative zero case (Where value == -0.0, and 0.0 == -0.0)
                    f = 0.0
                mAxis.mEntries[i] = f.toFloat()
                f += interval
                ++i
            }
        }

        // set decimals
        if (interval < 1) {
            mAxis.mDecimals = Math.ceil(-Math.log10(interval)).toInt()
        } else {
            mAxis.mDecimals = 0
        }
        if (mAxis.isCenterAxisLabelsEnabled) {
            if (mAxis.mCenteredEntries.size < n) {
                mAxis.mCenteredEntries = FloatArray(n)
            }
            val offset = interval.toFloat() / 2f
            for (i in 0 until n) {
                mAxis.mCenteredEntries[i] = mAxis.mEntries[i] + offset
            }
        }
    }

    /**
     * Draws the axis labels to the screen.
     *
     * @param c
     */
    abstract fun renderAxisLabels(c: Canvas)

    /**
     * Draws the grid lines belonging to the axis.
     *
     * @param c
     */
    abstract fun renderGridLines(c: Canvas)

    /**
     * Draws the line that goes alongside the axis.
     *
     * @param c
     */
    abstract fun renderAxisLine(c: Canvas)

    /**
     * Draws the LimitLines associated with this axis to the screen.
     *
     * @param c
     */
    abstract fun renderLimitLines(c: Canvas)

    init {
        if (mViewPortHandler != null) {
            paintAxisLabels = Paint(Paint.ANTI_ALIAS_FLAG)
            paintGrid = Paint()
            paintGrid!!.color = Color.GRAY
            paintGrid!!.strokeWidth = 1f
            paintGrid!!.style = Paint.Style.STROKE
            paintGrid!!.alpha = 90
            paintAxisLine = Paint()
            paintAxisLine!!.color = Color.BLACK
            paintAxisLine!!.strokeWidth = 1f
            paintAxisLine!!.style = Paint.Style.STROKE
            mLimitLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mLimitLinePaint!!.style = Paint.Style.STROKE
        }
    }
}
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
import com.github.mikephil.charting.utils.ViewPortHandler
import com.github.mikephil.charting.renderer.scatter.IShapeRenderer
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.utils.MPPointD
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.interfaces.dataprovider.ChartInterface
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.formatter.IValueFormatter
import android.graphics.Paint.Align
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.renderer.AxisRenderer
import com.github.mikephil.charting.utils.FSize
import com.github.mikephil.charting.utils.MPPointF
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
import com.github.mikephil.charting.utils.Fill
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
import java.lang.RuntimeException

/**
 * Created by Philipp Jahoda on 25/01/16.
 */
abstract class LineRadarRenderer(animator: ChartAnimator?, viewPortHandler: ViewPortHandler?) :
    LineScatterCandleRadarRenderer(animator, viewPortHandler) {
    /**
     * Draws the provided path in filled mode with the provided drawable.
     *
     * @param c
     * @param filledPath
     * @param drawable
     */
    protected fun drawFilledPath(c: Canvas, filledPath: Path?, drawable: Drawable) {
        if (clipPathSupported()) {
            val save = c.save()
            c.clipPath(filledPath!!)
            drawable.setBounds(
                mViewPortHandler.contentLeft().toInt(),
                mViewPortHandler.contentTop().toInt(),
                mViewPortHandler.contentRight().toInt(),
                mViewPortHandler.contentBottom().toInt()
            )
            drawable.draw(c)
            c.restoreToCount(save)
        } else {
            throw RuntimeException(
                "Fill-drawables not (yet) supported below API level 18, " +
                        "this code was run on API level " + sDKInt + "."
            )
        }
    }

    /**
     * Draws the provided path in filled mode with the provided color and alpha.
     * Special thanks to Angelo Suzuki (https://github.com/tinsukE) for this.
     *
     * @param c
     * @param filledPath
     * @param fillColor
     * @param fillAlpha
     */
    protected fun drawFilledPath(c: Canvas, filledPath: Path?, fillColor: Int, fillAlpha: Int) {
        val color = fillAlpha shl 24 or (fillColor and 0xffffff)
        if (clipPathSupported()) {
            val save = c.save()
            c.clipPath(filledPath!!)
            c.drawColor(color)
            c.restoreToCount(save)
        } else {

            // save
            val previous = mRenderPaint.style
            val previousColor = mRenderPaint.color

            // set
            mRenderPaint.style = Paint.Style.FILL
            mRenderPaint.color = color
            c.drawPath(filledPath!!, mRenderPaint)

            // restore
            mRenderPaint.color = previousColor
            mRenderPaint.style = previous
        }
    }

    /**
     * Clip path with hardware acceleration only working properly on API level 18 and above.
     *
     * @return
     */
    private fun clipPathSupported(): Boolean {
        return sDKInt >= 18
    }
}
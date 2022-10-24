package com.github.mikephil.charting.data

import com.github.mikephil.charting.interfaces.datasets.IDataSet.entryCount
import com.github.mikephil.charting.interfaces.datasets.IDataSet.getEntryForIndex
import com.github.mikephil.charting.interfaces.datasets.IDataSet.label
import com.github.mikephil.charting.highlight.Highlight.x
import com.github.mikephil.charting.interfaces.datasets.IDataSet.calcMinMaxY
import com.github.mikephil.charting.interfaces.datasets.IDataSet.yMax
import com.github.mikephil.charting.interfaces.datasets.IDataSet.yMin
import com.github.mikephil.charting.interfaces.datasets.IDataSet.axisDependency
import com.github.mikephil.charting.highlight.Highlight.dataSetIndex
import com.github.mikephil.charting.interfaces.datasets.IDataSet.getEntryForXValue
import com.github.mikephil.charting.highlight.Highlight.y
import com.github.mikephil.charting.interfaces.datasets.IDataSet.addEntry
import com.github.mikephil.charting.interfaces.datasets.IDataSet.xMax
import com.github.mikephil.charting.interfaces.datasets.IDataSet.xMin
import com.github.mikephil.charting.interfaces.datasets.IDataSet.removeEntry
import com.github.mikephil.charting.interfaces.datasets.IDataSet.colors
import com.github.mikephil.charting.interfaces.datasets.IDataSet.valueFormatter
import com.github.mikephil.charting.interfaces.datasets.IDataSet.valueTextColor
import com.github.mikephil.charting.interfaces.datasets.IDataSet.setValueTextColors
import com.github.mikephil.charting.interfaces.datasets.IDataSet.valueTypeface
import com.github.mikephil.charting.interfaces.datasets.IDataSet.valueTextSize
import com.github.mikephil.charting.interfaces.datasets.IDataSet.setDrawValues
import com.github.mikephil.charting.interfaces.datasets.IDataSet.isHighlightEnabled
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet.highlightCircleWidth
import com.github.mikephil.charting.utils.Utils.convertDpToPixel
import com.github.mikephil.charting.interfaces.datasets.IDataSet.calcMinMax
import com.github.mikephil.charting.utils.ColorTemplate.createColors
import com.github.mikephil.charting.utils.Utils.defaultValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet.scatterShapeSize
import com.github.mikephil.charting.highlight.Highlight.dataIndex
import com.github.mikephil.charting.interfaces.datasets.IDataSet.getEntriesForXValue
import android.annotation.TargetApi
import android.os.Build
import com.github.mikephil.charting.data.filter.ApproximatorN
import android.os.Parcelable
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.ParcelFormatException
import android.os.Parcelable.Creator
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BaseDataSet
import com.github.mikephil.charting.data.DataSet.Rounding
import com.github.mikephil.charting.data.DataSet
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet
import android.annotation.SuppressLint
import android.graphics.Color
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.formatter.IValueFormatter
import android.graphics.Typeface
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleDataSet
import com.github.mikephil.charting.utils.Fill
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet
import com.github.mikephil.charting.data.PieDataSet.ValuePosition
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.Legend
import android.graphics.DashPathEffect
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.data.BubbleEntry
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.LineRadarDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.DefaultFillFormatter
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.BubbleData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.BubbleDataSet
import com.github.mikephil.charting.data.LineScatterCandleRadarDataSet
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.charts.ScatterChart.ScatterShape
import com.github.mikephil.charting.renderer.scatter.TriangleShapeRenderer
import com.github.mikephil.charting.interfaces.datasets.ILineRadarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineScatterCandleRadarDataSet
import java.util.ArrayList

class RadarDataSet(yVals: MutableList<RadarEntry?>?, label: String?) :
    LineRadarDataSet<RadarEntry?>(yVals, label), IRadarDataSet {
    /// flag indicating whether highlight circle should be drawn or not
    protected var mDrawHighlightCircleEnabled = false
    protected var mHighlightCircleFillColor = Color.WHITE

    /// The stroke color for highlight circle.
    /// If Utils.COLOR_NONE, the color of the dataset is taken.
    protected var mHighlightCircleStrokeColor = ColorTemplate.COLOR_NONE
    protected var mHighlightCircleStrokeAlpha = (0.3 * 255).toInt()
    protected var mHighlightCircleInnerRadius = 3.0f
    protected var mHighlightCircleOuterRadius = 4.0f
    protected var mHighlightCircleStrokeWidth = 2.0f

    /// Returns true if highlight circle should be drawn, false if not
    override fun isDrawHighlightCircleEnabled(): Boolean {
        return mDrawHighlightCircleEnabled
    }

    /// Sets whether highlight circle should be drawn or not
    override fun setDrawHighlightCircleEnabled(enabled: Boolean) {
        mDrawHighlightCircleEnabled = enabled
    }

    override fun getHighlightCircleFillColor(): Int {
        return mHighlightCircleFillColor
    }

    fun setHighlightCircleFillColor(color: Int) {
        mHighlightCircleFillColor = color
    }

    /// Returns the stroke color for highlight circle.
    /// If Utils.COLOR_NONE, the color of the dataset is taken.
    override fun getHighlightCircleStrokeColor(): Int {
        return mHighlightCircleStrokeColor
    }

    /// Sets the stroke color for highlight circle.
    /// Set to Utils.COLOR_NONE in order to use the color of the dataset;
    fun setHighlightCircleStrokeColor(color: Int) {
        mHighlightCircleStrokeColor = color
    }

    override fun getHighlightCircleStrokeAlpha(): Int {
        return mHighlightCircleStrokeAlpha
    }

    fun setHighlightCircleStrokeAlpha(alpha: Int) {
        mHighlightCircleStrokeAlpha = alpha
    }

    override fun getHighlightCircleInnerRadius(): Float {
        return mHighlightCircleInnerRadius
    }

    fun setHighlightCircleInnerRadius(radius: Float) {
        mHighlightCircleInnerRadius = radius
    }

    override fun getHighlightCircleOuterRadius(): Float {
        return mHighlightCircleOuterRadius
    }

    fun setHighlightCircleOuterRadius(radius: Float) {
        mHighlightCircleOuterRadius = radius
    }

    override fun getHighlightCircleStrokeWidth(): Float {
        return mHighlightCircleStrokeWidth
    }

    fun setHighlightCircleStrokeWidth(strokeWidth: Float) {
        mHighlightCircleStrokeWidth = strokeWidth
    }

    override fun copy(): DataSet<RadarEntry?>? {
        val entries: MutableList<RadarEntry?> = ArrayList()
        for (i in mEntries!!.indices) {
            entries.add(mEntries!![i]!!.copy())
        }
        val copied = RadarDataSet(entries, getLabel())
        copy(copied)
        return copied
    }

    protected fun copy(radarDataSet: RadarDataSet) {
        super.copy(radarDataSet)
        radarDataSet.mDrawHighlightCircleEnabled = mDrawHighlightCircleEnabled
        radarDataSet.mHighlightCircleFillColor = mHighlightCircleFillColor
        radarDataSet.mHighlightCircleInnerRadius = mHighlightCircleInnerRadius
        radarDataSet.mHighlightCircleStrokeAlpha = mHighlightCircleStrokeAlpha
        radarDataSet.mHighlightCircleStrokeColor = mHighlightCircleStrokeColor
        radarDataSet.mHighlightCircleStrokeWidth = mHighlightCircleStrokeWidth
    }
}
package com.github.mikephil.charting.listener

import com.github.mikephil.charting.utils.Utils.convertDpToPixel
import com.github.mikephil.charting.utils.Utils.maximumFlingVelocity
import com.github.mikephil.charting.utils.Utils.minimumFlingVelocity
import com.github.mikephil.charting.utils.Utils.postInvalidateOnAnimation
import com.github.mikephil.charting.utils.Utils.velocityTrackerPointerUpCleanUpIfNecessary
import com.github.mikephil.charting.utils.ViewPortHandler.refresh
import com.github.mikephil.charting.utils.ViewPortHandler.canZoomOutMoreX
import com.github.mikephil.charting.utils.ViewPortHandler.canZoomInMoreX
import com.github.mikephil.charting.utils.ViewPortHandler.canZoomOutMoreY
import com.github.mikephil.charting.utils.ViewPortHandler.canZoomInMoreY
import com.github.mikephil.charting.utils.MPPointF.Companion.recycleInstance
import com.github.mikephil.charting.utils.ViewPortHandler.offsetLeft
import com.github.mikephil.charting.utils.ViewPortHandler.offsetTop
import com.github.mikephil.charting.utils.ViewPortHandler.offsetBottom
import com.github.mikephil.charting.data.DataSet
import com.github.mikephil.charting.charts.Chart
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture
import com.github.mikephil.charting.listener.ChartTouchListener
import android.view.GestureDetector
import android.view.MotionEvent
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import android.view.VelocityTracker
import android.annotation.SuppressLint
import com.github.mikephil.charting.listener.BarLineChartTouchListener
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.utils.ViewPortHandler
import com.github.mikephil.charting.charts.PieRadarChartBase
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.PieRadarChartTouchListener.AngularVelocitySample

/**
 * Listener for callbacks when selecting values inside the chart by
 * touch-gesture.
 *
 * @author Philipp Jahoda
 */
interface OnChartValueSelectedListener {
    /**
     * Called when a value has been selected inside the chart.
     *
     * @param e The selected Entry
     * @param h The corresponding highlight object that contains information
     * about the highlighted position such as dataSetIndex, ...
     */
    fun onValueSelected(e: Entry?, h: Highlight?)

    /**
     * Called when nothing has been selected or an "un-select" has been made.
     */
    fun onNothingSelected()
}
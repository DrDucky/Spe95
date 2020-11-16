package com.pomplarg.spe95.utils

import android.content.Context
import android.graphics.Color
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.pomplarg.spe95.R
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

fun configureChart(chart: PieChart, context: Context?) {
    chart.description.isEnabled = false
    chart.setNoDataText(context?.getString(R.string.statistiques_no_data))
    chart.setExtraOffsets(20f, 0f, 20f, 0f)
    chart.dragDecelerationFrictionCoef = 0.95f
    chart.setEntryLabelColor(Color.BLACK)
    chart.isDrawHoleEnabled = true
    chart.setTransparentCircleColor(Color.WHITE)
    chart.setTransparentCircleAlpha(110)
    chart.holeRadius = 58f
    chart.transparentCircleRadius = 61f
    chart.setDrawCenterText(true)
    chart.rotationAngle = 0f
    chart.isRotationEnabled = false
    chart.isHighlightPerTapEnabled = true
    chart.animateY(1400, Easing.EaseInOutQuad)
    chart.legend.isEnabled = false
}

fun setDataToChart(stats: HashMap<String?, Long?>?, chart: PieChart, title: String) {
    var totalCount = 0L
    val entries = ArrayList<PieEntry>()
    stats?.forEach {
        entries.add(PieEntry(it.value!!.toFloat(), it.key))
        totalCount += it.value!!
    }
    //ColorTemplate.VORDIPLOM_COLORS
    val colors = arrayListOf<Int>(
        Color.rgb(255, 208, 140),
        Color.rgb(140, 234, 255),
        Color.rgb(255, 140, 157),
        Color.rgb(192, 255, 140),
        Color.rgb(255, 247, 140)
    )
    val dataset = PieDataSet(entries, "Motifs")
    dataset.colors = colors
    dataset.valueTextSize = 16f
    dataset.valueFormatter = vf
    val pieData = PieData(dataset)
    chart.data = pieData
    chart.centerText = "$title\nTotal $totalCount"
    chart.invalidate()
}

var vf: ValueFormatter = object : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return "" + value.roundToInt()
    }
}
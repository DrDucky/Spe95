package com.pomplarg.spe95.utils

import android.content.Context
import android.graphics.Color
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.pomplarg.spe95.R
import java.text.DateFormatSymbols
import java.util.*
import kotlin.collections.ArrayList

fun configurePieChart(chart: PieChart, context: Context?) {
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
    chart.legend.isEnabled = true
    chart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
    chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
    chart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
    chart.legend.setDrawInside(false)
    chart.setDrawEntryLabels(false)
    chart.description.isEnabled = false
    chart.legend.isWordWrapEnabled = true
}

fun configureBarChart(chart: BarChart, context: Context?, hourAndTimeFormat: Boolean) {
    chart.description.isEnabled = false
    chart.setNoDataText(context?.getString(R.string.statistiques_no_data))
    chart.setExtraOffsets(20f, 0f, 20f, 0f)
    chart.dragDecelerationFrictionCoef = 0.95f
    chart.isHighlightPerTapEnabled = true
    chart.animateY(1400, Easing.EaseInOutQuad)
    chart.setFitBars(true)
    chart.setDrawBarShadow(false)
    chart.setDrawGridBackground(false)
    chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
    chart.xAxis.setCenterAxisLabels(true);
    chart.xAxis.setAvoidFirstLastClipping(true)
    chart.xAxis.setLabelCount(13, true)
    chart.xAxis.axisMinimum = 1f
    chart.xAxis.axisMaximum = 13f
    chart.axisLeft.axisMinimum = 0f
    chart.axisLeft.setDrawGridLines(false)
    chart.axisLeft.spaceTop = 35f;
    chart.axisRight.isEnabled = false
    chart.legend.isEnabled = true
    chart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
    chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
    chart.legend.orientation = Legend.LegendOrientation.VERTICAL
    chart.legend.setDrawInside(true)
    chart.legend.yOffset = 0f
    chart.legend.xOffset = 10f
    chart.legend.yEntrySpace = 0f
    chart.legend.textSize = 8f
    chart.xAxis.valueFormatter = vfMonth
    if (hourAndTimeFormat)
        chart.axisLeft.valueFormatter = vfHourAndTime
    else
        chart.axisLeft.valueFormatter = vf
    chart.setDrawValueAboveBar(true)
    chart.setPinchZoom(false)

}

fun setDataToChart(stats: HashMap<String?, Long?>?, chart: PieChart, title: String, hourAndTimeFormat: Boolean) {
    var totalCount = 0L
    val totalCountFormatted: String
    val entries = ArrayList<PieEntry>()
    stats?.forEach {
        entries.add(PieEntry(it.value!!.toFloat(), it.key))
        totalCount += it.value!!
    }
    //ColorTemplate.VORDIPLOM_COLORS
    val colors = arrayListOf<Int>(
        Color.rgb(255, 208, 140), //Intervention
        Color.rgb(140, 234, 255), //Entrainement
        Color.rgb(255, 140, 157), //Information
        Color.rgb(192, 255, 140), //Formation
        Color.rgb(255, 247, 140) //Regulation
    )
    val dataset = PieDataSet(entries, "")
    dataset.colors = colors
    dataset.valueTextSize = 16f
    if (hourAndTimeFormat) {
        dataset.valueFormatter = vfHourAndTime
        totalCountFormatted = "${String.format("%02d", totalCount / 60)}h${String.format("%02d", totalCount % 60)}"
    } else {
        dataset.valueFormatter = vf
        totalCountFormatted = totalCount.toString()
    }
    val pieData = PieData(dataset)
    chart.data = pieData
    chart.centerText = "$title\nTotal $totalCountFormatted"
    chart.invalidate()
}

fun setBarDataToChart(stats: HashMap<Int, HashMap<String?, Long?>?>, chart: BarChart, hourAndTimeFormat: Boolean) {
    val entriesGroupTraining = arrayListOf<BarEntry>()
    val entriesGroupInterventions = arrayListOf<BarEntry>()
    val entriesGroupFormations = arrayListOf<BarEntry>()
    val entriesGroupInformation = arrayListOf<BarEntry>()
    val entriesGroupRegulations = arrayListOf<BarEntry>()

    var totalCount = 0f
    for (i in 1..12) {
        entriesGroupTraining.add(i - 1, BarEntry(i.toFloat(), 0f))
        entriesGroupInterventions.add(i - 1, BarEntry(i.toFloat(), 0f))
        entriesGroupFormations.add(i - 1, BarEntry(i.toFloat(), 0f))
        entriesGroupInformation.add(i - 1, BarEntry(i.toFloat(), 0f))
        entriesGroupRegulations.add(i - 1, BarEntry(i.toFloat(), 0f))

        stats[i]?.forEach {
            when (it.key) {
                Constants.TYPE_OPERATION_TRAINING -> {
                    it.value?.let { value ->
                        entriesGroupTraining[i - 1] = BarEntry(i.toFloat(), value.toFloat())
                        totalCount += value.toFloat()
                    }
                }
                Constants.TYPE_OPERATION_INTERVENTION -> {
                    it.value?.let { value ->
                        entriesGroupInterventions[i - 1] = BarEntry(i.toFloat(), value.toFloat())
                        totalCount += value.toFloat()
                    }
                }
                Constants.TYPE_OPERATION_FORMATION -> {
                    it.value?.let { value ->
                        entriesGroupFormations[i - 1] = BarEntry(i.toFloat(), value.toFloat())
                        totalCount += value.toFloat()
                    }
                }
                Constants.TYPE_OPERATION_INFORMATION -> {
                    it.value?.let { value ->
                        entriesGroupInformation[i - 1] = BarEntry(i.toFloat(), value.toFloat())
                        totalCount += value.toFloat()
                    }
                }
                Constants.TYPE_OPERATION_REGULATION -> {
                    it.value?.let { value ->
                        entriesGroupRegulations[i - 1] = BarEntry(i.toFloat(), value.toFloat())
                        totalCount += value.toFloat()
                    }
                }
            }
        }
    }

    val setTraining = BarDataSet(entriesGroupTraining, Constants.TYPE_OPERATION_TRAINING)
    val setIntervention = BarDataSet(entriesGroupInterventions, Constants.TYPE_OPERATION_INTERVENTION)
    val setFormation = BarDataSet(entriesGroupFormations, Constants.TYPE_OPERATION_FORMATION)
    val setInformation = BarDataSet(entriesGroupInformation, Constants.TYPE_OPERATION_INFORMATION)
    val setRegulation = BarDataSet(entriesGroupRegulations, Constants.TYPE_OPERATION_REGULATION)

    setIntervention.color = Color.rgb(255, 208, 140)
    setTraining.color = Color.rgb(140, 234, 255)
    setInformation.color = Color.rgb(255, 140, 157)
    setFormation.color = Color.rgb(192, 255, 140)
    setRegulation.color = Color.rgb(255, 247, 140)

    val barData = BarData(setTraining, setIntervention, setFormation, setInformation, setRegulation)
    if (hourAndTimeFormat)
        barData.setValueFormatter(vfHourAndTime)
    else
        barData.setValueFormatter(vf)
    barData.barWidth = 0.2f
    chart.data = barData
    val groupSpace = 0.08f
    val barSpace = 0.03f
    chart.groupBars(1f, groupSpace, barSpace)

    chart.invalidate()
}

var vf: ValueFormatter = object : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return if (value == 0f)
            ""
        else
            "" + String.format("%02d", value.toInt())
    }
}

var vfHourAndTime: ValueFormatter = object : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return if (value == 0f)
            ""
        else {
            val hour = value / 60
            val minutes = value % 60
            "${String.format("%02d", hour.toInt())}h${String.format("%02d", minutes.toInt())}"
        }
    }
}

var vfMonth: ValueFormatter = object : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return if (value != 13f) {
            val month = DateFormatSymbols.getInstance(Locale.FRANCE).months[value.toInt() - 1]
            month.substring(0, 3)
        } else {
            "" //Hack for the library : mandatory to be on 13 labels to display december...
        }
    }
}
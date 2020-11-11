package com.loic.spe95.statistiques.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.loic.spe95.R
import com.loic.spe95.databinding.FragmentStatistiquesBinding
import com.loic.spe95.speoperations.ui.SpeOperationFragmentArgs
import com.loic.spe95.utils.Constants
import kotlinx.android.synthetic.main.grid_cyno_statistiques.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class StatistiquesFragment : Fragment() {

    private lateinit var specialtyDocument: String

    private val args: SpeOperationFragmentArgs by navArgs()
    private val statistiquesViewModel: StatistiquesViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentStatistiquesBinding.inflate(inflater, container, false)
        context ?: return binding.root

        specialtyDocument = args.specialty

        binding.specialty = specialtyDocument

        subscribeUi(binding)

        return binding.root
    }

    private fun subscribeUi(binding: FragmentStatistiquesBinding) {

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        statistiquesViewModel.fetchStats(specialtyDocument, currentYear.toString())
        statistiquesViewModel.statsMotifsLd.observe(viewLifecycleOwner, Observer {
            if (Constants.FIRESTORE_CYNO_DOCUMENT == specialtyDocument) {
                //Update UI
                configureChart(binding.chartsCynoStats.type_chart)
                configureChart(binding.chartsCynoStats.ipso_chart)
                configureChart(binding.chartsCynoStats.nano_chart)
                configureChart(binding.chartsCynoStats.nerone_chart)
                configureChart(binding.chartsCynoStats.priaxe_chart)

                setDataToChart(it.motifs, binding.chartsCynoStats.type_chart, "Motifs d'intervention")
                setDataToChart(it.ipso, binding.chartsCynoStats.ipso_chart, "Heures Ipso")
                setDataToChart(it.nano, binding.chartsCynoStats.nano_chart, "Heures Nano")
                setDataToChart(it.nerone, binding.chartsCynoStats.nerone_chart, "Heures Nerone")
                setDataToChart(it.priaxe, binding.chartsCynoStats.priaxe_chart, "Heures Priaxe")
            }
            if (Constants.FIRESTORE_SD_DOCUMENT == specialtyDocument) {
                //Update UI
                configureChart(binding.chartsSdStats.type_chart)

                setDataToChart(it.motifs, binding.chartsSdStats.type_chart, "Motifs d'intervention")
            }
        })
    }

    private fun configureChart(chart: PieChart) {
        chart.description.isEnabled = false
        chart.setNoDataText(getString(R.string.statistiques_no_data))
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

    private fun setDataToChart(stats: HashMap<String?, Long?>?, chart: PieChart, title: String) {
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

}
package com.pomplarg.spe95.statistiques.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.pomplarg.spe95.databinding.FragmentStatistiquesBinding
import com.pomplarg.spe95.speoperations.ui.SpeOperationFragmentArgs
import com.pomplarg.spe95.utils.Constants
import com.pomplarg.spe95.utils.configureChart
import com.pomplarg.spe95.utils.setDataToChart
import kotlinx.android.synthetic.main.grid_cyno_statistiques.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

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
                configureChart(binding.chartsCynoStats.type_chart, context!!)
                configureChart(binding.chartsCynoStats.ipso_chart, context!!)
                configureChart(binding.chartsCynoStats.nano_chart, context!!)
                configureChart(binding.chartsCynoStats.nerone_chart, context!!)
                configureChart(binding.chartsCynoStats.priaxe_chart, context!!)

                setDataToChart(it.motifs, binding.chartsCynoStats.type_chart, "Motifs d'intervention")
                setDataToChart(it.ipso, binding.chartsCynoStats.ipso_chart, "Minutes Ipso")
                setDataToChart(it.nano, binding.chartsCynoStats.nano_chart, "Minutes Nano")
                setDataToChart(it.nerone, binding.chartsCynoStats.nerone_chart, "Minutes Nerone")
                setDataToChart(it.priaxe, binding.chartsCynoStats.priaxe_chart, "Minutes Priaxe")
            }
            if (Constants.FIRESTORE_SD_DOCUMENT == specialtyDocument) {
                //Update UI
                configureChart(binding.chartsSdStats.type_chart, context!!)
                setDataToChart(it.motifs, binding.chartsSdStats.type_chart, "Motifs d'intervention")
            }
        })
    }


}
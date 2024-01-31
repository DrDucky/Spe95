package com.pomplarg.spe95.statistiques.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pomplarg.spe95.R
import com.pomplarg.spe95.agent.ui.MaterialSdAdapter
import com.pomplarg.spe95.databinding.FragmentStatistiquesBinding
import com.pomplarg.spe95.speoperations.data.AlertStock
import com.pomplarg.spe95.speoperations.ui.SpeOperationFragmentArgs
import com.pomplarg.spe95.utils.Constants
import com.pomplarg.spe95.utils.configurePieChart
import com.pomplarg.spe95.utils.setDataToChart
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.HashMap

class StatistiquesFragment : Fragment() {

    private lateinit var specialtyDocument: String

    private val args: SpeOperationFragmentArgs by navArgs()
    private val statistiquesViewModel: StatistiquesViewModel by viewModel()

    private var materialSdAdapter = MaterialSdAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentStatistiquesBinding.inflate(inflater, container, false)
        context ?: return binding.root

        specialtyDocument = args.specialty

        binding.specialty = specialtyDocument
        binding.chartsSdStats.rvMaterialSdListStock.adapter = materialSdAdapter

        subscribeUi(binding)

        return binding.root
    }

    private fun subscribeUi(binding: FragmentStatistiquesBinding) {

        //Defaut buttons
        binding.btnStatsYearSelection.check(R.id.btn_stats_year_2024)
        statistiquesViewModel.fetchStats(specialtyDocument, Constants.YEAR_2024)
        statistiquesViewModel.fetchOperations(specialtyDocument, Constants.YEAR_2024)

        //Common Stats to all specialties
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        statistiquesViewModel.statsMotifsLd.observe(viewLifecycleOwner, Observer {
            if (Constants.FIRESTORE_CYNO_DOCUMENT == specialtyDocument) {
                //Update UI
                configurePieChart(binding.chartsCynoStats.typeChart, context)
                configurePieChart(binding.chartsCynoStats.ipsoChart, context)
                configurePieChart(binding.chartsCynoStats.nanoChart, context)
                configurePieChart(binding.chartsCynoStats.neroneChart, context)
                configurePieChart(binding.chartsCynoStats.priaxeChart, context)
                configurePieChart(binding.chartsCynoStats.sniperChart, context)
                configurePieChart(binding.chartsCynoStats.ulcoChart, context)

                setDataToChart(it.motifs, binding.chartsCynoStats.typeChart, "Motifs d'intervention", false)
                setDataToChart(it.ipso, binding.chartsCynoStats.ipsoChart, "Ipso", true)
                setDataToChart(it.nano, binding.chartsCynoStats.nanoChart, "Nano", true)
                setDataToChart(it.nerone, binding.chartsCynoStats.neroneChart, "Nerone", true)
                setDataToChart(it.priaxe, binding.chartsCynoStats.priaxeChart, "Priaxe", true)
                setDataToChart(it.sniper, binding.chartsCynoStats.sniperChart, "Sniper", true)
                setDataToChart(it.ulco, binding.chartsCynoStats.ulcoChart, "Ulco", true)

            }
            if (Constants.FIRESTORE_SD_DOCUMENT == specialtyDocument) {
                //Update UI
                configurePieChart(binding.chartsSdStats.typeChart, context)
                setDataToChart(it.motifs, binding.chartsSdStats.typeChart, "Motifs d'intervention", false)
            }
            if (Constants.FIRESTORE_RA_DOCUMENT == specialtyDocument) {
                //Update UI
                configurePieChart(binding.chartsRaStats.typeChart, context)
                setDataToChart(it.motifs, binding.chartsRaStats.typeChart, "Motifs d'intervention", false)
            }
        })

        statistiquesViewModel.operationsLd.observe(viewLifecycleOwner) { listAllOperations ->
            val regulationsDecisions = statistiquesViewModel.getRegulationsStatistiques(specialtyDocument, listAllOperations.filter { it.type == Constants.TYPE_OPERATION_REGULATION })
            configureRegulationChart(binding, regulationsDecisions)
            val interventionsDestinations = statistiquesViewModel.getDecisionsStatistiques(listAllOperations.filter { it.type == Constants.TYPE_OPERATION_INTERVENTION })
            configureDestinationChart(binding, interventionsDestinations)
            val interventionsTransports = statistiquesViewModel.getTransportsStatistiques(listAllOperations.filter { it.type == Constants.TYPE_OPERATION_INTERVENTION })
            configureTransportChart(binding, interventionsTransports)
            val interventionsActions = statistiquesViewModel.getActionsStatistiques(listAllOperations.filter { it.type == Constants.TYPE_OPERATION_INTERVENTION })
            configureActionChart(binding, interventionsActions)
            val enginsSd = statistiquesViewModel.getEnginsStatistiques(specialtyDocument, listAllOperations.filter { it.type == Constants.TYPE_OPERATION_INTERVENTION })
            configureEnginsChart(binding, enginsSd)
        }

        //SD "functionnality" only
        if (Constants.FIRESTORE_SD_DOCUMENT == specialtyDocument) {
            val alerts = arrayListOf<AlertStock>()
            alerts.add(AlertStock(Constants.SD_ETAIEMENT_BOIS_GOUSSET, 20))
            alerts.add(AlertStock(Constants.SD_ETAIEMENT_BOIS_CHEVRON, 10))
            alerts.add(AlertStock(Constants.SD_ETAIEMENT_BOIS_VOLIGE, 10))
            alerts.add(AlertStock(Constants.SD_PETIT_MAT_CARBURANT_MARLINE, 2))
            alerts.add(AlertStock(Constants.SD_PETIT_MAT_CARBURANT_SP95, 2))
            alerts.add(AlertStock(Constants.SD_PETIT_MAT_CARBURANT_MELANGE, 2))
            alerts.add(AlertStock(Constants.SD_PETIT_MAT_VISSEUSE, 4))
            alerts.add(AlertStock(Constants.SD_ETAIEMENT_METAL_PETIT, 6))
            alerts.add(AlertStock(Constants.SD_ETAIEMENT_METAL_MOYEN, 6))
            alerts.add(AlertStock(Constants.SD_ETAIEMENT_METAL_GRAND, 6))

            statistiquesViewModel.fetchSdStock(currentYear.toString())

            statistiquesViewModel.statsStocksLd.observe(viewLifecycleOwner) {
                val stockList = it.toMutableList().sortedBy { stock -> stock.name }
                materialSdAdapter.submitList(stockList)
                val spinnerList = mutableListOf<String>()
                it.forEach { material ->
                    material.quantity?.let { quantity ->
                        alerts.forEach { alertStock ->
                            if (material.name == alertStock.name && quantity <= alertStock.threshold) {
                                context?.let { context ->
                                    MaterialAlertDialogBuilder(context)
                                        .setTitle(context.resources.getString(R.string.statistiques_alert_title))
                                        .setMessage(context.resources.getString(R.string.statistiques_alert_threshold, material.name))
                                        .setPositiveButton(context.resources.getString(android.R.string.ok), DialogInterface.OnClickListener { dialog, which ->
                                            dialog.dismiss()
                                        })
                                        .show()
                                }
                            }
                        }
                        material.name?.let { name -> spinnerList.add(name) }
                    }
                }
                spinnerList.sort()
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, spinnerList)
                binding.chartsSdStats.spStockUpdate.adapter = adapter
                binding.chartsSdStats.btnStockUpdate.setOnClickListener {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(context?.resources?.getString(R.string.stock_update_popup_title))
                        .setMessage(context?.resources?.getString(R.string.stock_update_popup_message))
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            updateStock(
                                binding.chartsSdStats.spStockUpdate.selectedItem.toString(),
                                binding.chartsSdStats.etStockUpdate.text.toString(),
                                currentYear.toString()
                            )
                        }
                        .show()
                }
            }
        }
        binding.btnStatsYearSelection.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                val yearChecked = when (checkedId) {
                    R.id.btn_stats_year_2022 -> Constants.YEAR_2022
                    R.id.btn_stats_year_2023 -> Constants.YEAR_2023
                    R.id.btn_stats_year_2024 -> Constants.YEAR_2024
                    else                     -> Constants.YEAR_2024
                }
                statistiquesViewModel.fetchStats(specialtyDocument, yearChecked)
                statistiquesViewModel.fetchOperations(specialtyDocument, yearChecked)
            }
        }
    }

    private fun configureTransportChart(binding: FragmentStatistiquesBinding, interventionsTransports: HashMap<String?, Long?>) {
        configurePieChart(binding.chartsRaStats.transportsRaChart, context)
        setDataToChart(interventionsTransports, binding.chartsRaStats.transportsRaChart, "Transports interventions", false)
    }

    private fun configureActionChart(binding: FragmentStatistiquesBinding, interventionsActions: HashMap<String?, Long?>) {
        configurePieChart(binding.chartsRaStats.actionsRaChart, context)
        setDataToChart(interventionsActions, binding.chartsRaStats.actionsRaChart, "Actions interventions", false)
    }

    private fun configureRegulationChart(binding: FragmentStatistiquesBinding, regulationsDecisions: HashMap<String?, Long?>) {
        configurePieChart(binding.chartsRaStats.decisionsRaRegulChart, context)
        configurePieChart(binding.chartsCynoStats.decisionsCynoRegulChart, context)
        setDataToChart(regulationsDecisions, binding.chartsRaStats.decisionsRaRegulChart, "Décisions régulation", false)
        setDataToChart(regulationsDecisions, binding.chartsCynoStats.decisionsCynoRegulChart, "Décisions régulation", false)
    }

    private fun configureDestinationChart(binding: FragmentStatistiquesBinding, interventionsDestinations: HashMap<String?, Long?>) {
        configurePieChart(binding.chartsRaStats.destinationsRaChart, context)
        setDataToChart(interventionsDestinations, binding.chartsRaStats.destinationsRaChart, "Destinations interventions", false)
    }

    private fun configureEnginsChart(binding: FragmentStatistiquesBinding, enginsStatistique: HashMap<String?, Long?>) {
        configurePieChart(binding.chartsSdStats.enginsChart, context)
        setDataToChart(enginsStatistique, binding.chartsSdStats.enginsChart, "Engins", false)
    }

    private fun updateStock(materialName: String, quantity: String, year: String) {
        statistiquesViewModel.updateStock(materialName, quantity, year)
    }


}
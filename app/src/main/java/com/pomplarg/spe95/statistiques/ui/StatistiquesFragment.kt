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
import kotlinx.android.synthetic.main.grid_cyno_statistiques.view.*
import kotlinx.android.synthetic.main.grid_cyno_statistiques.view.type_chart
import kotlinx.android.synthetic.main.grid_sd_statistiques.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

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
        binding.chartsSdStats.rv_material_sd_list_stock.adapter = materialSdAdapter

        subscribeUi(binding)

        return binding.root
    }

    private fun subscribeUi(binding: FragmentStatistiquesBinding) {

        //Common Stats to all specialties
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        statistiquesViewModel.fetchStats(specialtyDocument, currentYear.toString())
        statistiquesViewModel.statsMotifsLd.observe(viewLifecycleOwner, Observer {
            if (Constants.FIRESTORE_CYNO_DOCUMENT == specialtyDocument) {
                //Update UI
                configurePieChart(binding.chartsCynoStats.type_chart, context)
                configurePieChart(binding.chartsCynoStats.ipso_chart, context)
                configurePieChart(binding.chartsCynoStats.nano_chart, context)
                configurePieChart(binding.chartsCynoStats.nerone_chart, context)
                configurePieChart(binding.chartsCynoStats.priaxe_chart, context)
                configurePieChart(binding.chartsCynoStats.sniper_chart, context)

                setDataToChart(it.motifs, binding.chartsCynoStats.type_chart, "Motifs d'intervention", false)
                setDataToChart(it.ipso, binding.chartsCynoStats.ipso_chart, "Ipso", true)
                setDataToChart(it.nano, binding.chartsCynoStats.nano_chart, "Nano", true)
                setDataToChart(it.nerone, binding.chartsCynoStats.nerone_chart, "Nerone", true)
                setDataToChart(it.priaxe, binding.chartsCynoStats.priaxe_chart, "Priaxe", true)
                setDataToChart(it.sniper, binding.chartsCynoStats.sniper_chart, "Sniper", true)

            }
            if (Constants.FIRESTORE_SD_DOCUMENT == specialtyDocument) {
                //Update UI
                configurePieChart(binding.chartsSdStats.type_chart, context)
                setDataToChart(it.motifs, binding.chartsSdStats.type_chart, "Motifs d'intervention", false)
            }
            if (Constants.FIRESTORE_RA_DOCUMENT == specialtyDocument) {
                //Update UI
                configurePieChart(binding.chartsRaStats.type_chart, context)
                setDataToChart(it.motifs, binding.chartsRaStats.type_chart, "Motifs d'intervention", false)
            }
        })

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

            statistiquesViewModel.statsStocksLd.observe(viewLifecycleOwner, {
                materialSdAdapter.submitList(it)
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
                binding.chartsSdStats.sp_stock_update.adapter = adapter
                binding.chartsSdStats.btn_stock_update.setOnClickListener {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(context?.resources?.getString(R.string.stock_update_popup_title))
                        .setMessage(context?.resources?.getString(R.string.stock_update_popup_message))
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            updateStock(
                                binding.chartsSdStats.sp_stock_update.selectedItem.toString(),
                                binding.chartsSdStats.et_stock_update.text.toString(),
                                currentYear.toString()
                            )
                        }
                        .show()
                }
            })
        }
    }

    private fun updateStock(materialName: String, quantity: String, year: String) {
        statistiquesViewModel.updateStock(materialName, quantity, year)
    }


}
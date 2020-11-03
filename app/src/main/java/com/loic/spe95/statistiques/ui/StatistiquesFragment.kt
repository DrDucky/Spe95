package com.loic.spe95.statistiques.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.loic.spe95.databinding.FragmentStatistiquesBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class StatistiquesFragment : Fragment() {

    private val statistiquesViewModel: StatistiquesViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentStatistiquesBinding.inflate(inflater, container, false)
        context ?: return binding.root

        subscribeUi(binding)

        return binding.root
    }

    private fun subscribeUi(binding: FragmentStatistiquesBinding) {
        statistiquesViewModel.fetchMotifsStats("cyno", "2020")
        statistiquesViewModel.statsMotifsLd.observe(viewLifecycleOwner, Observer {
            //Update UI
            binding.tvStatsMotifs.text = Gson().toJson(it.motifs)
        })

    }
}
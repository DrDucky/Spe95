package com.loic.spe95.di

import com.loic.spe95.agent.data.AgentRepository
import com.loic.spe95.agent.ui.AgentDetailsViewModel
import com.loic.spe95.agent.ui.AgentViewModel
import com.loic.spe95.signin.data.LoginRepository
import com.loic.spe95.signin.ui.LoginViewModel
import com.loic.spe95.speoperations.data.SpeOperationRepository
import com.loic.spe95.speoperations.ui.SpeOperationViewModel
import com.loic.spe95.statistiques.data.StatistiqueRepository
import com.loic.spe95.statistiques.ui.StatistiquesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val viewModelModule = module {
    viewModel { AgentViewModel(get()) }
    viewModel { AgentDetailsViewModel(get()) }
    single { AgentRepository() }

    viewModel { (specialty: String) -> SpeOperationViewModel(specialty, get()) }
    viewModel { LoginViewModel(get()) }

    single { SpeOperationRepository() }
    single { LoginRepository() }

    viewModel { StatistiquesViewModel(get()) }
    single { StatistiqueRepository() }

}
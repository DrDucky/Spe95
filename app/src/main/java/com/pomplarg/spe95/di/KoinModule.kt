package com.pomplarg.spe95.di

import com.pomplarg.spe95.agent.data.AgentRepository
import com.pomplarg.spe95.agent.ui.AgentDetailsViewModel
import com.pomplarg.spe95.agent.ui.AgentViewModel
import com.pomplarg.spe95.signin.data.LoginRepository
import com.pomplarg.spe95.signin.ui.LoginViewModel
import com.pomplarg.spe95.speoperations.data.SpeOperationRepository
import com.pomplarg.spe95.speoperations.ui.SpeOperationViewModel
import com.pomplarg.spe95.statistiques.data.StatistiqueRepository
import com.pomplarg.spe95.statistiques.ui.StatistiquesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val viewModelModule = module {
    viewModel { AgentViewModel(get()) }
    viewModel { AgentDetailsViewModel(get()) }
    single { AgentRepository() }

    viewModel { (specialty: String) -> SpeOperationViewModel(specialty, get(), get()) }
    viewModel { LoginViewModel(get()) }

    single { SpeOperationRepository() }
    single { LoginRepository() }

    viewModel { StatistiquesViewModel(get()) }
    single { StatistiqueRepository() }

}
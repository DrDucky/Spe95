package com.loic.spe95.di

import com.loic.spe95.speoperations.data.SpeOperationRepository
import com.loic.spe95.speoperations.ui.SpeOperationViewModel
import com.loic.spe95.team.data.AgentRepository
import com.loic.spe95.team.ui.AgentDetailsViewModel
import com.loic.spe95.team.ui.AgentViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val viewModelModule = module {
    viewModel { AgentViewModel(get()) }
    viewModel { AgentDetailsViewModel(get()) }
    single { AgentRepository() }

    viewModel { (specialtyId: String) -> SpeOperationViewModel(specialtyId, get()) }

    single { SpeOperationRepository() }

}
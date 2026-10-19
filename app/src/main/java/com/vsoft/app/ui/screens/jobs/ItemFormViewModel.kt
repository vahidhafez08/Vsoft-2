package com.vsoft.app.ui.screens.jobs

import androidx.lifecycle.SavedStateHandle
import com.vsoft.app.data.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ItemFormViewModel @Inject constructor(handle: SavedStateHandle, repository: JobRepository) : JobFormViewModelBase(handle, repository)

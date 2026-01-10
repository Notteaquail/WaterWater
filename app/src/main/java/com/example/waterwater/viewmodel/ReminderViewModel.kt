package com.example.waterwater.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waterwater.alarm.AlarmScheduler
import com.example.waterwater.data.repository.ReminderRepository
import com.example.waterwater.model.CatBreed
import com.example.waterwater.model.CatInstance
import com.example.waterwater.model.Reminder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val repository: ReminderRepository,
    private val scheduler: AlarmScheduler
) : ViewModel() {

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _currentReminder = MutableStateFlow<Reminder?>(null)
    val currentReminder: StateFlow<Reminder?> = _currentReminder.asStateFlow()

    val cats = mutableStateListOf<CatInstance>()

    init {
        loadReminders()
        if (cats.isEmpty()) {
            cats.addAll(listOf(
                CatInstance(breed = CatBreed.BLACK_WHITE_LONG, offset = Offset(932f, 1149f), scale = 1.0f, isThinkingEnabled = false),
                CatInstance(breed = CatBreed.GOLDEN_LONG, offset = Offset(-23f, 911f), scale = 1.2f, isThinkingEnabled = true),
                CatInstance(breed = CatBreed.CREAM_BRITISH, offset = Offset(122.7f, 1973.5f), scale = 1.0f, isThinkingEnabled = true),
                CatInstance(breed = CatBreed.MUNCHKIN_SHORT, offset = Offset(702f, 1657f), scale = 3f, isThinkingEnabled = false),
                CatInstance(breed = CatBreed.ONE_EYE_GOLDEN, offset = Offset(504f, 1392f), scale = 0.5f, isThinkingEnabled = true)
            ))
        }
    }

    fun addCat(breed: CatBreed) {
        cats.add(CatInstance(breed = breed, offset = Offset(300f, 800f)))
    }

    private fun loadReminders() {
        viewModelScope.launch {
            repository.allReminders.collect { list -> _reminders.value = list }
        }
    }

    fun showAddDialog() { _currentReminder.value = null; _showDialog.value = true }
    fun showEditDialog(reminder: Reminder) { _currentReminder.value = reminder; _showDialog.value = true }
    fun dismissDialog() { _showDialog.value = false; _currentReminder.value = null }

    fun saveReminder(reminder: Reminder) {
        viewModelScope.launch {
            if (_currentReminder.value != null) {
                repository.updateReminder(reminder)
            } else {
                val newId = repository.insertReminder(reminder)
                scheduler.schedule(reminder.copy(id = newId))
            }
            scheduler.schedule(reminder)
            dismissDialog()
        }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.toggleReminderEnabled(reminder)
            if (!reminder.isEnabled) scheduler.schedule(reminder.copy(isEnabled = true))
            else scheduler.cancel(reminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            scheduler.cancel(reminder)
        }
    }
}

class ReminderViewModelFactory(
    private val repository: ReminderRepository,
    private val scheduler: AlarmScheduler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReminderViewModel(repository, scheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

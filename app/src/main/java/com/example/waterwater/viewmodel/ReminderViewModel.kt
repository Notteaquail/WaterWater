package com.example.waterwater.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waterwater.data.repository.ReminderRepository
import com.example.waterwater.model.CatMood
import com.example.waterwater.model.Reminder
import com.example.waterwater.model.RepeatType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class   ReminderViewModel(private val repository: ReminderRepository) : ViewModel() {

    // 所有提醒列表
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    // 当前编辑的提醒
    private val _currentReminder = MutableStateFlow<Reminder?>(null)
    val currentReminder: StateFlow<Reminder?> = _currentReminder.asStateFlow()

    // 是否显示添加/编辑对话框
    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    init {
        loadReminders()
    }

    private fun loadReminders() {
        viewModelScope.launch {
            repository.allReminders.collect { list ->
                _reminders.value = list
            }
        }
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.insertReminder(reminder)
        }
    }

    fun showAddDialog() {
        _currentReminder.value = null
        _showDialog.value = true
    }

    fun showEditDialog(reminder: Reminder) {
        _currentReminder.value = reminder
        _showDialog.value = true
    }

    fun dismissDialog() {
        _showDialog.value = false
        _currentReminder.value = null
    }

    fun saveReminder(reminder: Reminder) {
        viewModelScope.launch {
            if (_currentReminder.value != null) {
                repository.updateReminder(reminder)
            } else {
                repository.insertReminder(reminder)
            }
            dismissDialog()
        }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.toggleReminderEnabled(reminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }
}

// ViewModel 工厂
class ReminderViewModelFactory(private val repository: ReminderRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReminderViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
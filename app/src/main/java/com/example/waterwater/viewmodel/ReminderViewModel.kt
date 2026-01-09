package com.example.waterwater.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waterwater.alarm.AlarmScheduler
import com.example.waterwater.data.repository.ReminderRepository
import com.example.waterwater.model.Reminder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 构造函数增加 scheduler 参数
class ReminderViewModel(
    private val repository: ReminderRepository,
    private val scheduler: AlarmScheduler
) : ViewModel() {

    // ... (保持原有的 _reminders, _currentReminder 等代码不变) ...
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    private val _currentReminder = MutableStateFlow<Reminder?>(null)
    val currentReminder: StateFlow<Reminder?> = _currentReminder.asStateFlow()

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

    // ... (showAddDialog, showEditDialog, dismissDialog 保持不变) ...
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

    // === 修改后的核心逻辑 ===

    fun saveReminder(reminder: Reminder) {
        viewModelScope.launch {
            if (_currentReminder.value != null) {
                repository.updateReminder(reminder)
            } else {
                val newId = repository.insertReminder(reminder)
                // 插入新数据后，如果需要把生成的ID回写给AlarmScheduler，
                // 最好是使用 copy 拿到带 ID 的对象，或者让 insert 返回 ID 后再 schedule
                val savedReminder = reminder.copy(id = newId)
                scheduler.schedule(savedReminder)
                dismissDialog()
                return@launch
            }

            // 更新情况
            scheduler.schedule(reminder)
            dismissDialog()
        }
    }

    fun toggleReminder(reminder: Reminder) {
        val newStatus = !reminder.isEnabled
        val updatedReminder = reminder.copy(isEnabled = newStatus)

        viewModelScope.launch {
            repository.toggleReminderEnabled(reminder) // DB update

            if (newStatus) {
                scheduler.schedule(updatedReminder)
            } else {
                scheduler.cancel(updatedReminder)
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            scheduler.cancel(reminder)
        }
    }
}

// 修改工厂类
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
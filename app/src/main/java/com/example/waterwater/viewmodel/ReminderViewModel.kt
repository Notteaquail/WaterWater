package com.example.waterwater.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waterwater.alarm.AlarmScheduler
import com.example.waterwater.data.repository.ReminderRepository
import com.example.waterwater.model.CatBreed
import com.example.waterwater.model.CatInstance
import com.example.waterwater.model.Reminder
import com.example.waterwater.utils.CatPositionManager
import com.example.waterwater.utils.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReminderViewModel(
    application: Application,
    private val repository: ReminderRepository,
    private val scheduler: AlarmScheduler
) : AndroidViewModel(application) {

    private val positionManager = CatPositionManager(application)

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _currentReminder = MutableStateFlow<Reminder?>(null)
    val currentReminder: StateFlow<Reminder?> = _currentReminder.asStateFlow()

    val cats = mutableStateListOf<CatInstance>()

    init {
        loadReminders()
        initializeCats()
    }

    private fun initializeCats() {
        if (cats.isEmpty()) {
            val breeds = listOf(
                CatBreed.BLACK_WHITE_LONG to Offset(932f, 1149f),
                CatBreed.GOLDEN_LONG to Offset(-23f, 911f),
                CatBreed.CREAM_BRITISH to Offset(122.7f, 1973.5f),
                CatBreed.MUNCHKIN_SHORT to Offset(702f, 1657f),
                CatBreed.ONE_EYE_GOLDEN to Offset(504f, 1392f)
            )

            breeds.forEachIndexed { index, pair ->
                val breed = pair.first
                val defaultPos = pair.second
                val catId = "cat_$index"
                
                // 恢复缩放比例
                val catScale = when(breed) {
                    CatBreed.MUNCHKIN_SHORT -> 3f
                    CatBreed.ONE_EYE_GOLDEN -> 0.5f
                    CatBreed.GOLDEN_LONG -> 1.2f
                    else -> 1.0f
                }
                
                // === 核心修正：恢复你之前的气泡开关设置 ===
                val isThinking = when(breed) {
                    CatBreed.BLACK_WHITE_LONG -> false // 黑白长毛不冒泡
                    CatBreed.MUNCHKIN_SHORT -> false   // 矮脚不冒泡
                    else -> true                       // 其他正常冒泡
                }

                val savedPos = positionManager.getPosition(catId, defaultPos)
                cats.add(CatInstance(
                    id = catId, 
                    breed = breed, 
                    offset = savedPos, 
                    scale = catScale, 
                    isThinkingEnabled = isThinking
                ))
            }
        }
    }

    fun saveCatPosition(cat: CatInstance) {
        positionManager.savePosition(cat.id, cat.offset)
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
            val validatedTime = TimeUtils.getInitialValidTime(reminder)
            val validatedReminder = reminder.copy(timeInMillis = validatedTime)

            val finalReminder = if (_currentReminder.value != null) {
                repository.updateReminder(validatedReminder)
                validatedReminder
            } else {
                val newId = repository.insertReminder(validatedReminder)
                validatedReminder.copy(id = newId)
            }
            
            scheduler.schedule(finalReminder)
            dismissDialog()
        }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            val updatedEnabled = !reminder.isEnabled
            val updatedReminder = reminder.copy(isEnabled = updatedEnabled)
            repository.updateReminder(updatedReminder)

            if (updatedEnabled) {
                val nextValid = updatedReminder.copy(timeInMillis = TimeUtils.getInitialValidTime(updatedReminder))
                repository.updateReminder(nextValid)
                scheduler.schedule(nextValid)
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

class ReminderViewModelFactory(
    private val application: Application,
    private val repository: ReminderRepository,
    private val scheduler: AlarmScheduler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReminderViewModel(application, repository, scheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.example.waterwater.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.waterwater.data.repository.DeskRepository
import com.example.waterwater.model.DeskMessage
import com.example.waterwater.utils.CatPositionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class DeskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DeskRepository()
    private val positionManager = CatPositionManager(application)
    private val prefs = application.getSharedPreferences("desk_binding", Context.MODE_PRIVATE)
    
    private val deskId = "secret_desk_id"
    private val myUserIdKey = "my_user_id"
    private val partnerIdKey = "partner_user_id"
    private val roomIdKey = "shared_room_id"

    val myUserId: String = prefs.getString(myUserIdKey, null) ?: run {
        val newId = UUID.randomUUID().toString().take(8).uppercase()
        prefs.edit().putString(myUserIdKey, newId).apply()
        newId
    }

    private val _partnerId = MutableStateFlow(prefs.getString(partnerIdKey, null))
    val partnerId: StateFlow<String?> = _partnerId.asStateFlow()

    private val _messages = MutableStateFlow<List<DeskMessage>>(emptyList())
    val messages: StateFlow<List<DeskMessage>> = _messages.asStateFlow()

    private val _showDeskDialog = MutableStateFlow(false)
    val showDeskDialog: StateFlow<Boolean> = _showDeskDialog.asStateFlow()

    private val _deskOffset = MutableStateFlow(
        positionManager.getPosition(deskId, Offset(500f, 1500f))
    )
    val deskOffset: StateFlow<Offset> = _deskOffset.asStateFlow()

    init {
        // 启动时如果已绑定，直接开始轮询云端数据库
        val existingRoomId = prefs.getString(roomIdKey, null)
        if (existingRoomId != null) {
            startPolling(existingRoomId)
        }
    }

    fun bindPartner(code: String) {
        if (code.isBlank()) return
        if (code == myUserId) {
            Toast.makeText(getApplication(), "不能绑定自己喵！", Toast.LENGTH_SHORT).show()
            return
        }
        
        repository.checkAndBind(myUserId, code) { roomId, error ->
            if (roomId != null) {
                _partnerId.value = code
                prefs.edit().putString(partnerIdKey, code).putString(roomIdKey, roomId).apply()
                startPolling(roomId)
                Toast.makeText(getApplication(), "契约结成！❤️", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(getApplication(), "绑定失败: $error", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startPolling(roomId: String) {
        viewModelScope.launch {
            repository.pollMessages(roomId).collect { cloudMessages ->
                // 轮询方案是全量更新
                _messages.value = cloudMessages
            }
        }
    }

    fun sendMessage(content: String) {
        val roomId = prefs.getString(roomIdKey, null) ?: return
        if (content.isBlank()) return
        
        val newMessage = DeskMessage(senderId = myUserId, content = content)
        repository.sendMessage(roomId, newMessage) { success, error ->
            if (!success) {
                Toast.makeText(getApplication(), "刻字失败: $error", Toast.LENGTH_LONG).show()
            }
            // 发送后会自动触发下一轮轮询，或者这里可以本地预更新优化体验
        }
    }

    fun unbind() {
        _partnerId.value = null
        prefs.edit().remove(partnerIdKey).remove(roomIdKey).apply()
        _messages.value = emptyList()
        Toast.makeText(getApplication(), "契约已解除", Toast.LENGTH_SHORT).show()
    }

    fun openDesk() { _showDeskDialog.value = true }
    fun closeDesk() { _showDeskDialog.value = false }

    fun updateDeskPosition(offset: Offset) {
        _deskOffset.value = offset
        positionManager.savePosition(deskId, offset)
    }
}

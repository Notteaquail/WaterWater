package com.example.waterwater.data.repository

import android.util.Log
import cn.leancloud.LCObject
import cn.leancloud.LCQuery
import com.example.waterwater.model.DeskMessage
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class DeskRepository {

    /**
     * 发送留言到云端数据库
     */
    fun sendMessage(roomId: String, message: DeskMessage, onComplete: (Boolean, String?) -> Unit) {
        val msgObj = LCObject("Messages")
        msgObj.put("roomId", roomId)
        msgObj.put("senderId", message.senderId)
        msgObj.put("content", message.content)
        msgObj.put("timestamp", message.timestamp)
        
        msgObj.saveInBackground().subscribe(object : Observer<LCObject> {
            override fun onSubscribe(d: Disposable) {}
            override fun onNext(t: LCObject) {
                onComplete(true, null)
            }
            override fun onError(e: Throwable) {
                onComplete(false, e.message)
            }
            override fun onComplete() {}
        })
    }

    /**
     * 轮询拉取最新消息 - 修复 ANR 关键点
     */
    fun pollMessages(roomId: String): Flow<List<DeskMessage>> = flow {
        while (true) {
            try {
                // 核心修复：强制在 IO 线程执行网络请求，不阻塞主线程
                val messages = withContext(Dispatchers.IO) {
                    val query = LCQuery<LCObject>("Messages")
                    query.whereEqualTo("roomId", roomId)
                    query.orderByAscending("timestamp")
                    query.limit(50)
                    
                    val results = query.find()
                    results.map { 
                        DeskMessage(
                            id = it.objectId,
                            senderId = it.getString("senderId") ?: "",
                            content = it.getString("content") ?: "",
                            timestamp = it.getLong("timestamp")
                        )
                    }
                }
                emit(messages)
            } catch (e: Exception) {
                Log.e("DeskRepo", "轮询失败: ${e.message}")
            }
            
            delay(5000) // 休息5秒
        }
    }.flowOn(Dispatchers.IO) // 确保整个流的生产过程都在 IO 线程

    /**
     * 绑定逻辑
     */
    fun checkAndBind(myId: String, partnerId: String, onResult: (String?, String?) -> Unit) {
        val roomId = if (myId < partnerId) "${myId}_$partnerId" else "${partnerId}_$myId"
        onResult(roomId, null)
    }
}

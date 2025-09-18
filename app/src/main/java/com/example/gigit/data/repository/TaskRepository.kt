package com.example.gigit.data.repository

import com.example.gigit.data.model.Message
import com.example.gigit.data.model.Task
import com.example.gigit.data.source.TaskSource
import com.example.gigit.util.Resource
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val source: TaskSource) {

    suspend fun postNewTask(task: Task): Resource<Unit> {
        // In the future, you could add logic here, like validating the task data.
        return source.postNewTask(task)
    }

    // MODIFIED: Now passes the userId down to the source
    fun getOpenTasks(currentUserId: String): Flow<Resource<List<Task>>> {
        return source.getOpenTasks(currentUserId)
    }

    // --- NEW FUNCTIONS ---
    suspend fun getTaskDetails(taskId: String): Resource<Task?> {
        return source.getTaskDetails(taskId)
    }

    suspend fun acceptTask(taskId: String, taskerId: String): Resource<Unit> {
        return source.acceptTask(taskId, taskerId)
    }

    fun getMessages(taskId: String): Flow<Resource<List<Message>>> {
        return source.getMessages(taskId)
    }

    suspend fun sendMessage(taskId: String, message: Message): Resource<Unit> {
        return source.sendMessage(taskId, message)
    }

    fun getActiveGigs(userId: String): Flow<Resource<List<Task>>> {
        return source.getActiveGigs(userId)
    }

    suspend fun markTaskAsCompleted(taskId: String): Resource<Unit> {
        return source.markTaskAsCompleted(taskId)
    }

    suspend fun submitReview(review: com.example.gigit.data.model.Review): Resource<Unit> {
        return source.submitReview(review)
    }
}

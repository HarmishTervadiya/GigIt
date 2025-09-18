package com.example.gigit.services

import android.util.Log
import com.example.gigit.data.repository.UserRepository
import com.example.gigit.data.source.UserSource
import com.example.gigit.util.NotificationUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GigItFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "New token: $token")
        Firebase.auth.currentUser?.uid?.let { userId ->
            sendTokenToServer(userId, token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.notification?.let { notification ->
            NotificationUtils.showNotification(
                context = this,
                title = notification.title ?: "New Notification",
                message = notification.body ?: ""
            )
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun sendTokenToServer(userId: String, token: String) {
        GlobalScope.launch {
            try {
                val userSource = UserSource(Firebase.firestore)
                val userRepository = UserRepository(userSource)
                userRepository.updateFcmToken(userId, token)
            } catch (e: Exception) {
                Log.e("FCM_TOKEN", "Error saving token", e)
            }
        }
    }
}

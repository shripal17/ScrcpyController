package com.codertainment.scrcpy.controller.util

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent

/*
 * Created by Shripal Jain
 * on 18/06/2020
 */

object Notifier {

  fun notify(title: String, message: String, type: NotificationType, actions: List<ScrcpyNotificationAction>? = null) {
    val notif = NotificationGroupManager.getInstance().getNotificationGroup("scrcpy")
      .createNotification(title, message, type)

    actions?.forEach {
      notif.addAction(it)
    }

    notif.notify(null)
  }
}

class ScrcpyNotificationAction(title: String, private val onAction: (e: AnActionEvent, n: Notification) -> Unit) :
  NotificationAction(title) {
  override fun actionPerformed(p0: AnActionEvent, p1: Notification) {
    invokeLater {
      onAction(p0, p1)
    }
  }
}

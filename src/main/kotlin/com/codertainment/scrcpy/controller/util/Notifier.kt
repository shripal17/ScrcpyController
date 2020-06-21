package com.codertainment.scrcpy.controller.util

import com.intellij.notification.*
import com.intellij.openapi.actionSystem.AnActionEvent
import icons.Icons

/*
 * Created by Shripal Jain
 * on 18/06/2020
 */

object Notifier {
  val GROUP = NotificationGroup("scrcpy Connections", NotificationDisplayType.BALLOON, false, "scrcpy", Icons.TOOL_WINDOW)

  fun notify(title: String, message: String, type: NotificationType, actions: List<ScrcpyNotificationAction>? = null) {
    val n = GROUP.createNotification(title, null, message, type, null)
    if (actions != null && actions.isNotEmpty()) {
      actions.forEach {
        n.addAction(it)
      }
    }
    n.notify(null)
  }
}

class ScrcpyNotificationAction(title: String, private val onAction: (e: AnActionEvent, n: Notification) -> Unit) : NotificationAction(title) {
  override fun actionPerformed(p0: AnActionEvent, p1: Notification) {
    invokeLater {
      onAction(p0, p1)
    }
  }
}

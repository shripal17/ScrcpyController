package com.codertainment.scrcpy.controller.util

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType

/*
 * Created by Shripal Jain
 * on 18/06/2020
 */

object Notifier {
  val GROUP = NotificationGroup("scrcpy Connections", NotificationDisplayType.BALLOON, true)

  fun notify(title: String, message: String, type: NotificationType) {
    val n = GROUP.createNotification(title, message, type)
    n.notify(null)
  }
}
package com.codertainment.scrcpy.controller.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/*
 * Created by Shripal Jain
 * on 12/06/2020
 */

class ScrcpyToolWindowFactory : ToolWindowFactory {
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val controller = ScrcpyController(toolWindow)
    val content = ContentFactory.SERVICE.getInstance().createContent(controller.mainPanel, "Controller", false)
    toolWindow.contentManager.addContent(content)
  }
}
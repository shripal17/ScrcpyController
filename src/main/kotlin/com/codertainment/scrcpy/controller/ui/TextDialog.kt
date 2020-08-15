package com.codertainment.scrcpy.controller.ui

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.util.ui.UIUtil
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.JPanel
import javax.swing.event.HyperlinkEvent

/*
 * Created by Shripal Jain
 * on 21/06/2020
 */

class TextDialog(private val mTitle: String, private var text: String, private val isHtml: Boolean) : DialogWrapper(true) {

  init {
    init()
    title = mTitle
  }

  private var textPanel: JPanel? = null
  private var textPane: JEditorPane? = null

  override fun createCenterPanel(): JComponent? {
    if (isHtml) {
      textPane?.contentType = "text/html"
    }
    if (UIUtil.isUnderDarcula() && isHtml) {
      text = "<span style=\"color:white;\">$text</span>"
    }
    // println(text)
    textPane?.apply {
      text = this@TextDialog.text
      isEditable = false
      isOpaque = false
      addHyperlinkListener {
        if (HyperlinkEvent.EventType.ACTIVATED == it.eventType) {
          BrowserUtil.browse(it.url)
        }
      }
    }
    //shortcutsPanel?.setSize(500, 500)
    return textPanel
  }
}
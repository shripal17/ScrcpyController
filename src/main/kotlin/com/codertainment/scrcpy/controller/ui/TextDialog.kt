package com.codertainment.scrcpy.controller.ui

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.JPanel
import javax.swing.UIManager
import javax.swing.event.HyperlinkEvent

/*
 * Created by Shripal Jain
 * on 21/06/2020
 */

class TextDialog(mTitle: String, private var text: String, private val isHtml: Boolean) : DialogWrapper(true) {

  init {
    init()
    title = mTitle
  }

  private var textPanel: JPanel? = null
  private var textPane: JEditorPane? = null

  override fun createCenterPanel(): JComponent? {
    val foregroundColor = UIManager.getColor("Label.foreground")
    val foregroundHex = String.format("#%02x%02x%02x", foregroundColor.red, foregroundColor.green, foregroundColor.blue)
    if (isHtml) {
      textPane?.contentType = "text/html"
    }
    if (isHtml) {
      text = "<span style=\"color:$foregroundHex;\">$text</span>"
    }
    // println(text)
    textPane?.apply {
      foreground = UIManager.getColor("Label.foreground")
      background = UIManager.getColor("Label.background")
      text = this@TextDialog.text.replace("\n", "<br>")
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
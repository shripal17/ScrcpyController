package com.codertainment.scrcpy.controller.util

import com.intellij.openapi.application.ApplicationManager
import java.text.NumberFormat
import javax.swing.JFormattedTextField
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.NumberFormatter

/*
 * Created by Shripal Jain
 * on 18/06/2020
 */

internal fun JTextField?.active() = !this?.text.isNullOrEmpty()
internal fun JTextField?.text() = this?.text ?: ""

internal fun JFormattedTextField?.numberFormatter(maxValue: Int, defValue: Int? = null) {
  NumberFormatter(NumberFormat.getIntegerInstance().also { it.isGroupingUsed = false }).apply {
    valueClass = Integer::class.java
    allowsInvalid = true
    maximum = maxValue
    commitsOnValidEdit = true
    install(this@numberFormatter)
  }
  defValue?.let {
    this?.text = it.toString()
  }
}

internal fun JTextField?.onTextChangedListener(onChanged: (text: String) -> Unit) {
  this?.document?.addDocumentListener(object : DocumentListener {
    override fun changedUpdate(p0: DocumentEvent?) {
      onChanged(text())
    }

    override fun insertUpdate(p0: DocumentEvent?) {
      onChanged(text())
    }

    override fun removeUpdate(p0: DocumentEvent?) {
      onChanged(text())
    }
  })
}

internal fun String.intOrNull() = if (isEmpty()) null else try {
  toInt()
} catch (e: NumberFormatException) {
  null
}

inline fun invokeLater(crossinline toRun: () -> Unit) {
  ApplicationManager.getApplication().invokeLater {
    toRun()
  }
}

const val SHORTCUTS = "| Action                                      |   Shortcut                    |   Shortcut (macOS)\n" +
    " | ------------------------------------------- |:----------------------------- |:-----------------------------\n" +
    " | Switch fullscreen mode                      | `Ctrl`+`f`                    | `Cmd`+`f`\n" +
    " | Rotate display left                         | `Ctrl`+`←` _(left)_           | `Cmd`+`←` _(left)_\n" +
    " | Rotate display right                        | `Ctrl`+`→` _(right)_          | `Cmd`+`→` _(right)_\n" +
    " | Resize window to 1:1 (pixel-perfect)        | `Ctrl`+`g`                    | `Cmd`+`g`\n" +
    " | Resize window to remove black borders       | `Ctrl`+`x` \\| _Double-click¹_ | `Cmd`+`x`  \\| _Double-click¹_\n" +
    " | Click on `HOME`                             | `Ctrl`+`h` \\| _Middle-click_  | `Ctrl`+`h` \\| _Middle-click_\n" +
    " | Click on `BACK`                             | `Ctrl`+`b` \\| _Right-click²_  | `Cmd`+`b`  \\| _Right-click²_\n" +
    " | Click on `APP_SWITCH`                       | `Ctrl`+`s`                    | `Cmd`+`s`\n" +
    " | Click on `MENU`                             | `Ctrl`+`m`                    | `Ctrl`+`m`\n" +
    " | Click on `VOLUME_UP`                        | `Ctrl`+`↑` _(up)_             | `Cmd`+`↑` _(up)_\n" +
    " | Click on `VOLUME_DOWN`                      | `Ctrl`+`↓` _(down)_           | `Cmd`+`↓` _(down)_\n" +
    " | Click on `POWER`                            | `Ctrl`+`p`                    | `Cmd`+`p`\n" +
    " | Power on                                    | _Right-click²_                | _Right-click²_\n" +
    " | Turn device screen off (keep mirroring)     | `Ctrl`+`o`                    | `Cmd`+`o`\n" +
    " | Turn device screen on                       | `Ctrl`+`Shift`+`o`            | `Cmd`+`Shift`+`o`\n" +
    " | Rotate device screen                        | `Ctrl`+`r`                    | `Cmd`+`r`\n" +
    " | Expand notification panel                   | `Ctrl`+`n`                    | `Cmd`+`n`\n" +
    " | Collapse notification panel                 | `Ctrl`+`Shift`+`n`            | `Cmd`+`Shift`+`n`\n" +
    " | Copy device clipboard to computer           | `Ctrl`+`c`                    | `Cmd`+`c`\n" +
    " | Paste computer clipboard to device          | `Ctrl`+`v`                    | `Cmd`+`v`\n" +
    " | Copy computer clipboard to device and paste | `Ctrl`+`Shift`+`v`            | `Cmd`+`Shift`+`v`\n" +
    " | Enable/disable FPS counter (on stdout)      | `Ctrl`+`i`                    | `Cmd`+`i`\n" +
    "\n" +
    "_¹Double-click on black borders to remove them._  \n" +
    "_²Right-click turns the screen on if it was off, presses BACK otherwise._\n" +
    "\n"
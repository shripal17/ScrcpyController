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

const val SHORTCUTS = "In the following list, <kbd>MOD</kbd> is the shortcut modifier. By default, it's\n" +
    "(left) <kbd>Alt</kbd> or (left) <kbd>Super</kbd>.\n" +
    "\n" +
    "It can be changed using `--shortcut-mod`. Possible keys are `lctrl`, `rctrl`,\n" +
    "`lalt`, `ralt`, `lsuper` and `rsuper`. For example:\n" +
    "\n" +
    "```bash\n" +
    "# use RCtrl for shortcuts\n" +
    "scrcpy --shortcut-mod=rctrl\n" +
    "\n" +
    "# use either LCtrl+LAlt or LSuper for shortcuts\n" +
    "scrcpy --shortcut-mod=lctrl+lalt,lsuper\n" +
    "```\n" +
    "\n" +
    "_<kbd>[Super]</kbd> is typically the <kbd>Windows</kbd> or <kbd>Cmd</kbd> key._\n" +
    "\n" +
    "[Super]: https://en.wikipedia.org/wiki/Super_key_(keyboard_button)\n" +
    "\n" +
    " | Action                                      |   Shortcut\n" +
    " | ------------------------------------------- |:-----------------------------\n" +
    " | Switch fullscreen mode                      | <kbd>MOD</kbd>+<kbd>f</kbd>\n" +
    " | Rotate display left                         | <kbd>MOD</kbd>+<kbd>←</kbd> _(left)_\n" +
    " | Rotate display right                        | <kbd>MOD</kbd>+<kbd>→</kbd> _(right)_\n" +
    " | Resize window to 1:1 (pixel-perfect)        | <kbd>MOD</kbd>+<kbd>g</kbd>\n" +
    " | Resize window to remove black borders       | <kbd>MOD</kbd>+<kbd>w</kbd> \\| _Double-click¹_\n" +
    " | Click on `HOME`                             | <kbd>MOD</kbd>+<kbd>h</kbd> \\| _Middle-click_\n" +
    " | Click on `BACK`                             | <kbd>MOD</kbd>+<kbd>b</kbd> \\| _Right-click²_\n" +
    " | Click on `APP_SWITCH`                       | <kbd>MOD</kbd>+<kbd>s</kbd>\n" +
    " | Click on `MENU` (unlock screen)             | <kbd>MOD</kbd>+<kbd>m</kbd>\n" +
    " | Click on `VOLUME_UP`                        | <kbd>MOD</kbd>+<kbd>↑</kbd> _(up)_\n" +
    " | Click on `VOLUME_DOWN`                      | <kbd>MOD</kbd>+<kbd>↓</kbd> _(down)_\n" +
    " | Click on `POWER`                            | <kbd>MOD</kbd>+<kbd>p</kbd>\n" +
    " | Power on                                    | _Right-click²_\n" +
    " | Turn device screen off (keep mirroring)     | <kbd>MOD</kbd>+<kbd>o</kbd>\n" +
    " | Turn device screen on                       | <kbd>MOD</kbd>+<kbd>Shift</kbd>+<kbd>o</kbd>\n" +
    " | Rotate device screen                        | <kbd>MOD</kbd>+<kbd>r</kbd>\n" +
    " | Expand notification panel                   | <kbd>MOD</kbd>+<kbd>n</kbd>\n" +
    " | Collapse notification panel                 | <kbd>MOD</kbd>+<kbd>Shift</kbd>+<kbd>n</kbd>\n" +
    " | Copy to clipboard³                          | <kbd>MOD</kbd>+<kbd>c</kbd>\n" +
    " | Cut to clipboard³                           | <kbd>MOD</kbd>+<kbd>x</kbd>\n" +
    " | Synchronize clipboards and paste³           | <kbd>MOD</kbd>+<kbd>v</kbd>\n" +
    " | Inject computer clipboard text              | <kbd>MOD</kbd>+<kbd>Shift</kbd>+<kbd>v</kbd>\n" +
    " | Enable/disable FPS counter (on stdout)      | <kbd>MOD</kbd>+<kbd>i</kbd>\n" +
    " | Pinch-to-zoom                               | <kbd>Ctrl</kbd>+_click-and-move_\n" +
    "\n" +
    "_¹Double-click on black borders to remove them._  \n" +
    "_²Right-click turns the screen on if it was off, presses BACK otherwise._  \n" +
    "_³Only on Android >= 7._\n" +
    "\n" +
    "All <kbd>Ctrl</kbd>+_key_ shortcuts are forwarded to the device, so they are\n" +
    "handled by the active application.\n" +
    "\n"
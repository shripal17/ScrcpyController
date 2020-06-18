package com.codertainment.scrcpy.controller.util

import java.text.DecimalFormat
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
  NumberFormatter(NumberFormat.getIntegerInstance()).apply {
    valueClass = Integer::class.java
    format = DecimalFormat("#####")
    allowsInvalid = false
    overwriteMode = true
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

internal fun String.intOrNull() = if(isEmpty()) null else toInt()

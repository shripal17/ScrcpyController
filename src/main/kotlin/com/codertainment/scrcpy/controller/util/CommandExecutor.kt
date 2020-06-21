package com.codertainment.scrcpy.controller.util

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.invokeLater
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/*
 * Created by Shripal Jain
 * on 19/06/2020
 */

class CommandExecutor(cmds: List<String>, private val onUpdate: (exitCode: Int?, line: String?, fullOutput: String?, ioe: Boolean) -> Unit) : Thread() {

  init {
    println("Got Command: ${cmds.joinToString(" ")}")
  }

  private val processBuilder = ProcessBuilder(cmds).apply {
    redirectError(ProcessBuilder.Redirect.PIPE)
    redirectOutput(ProcessBuilder.Redirect.PIPE)
    redirectInput(ProcessBuilder.Redirect.PIPE)
  }

  private var process: Process? = null

  var output = StringBuilder()

  override fun run() {
    super.run()
    try {
      process = processBuilder.start()
      val reader = BufferedReader(InputStreamReader(process!!.inputStream))
      var line: String? = ""
      while (line != null) {
        line = reader.readLine()
        line?.let {
          output.append(it + "\n")
          invokeLater(ModalityState.any()) {
            onUpdate(null, line, null, false)
          }
        }
      }
      val exitCode = process!!.waitFor()
      invokeLater(ModalityState.any()) {
        onUpdate(exitCode, null, output.toString(), false)
      }
    } catch (io: IOException) {
      io.printStackTrace()
      invokeLater(ModalityState.any()) {
        onUpdate(null, null, null, true)
      }
    }
  }

  override fun interrupt() {
    process?.destroy()
    process?.waitFor()
    super.interrupt()
  }
}
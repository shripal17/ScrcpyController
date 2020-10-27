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

class CommandExecutor(
  cmds: List<String>,
  private var adbPath: String? = null,
  private var modalityState: ModalityState? = null,
  private val onUpdate: (exitCode: Int?, line: String?, fullOutput: String?, ioe: Boolean) -> Unit
) : Thread() {

  init {
    println("Got Command: ${cmds.joinToString(" ")}")
  }

  private val processBuilder = ProcessBuilder(cmds).apply {
    adbPath?.let {
      environment().put("ADB", it)
    }
    redirectError(ProcessBuilder.Redirect.PIPE)
    redirectOutput(ProcessBuilder.Redirect.PIPE)
    redirectInput(ProcessBuilder.Redirect.PIPE)
    redirectErrorStream(true)
  }

  private var process: Process? = null

  var output = StringBuilder()

  override fun run() {
    super.run()
    try {
      var line: String? = ""
      process = processBuilder.start()
      val reader = BufferedReader(InputStreamReader(process!!.inputStream))
      Thread {
        while (line != null) {
          line = reader.readLine()
          line?.let {
            println(it)
            output.append(it + "\n")
            invokeLater(modalityState) {
              onUpdate(null, line, null, false)
            }
          }
        }
      }.start()
      val exitCode = process!!.waitFor()
      invokeLater(modalityState) {
        onUpdate(exitCode, null, output.toString(), false)
      }
    } catch (io: IOException) {
      io.printStackTrace()
      invokeLater(modalityState) {
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
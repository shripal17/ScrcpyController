package com.codertainment.scrcpy.controller.util

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/*
 * Created by Shripal Jain
 * on 19/06/2020
 */

class CommandExecutor(cmds: List<String>, dir: String? = null, private val onUpdate: (exitCode: Int?, output: String?) -> Unit) : Thread() {

  init {
    println("Got Command: ${cmds.joinToString(" ")}")
  }

  private val processBuilder = ProcessBuilder(cmds).apply {
    redirectError(ProcessBuilder.Redirect.PIPE)
    redirectOutput(ProcessBuilder.Redirect.PIPE)
    redirectInput(ProcessBuilder.Redirect.PIPE)
    dir?.let {
      directory(File(it))
    }
  }

  private var process: Process? = null

  override fun run() {
    super.run()
    process = processBuilder.start()
    val reader = BufferedReader(InputStreamReader(process!!.inputStream))
    var line: String? = ""
    while (line != null) {
      line = reader.readLine()
      line?.let {
        onUpdate(null, it)
      }
    }
    onUpdate(process!!.waitFor(), null)
  }

  override fun interrupt() {
    process?.destroy()
    process?.waitFor()
    super.interrupt()
  }
}
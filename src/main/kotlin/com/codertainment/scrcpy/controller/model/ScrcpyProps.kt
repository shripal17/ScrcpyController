package com.codertainment.scrcpy.controller.model

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/*
 * Created by Shripal Jain
 * on 13/06/2020
 */

@State(name = "com.codertainment.scrcpy.controller.model.ScrcpyProps", storages = [Storage("ScrcpyProps.xml")])
data class ScrcpyProps(
  //adb
  var ip1: Int? = 192, var ip2: Int? = 168, var ip3: Int? = null, var ip4: Int? = null, var port: Int? = 5555,

  //display
  var maxResolution: Int? = null,
  var maxFps: Int? = null,
  var bitRate: Int? = null,
  var bitRateUnit: BitRateUnit = BitRateUnit.M,
  var videoOrientation: VideoOrientation = VideoOrientation.Unlocked,
  var cropX: Int? = null,
  var cropY: Int? = null,
  var cropXOffset: Int? = null,
  var cropYOffset: Int? = null,

  //recording
  var enableRecording: Boolean = false,
  var recordingPath: String? = null,
  var recordingFileName: String? = null,
  var disableMirroring: Boolean = false,

  //window
  var windowTitle: String? = null,
  var rotation: Rotation = Rotation.Default,
  var borderless: Boolean = false,
  var alwaysOnTop: Boolean = false,
  var fullscreen: Boolean = false,
  var positionX: Int? = null,
  var positionY: Int? = null,
  var positionWidth: Int? = null,
  var positionHeight: Int? = null,

  //other
  var readOnly: Boolean = false,
  var stayAwake: Boolean = false,
  var turnScreenOff: Boolean = false,
  var renderExpiredFrames: Boolean = false,
  var showTouches: Boolean = false
) : PersistentStateComponent<ScrcpyProps> {

  companion object {
    fun getInstance() = ServiceManager.getService(ScrcpyProps::class.java)
  }

  override fun getState(): ScrcpyProps? = this
  override fun loadState(state: ScrcpyProps) {
    XmlSerializerUtil.copyBean(state, this)
  }

  val isIpValid get() = ip1 != null && ip2 != null && ip3 != null && ip4 != null && port != null
  val ip get() = listOf(ip1, ip2, ip3, ip4).joinToString(".")

  fun buildCommand(serial: String) = arrayListOf("scrcpy").apply {
    add("-s")
    add(serial)

    maxResolution?.let {
      add("-m")
      add(maxResolution.toString())
    }

    maxFps?.let {
      add("--max-fps")
      add(maxFps.toString())
    }

    bitRate?.let {
      add("-b")
      add("${it}${bitRateUnit.name}")
    }

    if (videoOrientation != VideoOrientation.Unlocked) {
      add("--lock-video-orientation")
      add(videoOrientation.value.toString())
    }

    if (cropX != null && cropY != null && cropXOffset != null && cropYOffset != null) {
      add("--crop")
      add("$cropX:$cropY:$cropXOffset:$cropYOffset")
    }


    if (!windowTitle.isNullOrEmpty()) {
      add("--window-title")
      add("\'$windowTitle\'")
    }

    if (rotation != Rotation.Default) {
      add("--rotation")
      add(rotation.value.toString())
    }

    if (borderless) {
      add("--window-borderless")
    }
    if (alwaysOnTop) {
      add("--always-on-top")
    }
    if (fullscreen) {
      add("-f")
    }

    positionX?.let {
      add("--window-x")
      add(it.toString())
    }
    positionY?.let {
      add("--window-y")
      add(it.toString())
    }
    positionWidth?.let {
      add("--window-width")
      add(it.toString())
    }
    positionHeight?.let {
      add("--window-height")
      add(it.toString())
    }

    if (readOnly) {
      add("-n")
    }
    if (stayAwake) {
      add("-w")
    }
    if (turnScreenOff) {
      add("-S")
    }
    if (renderExpiredFrames) {
      add("--render-expired-frames")
    }
    if (showTouches) {
      add("-t")
    }
  }
}

enum class VideoOrientation(val value: Int? = null) {
  Unlocked,
  Portrait(0),
  Landscape(1),
  PortraitReverse(2),
  LandscapeReverse(3)
}

enum class BitRateUnit {
  K, M
}

enum class Rotation(val value: Int? = null) {
  Default,
  Landscape(1),
  PortraitReverse(2),
  LandscapeReverse(3)
}



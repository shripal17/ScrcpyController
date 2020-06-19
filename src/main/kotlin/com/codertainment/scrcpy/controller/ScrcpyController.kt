package com.codertainment.scrcpy.controller

import com.codertainment.scrcpy.controller.model.BitRateUnit
import com.codertainment.scrcpy.controller.model.Rotation
import com.codertainment.scrcpy.controller.model.ScrcpyProps
import com.codertainment.scrcpy.controller.model.VideoOrientation
import com.codertainment.scrcpy.controller.util.*
import com.intellij.notification.NotificationType
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.table.JBTable
import se.vidstige.jadb.DeviceDetectionListener
import se.vidstige.jadb.JadbConnection
import se.vidstige.jadb.JadbDevice
import java.net.ConnectException
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import javax.swing.*
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableModel
import kotlin.reflect.KMutableProperty1

typealias IntProp = KMutableProperty1<ScrcpyProps, Int?>
typealias StringProp = KMutableProperty1<ScrcpyProps, String?>
typealias BooleanProp = KMutableProperty1<ScrcpyProps, Boolean>

internal class ScrcpyController(private val toolWindow: ToolWindow) : DeviceDetectionListener {
  private var props = ScrcpyProps.getInstance()
  var mainPanel: JPanel? = null

  //adb
  private var conn: JadbConnection? = null

  private var devicesRefresh: JButton? = null
  private var wifiConnect: JButton? = null
  private var wifiIp1: JFormattedTextField? = null
  private var wifiIp2: JFormattedTextField? = null
  private var wifiIp3: JFormattedTextField? = null
  private var wifiIp4: JFormattedTextField? = null
  private var wifiPort: JFormattedTextField? = null
  private var devices: JBTable? = null

  //display
  private var maxResolution: JFormattedTextField? = null
  private var maxFps: JFormattedTextField? = null
  private var bitRate: JFormattedTextField? = null
  private var bitRateUnit: JComboBox<String>? = null
  private var videoOrientation: JComboBox<String>? = null
  private var cropX: JFormattedTextField? = null
  private var cropY: JFormattedTextField? = null
  private var cropXOffset: JFormattedTextField? = null
  private var cropYOffset: JFormattedTextField? = null

  //record
  private var enableRecording: JCheckBox? = null
  private var folderLabel: JLabel? = null
  private var folderButton: JButton? = null
  private var fileName: JFormattedTextField? = null
  private var disableMirroring: JCheckBox? = null

  // window
  private var windowTitle: JFormattedTextField? = null
  private var rotation: JComboBox<String>? = null
  private var borderless: JCheckBox? = null
  private var alwaysOnTop: JCheckBox? = null
  private var fullscreen: JCheckBox? = null
  private var positionX: JFormattedTextField? = null
  private var positionY: JFormattedTextField? = null
  private var positionWidth: JFormattedTextField? = null
  private var positionHeight: JFormattedTextField? = null

  //other
  private var readOnly: JCheckBox? = null
  private var stayAwake: JCheckBox? = null
  private var turnScreenOff: JCheckBox? = null
  private var renderExpiredFrames: JCheckBox? = null
  private var showTouches: JCheckBox? = null

  private var run: JButton? = null
  private var stop: JButton? = null

  private var commandExecutors = ConcurrentHashMap<String, CommandExecutor>()
  private var allDevices = listOf<JadbDevice>()
  private var selectedDevices = arrayListOf<String>()
  private var dataListener = DeviceCheckListener()

  init {
    initAdb()
    loadDevices()

    initIpFields()
    initDisplayFields()
    initRecordingFields()
    initWindowFields()
    initOtherFields()

    initButtons()

    run?.addActionListener {
      selectedDevices.forEach {
        if (!commandExecutors.containsKey(it)) {
          val cmd = CommandExecutor(props.buildCommand(it)) { exit, msg ->
            exit?.let { _ ->
              commandExecutors.remove(it)
              println("Exited: $exit")
            }
            msg?.let {
              println(it)
            }
          }
          cmd.start()
          commandExecutors[it] = cmd
        }
      }
    }

    stop?.addActionListener {
      selectedDevices.intersect(commandExecutors.keys().toList()).forEach {
        commandExecutors[it]?.interrupt()
        commandExecutors.remove(it)
      }
    }
  }

  private fun initDisplayFields() {
    maxResolution.bindNumber(4320, ScrcpyProps::maxResolution)
    maxFps.bindNumber(60, ScrcpyProps::maxFps)
    bitRate.bindNumber(10_000, ScrcpyProps::bitRate)

    bitRateUnit?.selectedIndex = BitRateUnit.values().indexOf(props.bitRateUnit)
    bitRateUnit?.addActionListener {
      props.bitRateUnit = if (bitRateUnit?.selectedIndex ?: 0 == 0) BitRateUnit.M else BitRateUnit.K
    }

    videoOrientation?.selectedIndex = VideoOrientation.values().indexOf(props.videoOrientation)
    videoOrientation?.addActionListener {
      props.videoOrientation = VideoOrientation.values()[videoOrientation?.selectedIndex ?: 0]
    }

    cropX.bindNumber(7680, ScrcpyProps::cropX)
    cropY.bindNumber(4320, ScrcpyProps::cropY)
    cropXOffset.bindNumber(7680, ScrcpyProps::cropXOffset)
    cropYOffset.bindNumber(4320, ScrcpyProps::cropYOffset)
  }

  private fun initRecordingFields() {
    enableRecording.bind(ScrcpyProps::enableRecording)
    disableMirroring.bind(ScrcpyProps::disableMirroring)
    fileName.bindString(ScrcpyProps::recordingFileName)
    // TODO Folder selection
  }

  private fun initWindowFields() {
    windowTitle.bindString(ScrcpyProps::windowTitle)

    rotation?.selectedIndex = Rotation.values().indexOf(props.rotation)
    rotation?.addActionListener {
      props.rotation = Rotation.values()[rotation?.selectedIndex ?: 0]
    }
    borderless.bind(ScrcpyProps::borderless)
    alwaysOnTop.bind(ScrcpyProps::alwaysOnTop)
    fullscreen.bind(ScrcpyProps::fullscreen)

    positionX.bindNumber(7680, ScrcpyProps::positionX)
    positionY.bindNumber(4320, ScrcpyProps::positionY)

    positionWidth.bindNumber(7680, ScrcpyProps::positionWidth)
    positionHeight.bindNumber(4320, ScrcpyProps::positionHeight)
  }

  private fun initOtherFields() {
    readOnly.bind(ScrcpyProps::readOnly)
    stayAwake.bind(ScrcpyProps::stayAwake)
    turnScreenOff.bind(ScrcpyProps::turnScreenOff)
    renderExpiredFrames.bind(ScrcpyProps::renderExpiredFrames)
    showTouches.bind(ScrcpyProps::showTouches)
  }

  private fun initIpFields() {
    wifiIp1.bindIp(ScrcpyProps::ip1)
    wifiIp2.bindIp(ScrcpyProps::ip2)
    wifiIp3.bindIp(ScrcpyProps::ip3)
    wifiIp4.bindIp(ScrcpyProps::ip4)

    wifiPort.bindNumber(65535, ScrcpyProps::port)
  }

  private fun JFormattedTextField?.bindIp(p: IntProp) {
    bindNumber(255, p)
  }

  private fun JFormattedTextField?.bindNumber(max: Int? = null, p: IntProp) {
    numberFormatter(max ?: Integer.MAX_VALUE, p.get(props))
    onTextChangedListener {
      p.set(props, it.intOrNull())
    }
  }

  private fun JFormattedTextField?.bindString(prop: StringProp) {
    this?.text = prop.get(props)
    onTextChangedListener {
      prop.set(props, it)
    }
  }

  private fun JCheckBox?.bind(prop: BooleanProp) {
    this?.isSelected = prop.get(props)
    this?.addActionListener {
      prop.set(props, isSelected)
    }
  }

  private fun initButtons() {
    wifiConnect?.addActionListener {
      if (props.isIpValid) {
        println("ip: ${props.ip}")
        try {
          conn?.connectToTcpDevice(InetSocketAddress(props.ip, props.port!!))
          loadDevices()
        } catch (ce: ConnectException) {
          ce.printStackTrace()
          startAdbDaemon {
            if (!it) {
              Notifier.notify("scrcpy ADB Failed", "ADB initialization failed, please run adb manually", NotificationType.ERROR)
            } else {
              wifiConnect?.doClick()
            }
          }
        }
      }
    }

    devicesRefresh?.addActionListener {
      loadDevices()
    }
  }

  private fun initAdb() {
    try {
      conn = JadbConnection()
    } catch (e: Exception) {
      e.printStackTrace()
      conn = null
      Notifier.notify("scrcpy ADB Failed", "ADB initialization failed, please run adb manually", NotificationType.ERROR)
    }
  }

  private fun loadDevices() {
    try {
      if (conn == null) return
      conn?.createDeviceWatcher(this)

      allDevices = conn!!.devices
      selectedDevices.clear()

      devices?.apply {
        model = object : DefaultTableModel(allDevices.map { arrayOf(false, it.serial, it.state.name, commandExecutors.containsKey(it.serial)) }.toTypedArray(), arrayOf("Select", "Serial", "State", "Running")) {
          override fun getColumnClass(p0: Int): Class<*> {
            return if (p0 == 0 || p0 == 3) Boolean::class.javaObjectType else String::class.java
          }

          override fun isCellEditable(p0: Int, p1: Int): Boolean {
            return p1 == 0
          }
        }.also { it.addTableModelListener(dataListener) }
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        columnModel.getColumn(0).apply {
          preferredWidth = 40
          maxWidth = 40
          width = 40
        }
        columnModel.getColumn(3).apply {
          preferredWidth = 40
          maxWidth = 40
          width = 40
        }
      }
    } catch (ce: ConnectException) {
      ce.printStackTrace()
      startAdbDaemon {
        if (!it) {
          Notifier.notify("scrcpy ADB Failed", "ADB initialization failed, please run adb manually", NotificationType.ERROR)
        } else {
          loadDevices()
        }
      }
    }
  }

  private fun startAdbDaemon(onComplete: (started: Boolean) -> Unit) {
    Thread {
      val returnCode = ProcessBuilder("adb", "devices").start().waitFor()
      if (returnCode == 0) {
        Notifier.notify("ADB Started", "ADB Daemon was started successfully", NotificationType.INFORMATION)
      }
      onComplete(returnCode == 0)
    }.start()
  }

  override fun onException(e: java.lang.Exception?) {
    e?.printStackTrace()
  }

  override fun onDetect(devices: MutableList<JadbDevice>?) {
    loadDevices()
  }

  inner class DeviceCheckListener : TableModelListener {
    override fun tableChanged(e: TableModelEvent?) {
      e?.let {
        if (e.firstRow == e.lastRow && e.column == 0 && e.source is TableModel) {
          val isChecked = (e.source as TableModel).getValueAt(e.firstRow, e.column) as Boolean
          val current = allDevices[e.firstRow].serial
          if (isChecked) {
            selectedDevices.add(current)
          } else if (selectedDevices.contains(current)) {
            selectedDevices.remove(current)
          }
          val intersection = commandExecutors.keys().toList().intersect(selectedDevices)
          run?.isEnabled = selectedDevices.isNotEmpty() && intersection.isEmpty()
          stop?.isEnabled = selectedDevices.isNotEmpty() && intersection.isNotEmpty()
        }
      }
    }
  }
}
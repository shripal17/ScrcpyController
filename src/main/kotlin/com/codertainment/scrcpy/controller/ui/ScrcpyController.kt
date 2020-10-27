package com.codertainment.scrcpy.controller.ui

import com.codertainment.scrcpy.controller.model.*
import com.codertainment.scrcpy.controller.util.*
import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationType
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.table.JBTable
import com.intellij.util.net.IOExceptionDialog
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import icons.Icons
import se.vidstige.jadb.*
import java.awt.Dimension
import java.io.File
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import javax.swing.*
import javax.swing.event.AncestorEvent
import javax.swing.event.AncestorListener
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
  private var deviceWatcher: DeviceWatcher? = null

  private var devicesRefresh: JButton? = null
  private var wifiConnect: JButton? = null
  private var wifiIp1: JFormattedTextField? = null
  private var wifiIp2: JFormattedTextField? = null
  private var wifiIp3: JFormattedTextField? = null
  private var wifiIp4: JFormattedTextField? = null
  private var wifiPort: JFormattedTextField? = null
  private var devices: JBTable? = null
  private var toWiFi: JButton? = null
  private var disconnect: JButton? = null
  private val IP_REGEX = Regex(
    "^(([0-9]{1,3}\\.){3})[0-9]{1,3}:[0-9]{1,5}\$"
  )

  //display
  private var maxResolution: JFormattedTextField? = null
  private var maxFps: JFormattedTextField? = null
  private var bitRate: JFormattedTextField? = null
  private var bitRateUnit: JComboBox<String>? = null
  private var videoOrientation: JComboBox<String>? = null
  private var crop: JCheckBox? = null
  private var cropX: JFormattedTextField? = null
  private var cropY: JFormattedTextField? = null
  private var cropXOffset: JFormattedTextField? = null
  private var cropYOffset: JFormattedTextField? = null

  //record
  private var enableRecording: JCheckBox? = null
  private var folder: TextFieldWithBrowseButton? = null
  private var fileName: JTextField? = null
  private var fileExtension: JComboBox<String>? = null
  private var disableMirroring: JCheckBox? = null

  // window
  private var windowTitle: JFormattedTextField? = null
  private var rotation: JComboBox<String>? = null
  private var borderless: JCheckBox? = null
  private var alwaysOnTop: JCheckBox? = null
  private var fullscreen: JCheckBox? = null
  private var position: JCheckBox? = null
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
  private var preferText: JCheckBox? = null
  private var disableScreenSaver: JCheckBox? = null
  private var disableKeyRepeat: JCheckBox? = null

  //help and about
  private var scrcpyButton: JButton? = null
  private var scrcpyControllerButton: JButton? = null
  private var shortcutsButton: JButton? = null
  private var donateButton: JButton? = null

  private val options = MutableDataSet().apply {
    set(Parser.EXTENSIONS, listOf(TablesExtension.create()))
  }
  var parser = Parser.builder(options).build()
  var renderer = HtmlRenderer.builder(options).build()

  private var run: JButton? = null
  private var stop: JButton? = null

  private var commandExecutors = ConcurrentHashMap<String, CommandExecutor>()
  private var allDevices = listOf<JadbDevice>()
  private var toWifi = arrayListOf<String>()
  private var toDisconnect = arrayListOf<String>()
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

    updateButtons()

    run?.addActionListener {
      try {
        selectedDevices.iterator().forEach {
          if (!commandExecutors.containsKey(it)) {
            val cmd = CommandExecutor(props.buildCommand(it), adbPath = props.adbPath) { exit, msg, fullOp, ioe ->
              exit?.let { _ ->
                commandExecutors.remove(it)
                println("Exited: $exit")
                loadDevices(false)
                if (exit != 0) {
                  val action = ScrcpyNotificationAction("View Error") { _, _ ->
                    IOExceptionDialog("scrcpy error", fullOp).showAndGet()
                    Unit
                  }
                  Notifier.notify("scrcpy failed", "An error occurred", NotificationType.ERROR, listOf(action))
                }
              }
              if (ioe) {
                commandExecutors.remove(it)
                val notifMsg = if (props.scrcpyPath != null && !props.scrcpyPath.isNullOrEmpty()) "scrcpy was not found in the configured path" else "scrcpy path is not configured"
                val actionText = if (props.scrcpyPath != null && !props.scrcpyPath.isNullOrEmpty()) "Re-configure" else "Configure"

                val action = ScrcpyNotificationAction(actionText) { _, _ ->
                  ShowSettingsUtil.getInstance().showSettingsDialog(null, ScrcpyControllerConfigurable::class.java)
                }
                Notifier.notify("scrcpy failed", notifMsg, NotificationType.ERROR, listOf(action))
              }
              msg?.let {
                println(it)
              }
            }
            commandExecutors[it] = cmd
            cmd.start()
            loadDevices(false)
          }
        }
      } catch (cce: ConcurrentModificationException) {
        cce.printStackTrace()
      }
    }

    stop?.addActionListener {
      selectedDevices.intersect(commandExecutors.keys().toList()).forEach {
        commandExecutors[it]?.interrupt()
        commandExecutors.remove(it)
        loadDevices(false)
      }
    }

    mainPanel?.addAncestorListener(object : AncestorListener {
      override fun ancestorAdded(p0: AncestorEvent?) {
        startDeviceWatcher()
        loadDevices(true)
      }

      override fun ancestorMoved(p0: AncestorEvent?) {
      }

      override fun ancestorRemoved(p0: AncestorEvent?) {
        stopDeviceWatcher()
      }
    })
  }

  private fun startDeviceWatcher() {
    if (toolWindow.isActive) {
      Thread {
        deviceWatcher?.watch()
      }.start()
    }
  }

  private fun stopDeviceWatcher() {
    Thread {
      deviceWatcher?.stop()
    }.start()
  }

  private fun initDisplayFields() {
    maxResolution.bindNumber(4320, ScrcpyProps::maxResolution)
    maxFps.bindNumber(60, ScrcpyProps::maxFps)
    bitRate.bindNumber(10_000, ScrcpyProps::bitRate)

    bitRateUnit?.selectedIndex = BitRateUnit.values().indexOf(props.bitRateUnit)
    bitRateUnit?.addActionListener {
      props.bitRateUnit = BitRateUnit.values()[bitRateUnit?.selectedIndex ?: 0]
    }

    videoOrientation?.selectedIndex = VideoOrientation.values().indexOf(props.videoOrientation)
    videoOrientation?.addActionListener {
      props.videoOrientation = VideoOrientation.values()[videoOrientation?.selectedIndex ?: 0]
    }

    crop.bind(ScrcpyProps::crop)

    cropX.bindNumber(7680, ScrcpyProps::cropX)
    cropY.bindNumber(4320, ScrcpyProps::cropY)
    cropXOffset.bindNumber(7680, ScrcpyProps::cropXOffset)
    cropYOffset.bindNumber(4320, ScrcpyProps::cropYOffset)
  }

  private fun initRecordingFields() {
    enableRecording.bind(ScrcpyProps::enableRecording)
    disableMirroring.bind(ScrcpyProps::disableMirroring)
    fileName.bindString(ScrcpyProps::recordingFileName)

    fileExtension?.selectedIndex = RecordingExtension.values().indexOf(props.recordingFileExtension)
    fileExtension?.addActionListener {
      props.recordingFileExtension = RecordingExtension.values()[fileExtension?.selectedIndex ?: 0]
    }

    folder?.textField?.bindString(ScrcpyProps::recordingPath)
    folder?.addBrowseFolderListener(object : TextBrowseFolderListener(FileChooserDescriptor(false, true, false, false, false, false)) {
      override fun getInitialFile(): VirtualFile? {
        return if (props.recordingPath == null) null else LocalFileSystem.getInstance().findFileByIoFile(File(props.recordingPath))
      }
    })
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

    position.bind(ScrcpyProps::position)

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
    preferText.bind(ScrcpyProps::preferText)
    disableScreenSaver.bind(ScrcpyProps::disableScreenSaver)
    disableKeyRepeat.bind(ScrcpyProps::disableKeyRepeat)
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

  private fun JTextField?.bindString(prop: StringProp) {
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
    scrcpyButton?.icon = Icons.GITHUB
    scrcpyControllerButton?.icon = Icons.GITHUB
    shortcutsButton?.icon = Icons.KEYBOARD
    donateButton?.icon = Icons.COFFEE

    scrcpyButton?.addActionListener {
      BrowserUtil.browse("http://github.com/Genymobile/scrcpy")
    }
    scrcpyControllerButton?.addActionListener {
      BrowserUtil.browse("http://github.com/shripal17/ScrcpyController")
    }
    shortcutsButton?.addActionListener {
      try {
        val scrcpy = URL("https://raw.githubusercontent.com/Genymobile/scrcpy/master/README.md").readText().split("## Shortcuts")[1].split("##")[0]
        TextDialog("scrcpy Shortcuts", renderer.render(parser.parse(scrcpy)), true).showAndGet()
      } catch (e: Exception) {
        e.printStackTrace()
        TextDialog("scrcpy Shortcuts", renderer.render(parser.parse(SHORTCUTS)), true).showAndGet()
      }
    }
    donateButton?.addActionListener {
      TextDialog("Scrcpy Controller - Donate", "UPI (India only) ID: <b>shripal17@okicici</b> (Shripal Jain)<br>PayPal: <a href=\"https://paypal.me/shripaul17\">https://paypal.me/shripaul17</a>", true).showAndGet()
    }

    wifiConnect?.addActionListener {
      if (props.isIpValid) {
        println("ip: ${props.ip}")
        connectAdbWifi(props.ip, props.port!!)
      }
    }

    devicesRefresh?.addActionListener {
      loadDevices(true)
    }

    disconnect?.addActionListener {
      if (toDisconnect.isEmpty()) return@addActionListener
      toDisconnect.forEach {
        runInBg {
          try {
            val parts = it.split(":")
            conn?.disconnectFromTcpDevice(InetSocketAddress(parts[0], parts.getOrNull(1)?.toInt() ?: 5555))
            loadDevices()
            toDisconnect.clear()
            updateButtons()
          } catch (e: Exception) {
            e.printStackTrace()
          }
        }
      }
    }

    toWiFi?.addActionListener {
      if (toWifi.isEmpty()) return@addActionListener
      toWifi.forEach { serial ->
        runInBg {
          try {
            CommandExecutor(listOf(props.adbPath(), "-s", serial, "tcpip", props.port?.toString() ?: "5555")) { exitCode, _, fullOp, _ ->
              exitCode?.let {
                if (it != 0) {
                  Notifier.notify(
                    "ADB to WiFi Failed", "Could not switch device $it to WiFi", NotificationType.ERROR,
                    listOf(ScrcpyNotificationAction("View Error") { _, _ ->
                      IOExceptionDialog("scrcpy error", fullOp).showAndGet()
                    })
                  )
                } else {
                  Thread.sleep(1500)
                  CommandExecutor(listOf(props.adbPath(), "-s", serial, "shell", "ip -f inet addr show wlan0")) { exitCode, _, fullOp, _ ->
                    exitCode?.let {
                      if (it == 0 && fullOp != null) {
                        val ip = fullOp.split("inet ")[1].split("/")[0]
                        connectAdbWifi(ip, props.port ?: 5555)
                      }
                    }
                  }.start()
                }
              }
            }.start()
          } catch (e: Exception) {
            e.printStackTrace()
          }
        }
      }
      toWifi.clear()
      updateButtons()
    }
  }

  private fun connectAdbWifi(ip: String, port: Int) {
    runInBg {
      try {
        conn?.connectToTcpDevice(InetSocketAddress(ip, port))
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
      } catch (ctrde: ConnectionToRemoteDeviceException) {
        Notifier.notify("ADB WiFi Connect Failed", ctrde.localizedMessage, NotificationType.ERROR)
      }
    }
  }

  private fun initAdb() {
    try {
      conn = JadbConnection()
      if (deviceWatcher == null) {
        deviceWatcher = conn?.createDeviceWatcher(this)
        startDeviceWatcher()
      }
    } catch (e: Exception) {
      e.printStackTrace()
      conn = null
      startAdbDaemon {
        if (!it) {
          Notifier.notify("scrcpy ADB Failed", "ADB initialization failed, please run adb manually", NotificationType.ERROR)
        } else {
          initAdb()
        }
      }
    }
  }

  private fun loadDevices(hardRefresh: Boolean = true) {
    try {
      if ((hardRefresh || allDevices.isEmpty()) && conn != null) {
        allDevices = conn!!.devices
      }
      selectedDevices.clear()
      toWifi.clear()
      toDisconnect.clear()

      updateButtons()

      devices?.apply {
        model = object : DefaultTableModel(allDevices.map {
          var serial = it.serial
          if (commandExecutors.containsKey(it.serial)) {
            serial += " •"
          }
          arrayOf(false, serial, it.state.name)
        }.toTypedArray(), arrayOf("Select", "Serial", "State")) {
          override fun getColumnClass(p0: Int): Class<*> {
            return if (p0 == 0) Boolean::class.javaObjectType else String::class.java
          }

          override fun isCellEditable(p0: Int, p1: Int): Boolean {
            return p1 == 0
          }
        }.also { it.addTableModelListener(dataListener) }
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        columnModel.getColumn(0).apply {
          preferredWidth = 60
          maxWidth = 60
          width = 60
        }
      }
      devices?.preferredScrollableViewportSize = Dimension(devices?.preferredSize?.width ?: 0, devices?.rowHeight ?: 0 * 3)
    } catch (ce: ConnectException) {
      ce.printStackTrace()
      startAdbDaemon {
        if (!it) {
          Notifier.notify("scrcpy ADB Failed", "ADB initialization failed, please run adb manually", NotificationType.ERROR)
        } else {
          loadDevices()
        }
      }
    } catch (jadbe: JadbException) {
      jadbe.printStackTrace()
      Notifier.notify("ADB Error", jadbe.localizedMessage ?: "Something went wrong", NotificationType.ERROR)
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
          val c = allDevices[e.firstRow]
          if (c.state != JadbDevice.State.Device) return@let
          val current = allDevices[e.firstRow].serial
          if (isChecked) {
            selectedDevices.add(current)
          } else if (selectedDevices.contains(current)) {
            selectedDevices.remove(current)
          }
          if (current.matches(IP_REGEX)) {
            if (isChecked) {
              toDisconnect.add(current)
            } else {
              toDisconnect.remove(current)
            }
          } else {
            if (isChecked) {
              toWifi.add(current)
            } else {
              toWifi.remove(current)
            }
          }
          updateButtons()
        }
      }
    }
  }

  private fun updateButtons() {
    val intersection = commandExecutors.keys().toList().intersect(selectedDevices)
    run?.isEnabled = selectedDevices.isNotEmpty() && intersection.isEmpty()
    stop?.isEnabled = selectedDevices.isNotEmpty() && intersection.isNotEmpty()
    disconnect?.isEnabled = toDisconnect.isNotEmpty()
    toWiFi?.isEnabled = toWifi.isNotEmpty()
  }

  @Throws(Exception::class)
  private inline fun runInBg(crossinline toRun: () -> Unit) {
    SwingUtilities.invokeLater {
      Thread {
        toRun()
      }.start()
    }
  }
}
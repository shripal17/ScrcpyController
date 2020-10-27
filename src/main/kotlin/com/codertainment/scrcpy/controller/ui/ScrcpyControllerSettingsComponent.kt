package com.codertainment.scrcpy.controller.ui

import com.codertainment.scrcpy.controller.model.RenderDriver
import com.codertainment.scrcpy.controller.model.ScrcpyProps
import com.codertainment.scrcpy.controller.model.Verbosity
import com.codertainment.scrcpy.controller.util.CommandExecutor
import com.codertainment.scrcpy.controller.util.intOrNull
import com.codertainment.scrcpy.controller.util.numberFormatter
import com.codertainment.scrcpy.controller.util.onTextChangedListener
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import javax.swing.*

/*
 * Created by Shripal Jain
 * on 21/06/2020
 */
internal class ScrcpyControllerSettingsComponent {

  private val props = ScrcpyProps.getInstance()
  private var modProps = props.copy()

  var settingsPanel: JPanel? = null
  private var scrcpyPath: TextFieldWithBrowseButton? = null
  private var adbPath: TextFieldWithBrowseButton? = null
  private var pathTest: JButton? = null
  private var adbTest: JButton? = null

  private var forceAdbForward: JCheckBox? = null
  private var noMipmaps: JCheckBox? = null
  private var startPort: JFormattedTextField? = null
  private var endPort: JFormattedTextField? = null
  private var pushTarget: JFormattedTextField? = null
  private var renderDriver: JComboBox<String>? = null
  private var verbosity: JComboBox<String>? = null
  private var displayId: JFormattedTextField? = null
  private var shortcutMod: JFormattedTextField? = null

  init {
    scrcpyPath?.addBrowseFolderListener(object : TextBrowseFolderListener(FileChooserDescriptor(false, true, false, false, false, false)) {
      override fun getInitialFile(): VirtualFile? {
        return if (modProps.scrcpyPath == null) null else LocalFileSystem.getInstance().findFileByIoFile(File(modProps.scrcpyPath))
      }
    })
    adbPath?.addBrowseFolderListener(object : TextBrowseFolderListener(FileChooserDescriptor(true, false, false, false, false, false)) {
      override fun getInitialFile(): VirtualFile? {
        return if (modProps.scrcpyPath == null) null else LocalFileSystem.getInstance().findFileByIoFile(File(modProps.adbPath))
      }
    })
    scrcpyPath?.textField?.bindString(ScrcpyProps::scrcpyPath)
    adbPath?.textField?.bindString(ScrcpyProps::adbPath)
    forceAdbForward?.bind(ScrcpyProps::forceAdbForward)
    noMipmaps?.bind(ScrcpyProps::noMipmaps)

    startPort?.bindNumber(65535, ScrcpyProps::startPort)
    endPort?.bindNumber(65535, ScrcpyProps::endPort)

    pushTarget?.bindString(ScrcpyProps::pushTarget)

    renderDriver?.addActionListener {
      modProps.renderDriver = RenderDriver.values()[renderDriver?.selectedIndex ?: 0]
    }
    verbosity?.addActionListener {
      modProps.verbosity = Verbosity.values()[verbosity?.selectedIndex ?: 1]
    }

    displayId?.bindNumber(9, ScrcpyProps::displayId)

    shortcutMod?.bindString(ScrcpyProps::shortcutMod)

    pathTest?.addActionListener {
      CommandExecutor(pathTestCommand(), modalityState = ModalityState.current()) { e, _, fullOp, ioe ->
        if (ioe) {
          TextDialog("Path Invalid", "Provided path does not contain scrcpy executable", false).showAndGet()
        } else if (e != null) {
          val output = if (fullOp != null) {
            "scrcpy Path is valid<br><br>Version Info:<br>${fullOp.replace("\n", "<br>")}"
          } else {
            "Path is valid"
          }
          TextDialog("Path Valid", output, false).showAndGet()
        }
      }.start()
    }
    adbTest?.addActionListener {
      CommandExecutor(adbTestCommand(), modalityState = ModalityState.current()) { e, _, fullOp, ioe ->
        if (ioe) {
          TextDialog("Invalid Executable", "Provided path does not contain adb executable", false).showAndGet()
        } else if (e != null) {
          val output = if (fullOp != null) {
            "adb executable is valid<br><br>Version Info:<br>${fullOp.replace("\n", "<br>")}"
          } else {
            "Valid executable"
          }
          TextDialog("Valid executable", output, false).showAndGet()
        }
      }.start()
    }
  }

  fun init() {
    modProps = props.copy()
    scrcpyPath?.textField?.text = modProps.scrcpyPath ?: ""
    adbPath?.textField?.text = modProps.adbPath ?: ""
    forceAdbForward?.isSelected = modProps.forceAdbForward
    noMipmaps?.isSelected = modProps.noMipmaps
    startPort?.text = modProps.startPort?.toString() ?: ""
    endPort?.text = modProps.endPort?.toString() ?: ""
    pushTarget?.text = modProps.pushTarget ?: ""
    displayId?.text = modProps.displayId?.toString() ?: ""
    shortcutMod?.text = modProps.shortcutMod ?: ""

    renderDriver?.selectedIndex = RenderDriver.values().indexOf(modProps.renderDriver)
    verbosity?.selectedIndex = Verbosity.values().indexOf(modProps.verbosity)
  }

  fun isModified() = props != modProps

  fun apply() {
    modProps.apply {
      props.scrcpyPath = scrcpyPath
      props.adbPath = adbPath
      props.forceAdbForward = forceAdbForward
      props.noMipmaps = noMipmaps
      props.startPort = startPort
      props.endPort = endPort
      props.pushTarget = pushTarget
      props.displayId = displayId
      props.renderDriver = renderDriver
      props.verbosity = verbosity
      props.shortcutMod = shortcutMod
    }
  }

  private fun JFormattedTextField?.bindNumber(max: Int? = null, p: IntProp) {
    numberFormatter(max ?: Integer.MAX_VALUE, p.get(modProps))
    onTextChangedListener {
      p.set(modProps, it.intOrNull())
    }
  }

  private fun JTextField?.bindString(prop: StringProp) {
    this?.text = prop.get(modProps)
    onTextChangedListener {
      prop.set(modProps, it)
    }
  }

  private fun JCheckBox?.bind(prop: BooleanProp) {
    this?.isSelected = prop.get(modProps)
    this?.addActionListener {
      prop.set(modProps, isSelected)
    }
  }


  private fun pathTestCommand() = arrayListOf<String>().apply {
    val path = scrcpyPath?.textField?.text
    if (path != null) {
      add(path + File.separator + "scrcpy")
    } else {
      add("scrcpy")
    }
    add("-v")
  }

  private fun adbTestCommand() = arrayListOf<String>().apply {
    val adb = adbPath?.textField?.text
    if (adb != null) {
      add(adb)
    } else {
      add("adb")
    }
    add("version")
  }
}
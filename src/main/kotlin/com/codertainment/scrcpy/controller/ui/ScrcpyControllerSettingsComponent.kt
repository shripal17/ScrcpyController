package com.codertainment.scrcpy.controller.ui

import com.codertainment.scrcpy.controller.model.RenderDriver
import com.codertainment.scrcpy.controller.model.ScrcpyProps
import com.codertainment.scrcpy.controller.model.Verbosity
import com.codertainment.scrcpy.controller.util.CommandExecutor
import com.codertainment.scrcpy.controller.util.intOrNull
import com.codertainment.scrcpy.controller.util.numberFormatter
import com.codertainment.scrcpy.controller.util.onTextChangedListener
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
  private var pathTest: JButton? = null

  private var forceAdbForward: JCheckBox? = null
  private var noMipmaps: JCheckBox? = null
  private var startPort: JFormattedTextField? = null
  private var endPort: JFormattedTextField? = null
  private var pushTarget: JFormattedTextField? = null
  private var renderDriver: JComboBox<String>? = null
  private var verbosity: JComboBox<String>? = null
  private var displayId: JFormattedTextField? = null

  init {
    scrcpyPath?.addBrowseFolderListener(object : TextBrowseFolderListener(FileChooserDescriptor(false, true, false, false, false, false)) {
      override fun getInitialFile(): VirtualFile? {
        return if (modProps.scrcpyPath == null) null else LocalFileSystem.getInstance().findFileByIoFile(File(modProps.scrcpyPath))
      }
    })
    scrcpyPath?.textField?.bindString(ScrcpyProps::scrcpyPath)
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

    pathTest?.addActionListener {
      CommandExecutor(modProps.pathTestCommand()) { e, _, fullOp, ioe ->
        if (ioe) {
          TextDialog("Path Invalid", "Provided path does not contain scrcpy executable", false).showAndGet()
        } else if (e != null) {
          TextDialog("Path Valid", "Path is valid", false).showAndGet()
        }
      }.start()
    }
  }

  fun init() {
    modProps = props.copy()
    scrcpyPath?.textField?.text = modProps.scrcpyPath ?: ""
    forceAdbForward?.isSelected = modProps.forceAdbForward
    noMipmaps?.isSelected = modProps.noMipmaps
    startPort?.text = modProps.startPort?.toString() ?: ""
    endPort?.text = modProps.endPort?.toString() ?: ""
    pushTarget?.text = modProps.pushTarget ?: ""
    displayId?.text = modProps.displayId?.toString() ?: ""

    renderDriver?.selectedIndex = RenderDriver.values().indexOf(modProps.renderDriver)
    verbosity?.selectedIndex = Verbosity.values().indexOf(modProps.verbosity)
  }

  fun isModified() = props != modProps

  fun apply() {
    modProps.apply {
      props.scrcpyPath = scrcpyPath
      props.forceAdbForward = forceAdbForward
      props.noMipmaps = noMipmaps
      props.startPort = startPort
      props.endPort = endPort
      props.pushTarget = pushTarget
      props.displayId = displayId
      props.renderDriver = renderDriver
      props.verbosity = verbosity
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
}
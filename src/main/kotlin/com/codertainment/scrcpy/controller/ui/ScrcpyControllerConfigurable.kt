package com.codertainment.scrcpy.controller.ui

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

/*
 * Created by Shripal Jain
 * on 21/06/2020
 */

class ScrcpyControllerConfigurable : Configurable {

  private val settingsComponent = ScrcpyControllerSettingsComponent()

  override fun isModified(): Boolean = settingsComponent.isModified()

  override fun getDisplayName(): String = "Scrcpy Controller"

  override fun apply() {
    settingsComponent.apply()
  }

  override fun createComponent(): JComponent? = settingsComponent.settingsPanel

  override fun reset() {
    settingsComponent.init()
  }

  override fun disposeUIResources() {
    settingsComponent.settingsPanel = null
  }
}
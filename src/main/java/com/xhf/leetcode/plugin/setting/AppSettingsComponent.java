// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.xhf.leetcode.plugin.setting;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.xhf.leetcode.plugin.utils.LangType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

/**
 * Supports creating and managing a {@link JPanel} for the Settings Dialog.
 */
public class AppSettingsComponent {

  private final JPanel myMainPanel;
  private final JBTextField myUserNameText = new JBTextField();

  private final JBTextField filePathText = new JBTextField();

  private final JBCheckBox myIdeaUserStatus = new JBCheckBox("IntelliJ IDEA user");

  // lang type
  private final ComboBox<String> myLangType = new ComboBox<>();
  // file path
  private final TextFieldWithBrowseButton myFileBrowserBtn = new TextFieldWithBrowseButton();

  public AppSettingsComponent() {
    initComponent();
    myMainPanel = FormBuilder.createFormBuilder()
//        .addLabeledComponent(new JBLabel("User name:"), myUserNameText, 1, false)
        .addLabeledComponent(new JBLabel("lang type"), myLangType, 1, false)
        .addLabeledComponent(new JBLabel("Store path:"), myFileBrowserBtn, 1, false)
//        .addComponent(myIdeaUserStatus, 1)
        .addComponentFillVertically(new JPanel(), 0)
        .getPanel();
  }

  private void initComponent() {
    // init langType
    for (LangType langType : LangType.values()) {
      myLangType.addItem(langType.getLangType());
    }
    // init file chooser
    myFileBrowserBtn .addBrowseFolderListener(
            new TextBrowseFolderListener(
                    FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
            ){});
    myFileBrowserBtn.setText(AppSettings.getInstance().getFilePath());
  }

  public JPanel getPanel() {
    return myMainPanel;
  }

  public JComponent getPreferredFocusedComponent() {
    return myUserNameText;
  }

  @NotNull
  public String getUserNameText() {
    return myUserNameText.getText();
  }

  public void setUserNameText(@NotNull String newText) {
    myUserNameText.setText(newText);
  }

  public boolean getIdeaUserStatus() {
    return myIdeaUserStatus.isSelected();
  }

  public void setIdeaUserStatus(boolean newStatus) {
    myIdeaUserStatus.setSelected(newStatus);
  }

  public void setLangType(String langType) {
    myLangType.setItem(langType);
  }

  public String getLangType() {
    return Objects.requireNonNull(myLangType.getSelectedItem()).toString();
  }
  
  public void setFilePath(String filePath) {
    myFileBrowserBtn.setText(filePath);
  }

  public String getFilePath() {
    return myFileBrowserBtn.getText();
  }
}

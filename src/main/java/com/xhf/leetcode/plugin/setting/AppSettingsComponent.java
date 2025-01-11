package com.xhf.leetcode.plugin.setting;

import com.intellij.icons.AllIcons;
import com.intellij.ide.HelpTooltip;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.LangType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
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

  private static final String LANG_TYPE_HELP_TEXT = "选择支持的编程语言类型, 该设置将决定后续创建,提交的代码类型. 此外, 语言类型将决定debug功能启动的执行器类型";
  private static final String STORE_PATH_HELP_TEXT = "选择文件的存储路径. 该参数将影响后续代码文件创建的位置";

  /*---------debug----------*/
  private final DebugPanel debugPanel = new DebugPanel();



  public AppSettingsComponent() {
    initComponent();
    myMainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(new JBLabel("Lang type"), InnerHelpTooltip.FlowLayout(FlowLayout.LEFT).add(myLangType).addHelp(LANG_TYPE_HELP_TEXT).getTargetComponent(), 1, false)
            .addLabeledComponent(new JBLabel("Store path"), InnerHelpTooltip.BoxLayout().add(myFileBrowserBtn).addHelp(STORE_PATH_HELP_TEXT).getTargetComponent(), 1, false)
            .addComponent((JComponent) Box.createVerticalStrut(10))
            .addComponent(createSeparatorWithText("debug configuration"))
            .addComponent((JComponent) Box.createVerticalStrut(5))
            .addComponent(debugPanel)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
  }

  // 创建一个带文字的分割线
  public JComponent createSeparatorWithText(String text) {
    TitledSeparator titledSeparator = new TitledSeparator(text);
    titledSeparator.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Constants.BACKGROUND_COLOR));
    titledSeparator.setTitleFont(new Font(titledSeparator.getFont().getName(), Font.BOLD, 14));
    return titledSeparator;
  }
  private void initComponent() {
    // init langType
    for (LangType langType : LangType.values()) {
      myLangType.addItem(langType.getLangType());
    }
    // init file chooser
    myFileBrowserBtn.addBrowseFolderListener(
            new TextBrowseFolderListener(
                    FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
            ) {
            });
    myFileBrowserBtn.setText(AppSettings.getInstance().getFilePath());
    myFileBrowserBtn.setEditable(false);
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

  public void setReadTypeName(String readTypeName) {
    debugPanel.setReadTypeName(readTypeName);
  }

  public void setOutputTypeName(String outputTypeName) {
    debugPanel.setOutputTypeName(outputTypeName);
  }

  public String getReadTypeName() {
    return debugPanel.getReadTypeName();
  }

  public String getOutputTypeName() {
    return debugPanel.getOutputTypeName();
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
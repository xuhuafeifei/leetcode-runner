package com.xhf.leetcode.plugin.setting;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.xhf.leetcode.plugin.debug.output.OutputType;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.LangType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
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

  /*---------debug----------*/
  private final DebugPanel debugPanel = new DebugPanel();

  public AppSettingsComponent() {
    initComponent();
    myMainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(new JBLabel("Lang type"), myLangType, 1, false)
            .addLabeledComponent(new JBLabel("Store path:"), myFileBrowserBtn, 1, false)
            .addComponent(createSeparatorWithText("debug 配置"))
            .addComponent(debugPanel)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
  }

  // 创建一个带文字的分割线
  public JComponent createSeparatorWithText(String text) {
    JPanel panel = new JPanel(new GridBagLayout()); // 使用 GridBagLayout 实现居中布局
    panel.setOpaque(false); // 背景透明

    // 创建分割线和文字
    JSeparator leftLine = new JSeparator(SwingConstants.HORIZONTAL);
    JSeparator rightLine = new JSeparator(SwingConstants.HORIZONTAL);
    JLabel label = new JLabel(text);

    // 设置分割线和文字的颜色适配当前主题
    Color separatorColor = UIManager.getColor("Separator.foreground");
    if (separatorColor == null) {
      separatorColor = Constants.BACKGROUND_COLOR; // 默认颜色
    }
    leftLine.setForeground(separatorColor);
    rightLine.setForeground(separatorColor);

    // 使用 GridBagConstraints 对齐布局
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;

    // 左边分割线
    gbc.gridx = 0;
    panel.add(leftLine, gbc);

    // 中间文字
    gbc.gridx = 1;
    gbc.weightx = 0; // 文字部分不扩展
    gbc.insets = JBUI.insets(0, 5); // 文字两侧间距
    panel.add(label, gbc);

    // 右边分割线
    gbc.gridx = 2;
    gbc.weightx = 1.0; // 右侧分割线扩展
    panel.add(rightLine, gbc);

    return panel;
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
package com.xhf.leetcode.plugin.setting;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.xhf.leetcode.plugin.bus.ClearCacheEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.model.i18nTypeEnum;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.LangType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

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
  private static final String REPOSITION_HELP_TEXT = "选择reposition功能文件打开方式. 如果是'" + AppSettings.REPOSITION_DEFAULT + "', 重定位后系统将会依据文件代表的语言类型重新打开文件; 如果是'" + AppSettings.REPOSITION_SETTING + "', 重定位后系统将会依据设置中语言类型打开文件";
  private static final String LANGUAGE_HELP_TEXT = "选择Leetcode-Runner显示的语言类型";

  /*---------debug----------*/
  private final DebugPanel debugPanel = new DebugPanel();

  /*---------通用配置--------*/
  private final ComboBox<String> reposition = new ComboBox<>();
  private final ComboBox<String> language   = new ComboBox<>();


  public AppSettingsComponent() {
    initComponent();
    myMainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(new JBLabel("Lang type "), InnerHelpTooltip.FlowLayout(FlowLayout.LEFT).add(myLangType).addHelp(LANG_TYPE_HELP_TEXT).getTargetComponent(), 1, false)
            .addLabeledComponent(new JBLabel("Store path"), InnerHelpTooltip.BoxLayout().add(myFileBrowserBtn).addHelp(STORE_PATH_HELP_TEXT).getTargetComponent(), 1, false)
            .addComponent((JComponent) Box.createVerticalStrut(10))
            .addComponent(createSeparatorWithText("debug configuration"))
            .addComponent((JComponent) Box.createVerticalStrut(5))
            .addComponent(debugPanel)
            .addComponent((JComponent) Box.createVerticalStrut(10))
            .addComponent(createSeparatorWithText("others configuration"))
            .addComponent((JComponent) Box.createVerticalStrut(5))
            .addLabeledComponent(new JBLabel("Reposition"), InnerHelpTooltip.FlowLayout(FlowLayout.LEFT).add(reposition).addHelp(REPOSITION_HELP_TEXT).getTargetComponent(), 1, false)
            .addLabeledComponent(new JBLabel("Language  "), InnerHelpTooltip.FlowLayout(FlowLayout.LEFT).add(language).addHelp(LANGUAGE_HELP_TEXT).getTargetComponent(), 1, false)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
    LCEventBus.getInstance().register(this);
  }

  @Subscribe
  public void clearCacheEventListener(ClearCacheEvent event) {
    this.myLangType.setSelectedIndex(-1);
    this.myFileBrowserBtn.setText("");
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

    reposition.addItem("按照文件代表的语言类型");
    reposition.addItem("按照设置中的语言类型");

    language.addItem(i18nTypeEnum.ZH.getValue());
    language.addItem(i18nTypeEnum.EN.getValue());
    // 增加一个点击事件
    language.addActionListener(e -> {
      String selectedItem = (String) language.getSelectedItem();
      if (StringUtils.isNotBlank(selectedItem) && !selectedItem.equals(AppSettings.getInstance().getLocale())) {
        JOptionPane.showMessageDialog(null, "语言设置生效需要重启IDE, 请您保存设置并重启");
      }
    });
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
    if (StringUtils.isBlank(langType)) {
      myLangType.setSelectedIndex(-1);
      return;
    }
    myLangType.setItem(langType);
  }

  public String getLangType() {
    Object selectedItem = myLangType.getSelectedItem();
    if (selectedItem == null) {
      return "";
    }
    return (String) selectedItem;
  }

  public void setFilePath(String filePath) {
    myFileBrowserBtn.setText(filePath);
  }

  public String getFilePath() {
    return myFileBrowserBtn.getText();
  }

  public void setReposition(String rePositionSetting) {
    if (StringUtils.isBlank(rePositionSetting)) {
      reposition.setItem(AppSettings.REPOSITION_DEFAULT);
      return;
    }
    reposition.setItem(rePositionSetting);
  }

  public String getReposition() {
    Object selectedItem = reposition.getSelectedItem();
    if (selectedItem == null) {
      return AppSettings.REPOSITION_DEFAULT;
    }
    return (String) selectedItem;
  }

  public void setLocale(String locale) {
    if (StringUtils.isBlank(locale)) {
      language.setItem(i18nTypeEnum.ZH.getValue());
      return;
    }
    language.setItem(locale);
  }

  public String getLocale() {
    Object selectedItem = language.getSelectedItem();
    if (selectedItem == null) {
      return i18nTypeEnum.EN.getValue();
    }
    return (String) selectedItem;
  }
}
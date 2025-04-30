package com.xhf.leetcode.plugin.setting;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.OnOffButton;
import com.intellij.util.ui.FormBuilder;
import com.xhf.leetcode.plugin.bus.ClearCacheEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.NoLanguageError;
import com.xhf.leetcode.plugin.model.I18nTypeEnum;
import com.xhf.leetcode.plugin.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.security.NoSuchAlgorithmException;

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

  private static final String LANG_TYPE_HELP_TEXT = BundleUtils.i18nHelper(
          "选择支持的编程语言类型, 该设置将决定后续创建,提交的代码类型. 此外, 语言类型将决定debug功能启动的执行器类型"
          , "Select the programming language type, which will determine the type of code created and submitted subsequently. In addition, the language type will determine the execution environment of the debug function."
  );

  private static final String STORE_PATH_HELP_TEXT = BundleUtils.i18nHelper(
          "选择文件的存储路径. 该参数将影响后续代码文件创建的位置"
          ,"Select the storage path of the file. This parameter will affect the location of the code file created subsequently."
  );

  private static final String REPOSITION_HELP_TEXT = BundleUtils.i18nHelper(
          "选择reposition功能文件打开方式. 如果是'"
                  + AppSettings.REPOSITION_DEFAULT
                  + "', 重定位后系统将会依据文件代表的语言类型重新打开文件; 如果是'"
                  + AppSettings.REPOSITION_SETTING +
                  "', 重定位后系统将会依据设置中语言类型打开文件",
          "Select the reposition function file opening method. If it is '"
                  + AppSettings.REPOSITION_DEFAULT
                  + "', the system will re-open the file according to the language type represented by the file; if it is '"
                  + AppSettings.REPOSITION_SETTING
                  + "', the system will open the file according to the language type set in the setting."
  );

  private static final String LANGUAGE_HELP_TEXT = BundleUtils.i18nHelper(
          "选择Leetcode-Runner显示的语言类型"
          ,"Select the language type displayed by Leetcode-Runner"
  );

  private static final String FLOATING_TOOLBAR_HELP_TEXT = BundleUtils.i18nHelper(
      "是否启用悬浮工具栏, 该功能将在代码编辑区域的右上角显示一个悬浮工具栏, 方便快速操作, 如: 代码运行, 输入测试案例等.",
      "Whether to enable the floating toolbar, which will display a floating toolbar in the upper right corner of the code editing area, to facilitate quick operation, such as code running, input test cases, etc."
  );

  /*---------debug----------*/
  private final DebugPanel debugPanel = new DebugPanel();

  /*---------通用配置--------*/
  private final ComboBox<String> reposition = new ComboBox<>();
  private final ComboBox<String> language   = new ComboBox<>();
  private JBTextField secretText;
  private OnOffButton onOffButton;
  private OnOffButton enableFloatingToolbarBtn;


  public AppSettingsComponent() {
    initComponent();
    myMainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(new JBLabel(BundleUtils.i18nHelper("编程语言", "Lang  type")), InnerHelpTooltip.FlowLayout(FlowLayout.LEFT).add(myLangType).addHelp(LANG_TYPE_HELP_TEXT).getTargetComponent(), 1, false)
            .addLabeledComponent(new JBLabel(BundleUtils.i18nHelper("存储路径", "Store path")), InnerHelpTooltip.BoxLayout().add(myFileBrowserBtn).addHelp(STORE_PATH_HELP_TEXT).getTargetComponent(), 1, false)
            .addComponent((JComponent) Box.createVerticalStrut(10))
//            .addComponent(createSeparatorWithText(BundleUtils.i18nHelper("debug 配置", "Debug configuration")))
            .addComponent(createSeparatorWithText("Debug"))
            .addComponent((JComponent) Box.createVerticalStrut(5))
            .addComponent(debugPanel)
            .addComponent((JComponent) Box.createVerticalStrut(10))
//            .addComponent(createSeparatorWithText(BundleUtils.i18nHelper("通用配置", "General configuration")))
            .addComponent(createSeparatorWithText("General"))
            .addComponent((JComponent) Box.createVerticalStrut(5))
            .addLabeledComponent(new JBLabel(BundleUtils.i18nHelper("重定位", "Reposition")), InnerHelpTooltip.FlowLayout(FlowLayout.LEFT).add(reposition).addHelp(REPOSITION_HELP_TEXT).getTargetComponent(), 1, false)
            .addLabeledComponent(new JBLabel(BundleUtils.i18nHelper("语言和地区", "Language  ")), InnerHelpTooltip.FlowLayout(FlowLayout.LEFT).add(language).addHelp(LANGUAGE_HELP_TEXT).getTargetComponent(), 1, false)
            .addComponent(createEncryptPanel())
            .addLabeledComponent(new JBLabel(BundleUtils.i18nHelper("是否启用悬浮工具栏", "enable floating toolbar or not")), InnerHelpTooltip.FlowLayout(FlowLayout.LEFT).add(enableFloatingToolbarBtn).addHelp(FLOATING_TOOLBAR_HELP_TEXT).getTargetComponent(), 1, false)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
    LCEventBus.getInstance().register(this);
  }

  /**
   * 加密面板
   */
  private JComponent createEncryptPanel() {
    JPanel jPanel = new JPanel();
    jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));

    // 密钥
    JBLabel secretLabel = new JBLabel(BundleUtils.i18n("setting.leetcode.secret"));
    secretLabel.setVisible(false);

    this.secretText = new JBTextField();
    secretText.setVisible(false);
    secretText.setEditable(false);
    secretText.setEnabled(false);

    JButton generateBtn = new JButton(BundleUtils.i18n("setting.leetcode.generate"));
    generateBtn.setVisible(false);
    generateBtn.setEnabled(false);

    AppSettings appSettings = AppSettings.getInstance();
    if (appSettings.getEncryptOrNot()) {
      secretLabel.setVisible(true);
      secretText.setVisible(true);
      generateBtn.setVisible(true);
      generateBtn.setEnabled(true);
    }

    generateBtn.addActionListener(e -> {
      try {
        String text = secretText.getText();
        if (StringUtils.isNotBlank(text)) {
          int i = ViewUtils.getDialogWrapper(
                  BundleUtils.i18n("setting.leetcode.has.secret")
          ).getExitCode();

          if (i != DialogWrapper.OK_EXIT_CODE) {
            return;
          }
        }
        String key = AESUtils.generateKey();
        secretText.setText(key);
      } catch (NoSuchAlgorithmException ex) {
        ViewUtils.showError(BundleUtils.i18n("action.leetcode.plugin.error"));
        LogUtils.error(ex);
      }
    });

    // 开关
    this.onOffButton = new OnOffButton();
    onOffButton.addActionListener(e -> {
      // 如何判断当前是on还是off
      if (onOffButton.isSelected()) {
        secretLabel.setVisible(true);
        secretText.setVisible(true);
        generateBtn.setVisible(true);
        generateBtn.setEnabled(true);
        if (StringUtils.isBlank(secretText.getText())) {
          ViewUtils.getDialogWrapper( BundleUtils.i18n("setting.leetcode.secret.isnull"));
        }
      } else {
        secretLabel.setVisible(false);
        secretText.setVisible(false);
        generateBtn.setVisible(false);
        generateBtn.setEnabled(false);
      }
    });

    jPanel.add(new JBLabel(BundleUtils.i18n("setting.leetcode.encrypt")));
    jPanel.add(onOffButton);

    jPanel.add(secretLabel);
    jPanel.add(secretText);
    jPanel.add(generateBtn);

    return jPanel;
  }

  @Subscribe
  public void clearCacheEventListener(ClearCacheEvent event) {
    this.myLangType.setSelectedIndex(-1);
    this.myFileBrowserBtn.setText("");
    this.onOffButton.setSelected(false);
    this.secretText.setText("");
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

    reposition.addItem(BundleUtils.i18nHelper(LanguageConvertor.REPOSITION_DEFAULT_ZH, LanguageConvertor.REPOSITION_DEFAULT_EN));
    reposition.addItem(BundleUtils.i18nHelper(LanguageConvertor.REPOSITION_SETTING_ZH, LanguageConvertor.REPOSITION_SETTING_EN));

    language.addItem(I18nTypeEnum.ZH.getValue());
    language.addItem(I18nTypeEnum.EN.getValue());
    // 增加一个点击事件
    language.addActionListener(e -> {
      String selectedItem = (String) language.getSelectedItem();
      if (StringUtils.isNotBlank(selectedItem) && !selectedItem.equals(AppSettings.getInstance().getLocale())) {
        ViewUtils.getDialogWrapper( BundleUtils.i18nHelper("语言设置生效需要重启IDE, 请您保存设置并重启", "Language settings take effect after restarting IDE, please save the settings and restart"));
      }
    });

    enableFloatingToolbarBtn = new OnOffButton();
    enableFloatingToolbarBtn.setSelected(true);
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

    I18nTypeEnum i18N = I18nTypeEnum.getI18N(AppSettings.getInstance().getLocale());
    if (i18N == I18nTypeEnum.ZH) {
      try {
        reposition.setItem(LanguageConvertor.toZh(rePositionSetting));
      } catch (NoLanguageError e) {
        LogUtils.warn(DebugUtils.getStackTraceAsString(e));
        reposition.setSelectedIndex(reposition.getItemCount() - 1);
      }
    } else {
      try {
        reposition.setItem(LanguageConvertor.toEn(rePositionSetting));
      } catch (NoLanguageError e) {
        LogUtils.warn(DebugUtils.getStackTraceAsString(e));
        reposition.setSelectedIndex(reposition.getItemCount() - 1);
      }
    }
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
      language.setItem(I18nTypeEnum.ZH.getValue());
      return;
    }
    language.setItem(locale);
  }

  public String getLocale() {
    Object selectedItem = language.getSelectedItem();
    if (selectedItem == null) {
      return I18nTypeEnum.EN.getValue();
    }
    return (String) selectedItem;
  }

  public String getSecretKey() {
    return secretText.getText();
  }

  public void setSecretKey(String secretKey) {
    secretText.setText(secretKey);
  }

  public boolean getEncryptOrNot() {
    return onOffButton.isSelected();
  }

  public void setEncryptOrNot(boolean encryptOrNot) {
    onOffButton.setSelected(encryptOrNot);
    onOffButton.dispatchEvent(new ActionEvent(onOffButton, ActionEvent.ACTION_PERFORMED, null));
  }

  public void setEnableFloatingToolbar(boolean enableFloatingToolbar) {
    enableFloatingToolbarBtn.setSelected(enableFloatingToolbar);
    enableFloatingToolbarBtn.dispatchEvent(new ActionEvent(enableFloatingToolbarBtn, ActionEvent.ACTION_PERFORMED, null));
  }

  public boolean getEnableFloatingToolbar() {
    return enableFloatingToolbarBtn.isSelected();
  }
}
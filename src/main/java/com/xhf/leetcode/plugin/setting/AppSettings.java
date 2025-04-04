package com.xhf.leetcode.plugin.setting;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.xhf.leetcode.plugin.bus.ClearCacheEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.debug.output.OutputType;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.NoLanguageError;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.model.I18nTypeEnum;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LangType;
import com.xhf.leetcode.plugin.utils.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@State(
        name = "AppSettings",
        storages = @Storage("SdkSettingsPlugin.xml")
)
public final class AppSettings
        implements PersistentStateComponent<AppSettings.State> {

  public static final String EMPTY_FILE_PATH = "";
  public static final String EMPTY_LANGUAGE_TYPE = "";

  public static final String REPOSITION_DEFAULT = BundleUtils.i18nHelper(LanguageConvertor.REPOSITION_DEFAULT_ZH, LanguageConvertor.REPOSITION_DEFAULT_EN);
  public static final String REPOSITION_SETTING = BundleUtils.i18nHelper(LanguageConvertor.REPOSITION_SETTING_ZH, LanguageConvertor.REPOSITION_SETTING_EN);

  static class State {

    public String filePath = EMPTY_FILE_PATH;

    public String langType = EMPTY_LANGUAGE_TYPE;

    /**
     * core path stores the cache file, and this path will be init only once in a certain project
     */
    public String coreFilePath = EMPTY_FILE_PATH;

    public String locale = I18nTypeEnum.ZH.getValue();

    public boolean isEmptyFilePath() {
      return filePath.equals(EMPTY_FILE_PATH);
    }
    public boolean isEmptyLangType() {
      return langType.equals(EMPTY_LANGUAGE_TYPE);
    }
    public boolean isEmptyCoreFilePath() {
      return coreFilePath.equals(EMPTY_FILE_PATH);
    }

    /*--------debug 相关配置----------*/
    // debug指令读取来源
    public String readTypeName;
    // debug信息输出到什么地方
    public String outputTypeName;

    /*---------辅助配置---------*/
    public String rePositionSetting;
    public transient String secretKey = "";
    public boolean encryptOrNot = false;

    @Override
    public String toString() {
      return "State{" +
              "filePath='" + filePath + '\'' +
              ", langType='" + langType + '\'' +
              ", coreFilePath='" + coreFilePath + '\'' +
              ", readTypeName='" + readTypeName + '\'' +
              ", outputTypeName='" + outputTypeName + '\'' +
              ", rePositionSetting='" + rePositionSetting + '\'' +
              '}';
    }
  }

  /**
   * get the absolute path of the cache file path
   * <p>
   * this method will keep the cache file exists one no matter how the filePath is change
   *
   * @return string
   */
  public String getCoreFilePath() {
    if (myState.filePath.equals(EMPTY_FILE_PATH)) {
      return EMPTY_FILE_PATH;
    }
    if (myState.coreFilePath.equals(EMPTY_FILE_PATH)) {
      return new FileUtils.PathBuilder(myState.filePath).append("cache").build();
    }
    return new FileUtils.PathBuilder(myState.coreFilePath).append("cache").build();
  }

  private State myState = new State();

  public AppSettings() {
    LCEventBus.getInstance().register(this);
  }

  @Subscribe
  public void clearCacheEventListener(ClearCacheEvent event) {
    myState.coreFilePath = EMPTY_FILE_PATH;
    myState.filePath = EMPTY_FILE_PATH;
    myState.langType = EMPTY_LANGUAGE_TYPE;
    myState.encryptOrNot = false;
    myState.secretKey = "";
  }

  public static AppSettings getInstance() {
    return ApplicationManager.getApplication()
            .getService(AppSettings.class);
  }

  @Override
  public String toString() {
    // 将state变为string
    return myState.toString();
  }

  public String getFilePath() {
    return myState.filePath;
  }

  public String getLangType() {
    return myState.langType;
  }

  public boolean isUIReader() {
    return ReadType.UI_IN.equals(ReadType.getByName(AppSettings.getInstance().getReadTypeName()));
  }

  public boolean isUIOutput() {
    return OutputType.UI_OUT.equals(OutputType.getByName(AppSettings.getInstance().getOutputTypeName()));
  }

  public boolean isCommandReader() {
    return ReadType.COMMAND_IN.equals(ReadType.getByName(AppSettings.getInstance().getReadTypeName()));
  }
  public boolean isConsoleOutput() {
    return OutputType.CONSOLE_OUT.equals(OutputType.getByName(AppSettings.getInstance().getOutputTypeName()));
  }
  /**
   * 获取debug读取数据来源
   * 在v3.6.8版本中, 引入了en/zh系统, 因此需要convert
   * @return 返回readType. 如果中英文转换出现异常, 则返回空串
   */
  public String getReadTypeName() {
    if (I18nTypeEnum.getI18N(myState.locale) == I18nTypeEnum.EN) {
      try {
        return LanguageConvertor.toEn(myState.readTypeName);
      } catch (NoLanguageError e) {
        LogUtils.warn(DebugUtils.getStackTraceAsString(e));
        return "";
      }
    } else {
      try {
        return LanguageConvertor.toZh(myState.readTypeName);
      } catch (NoLanguageError e) {
        LogUtils.warn(DebugUtils.getStackTraceAsString(e));
        return "";
      }
    }
  }

  /**
   * 获取debug输出到什么地方
   * 在v3.6.8版本中, 引入了en/zh系统, 因此需要convert
   * @return 返回outputType. 如果中英文转换出现异常, 则返回空串
   */
  public String getOutputTypeName() {
    if (I18nTypeEnum.getI18N(myState.locale) == I18nTypeEnum.EN) {
      try {
        return LanguageConvertor.toEn(myState.outputTypeName);
      } catch (NoLanguageError e) {
        LogUtils.warn(DebugUtils.getStackTraceAsString(e));
        return "";
      }
    } else {
      try {
        return LanguageConvertor.toZh(myState.outputTypeName);
      } catch (NoLanguageError e) {
        LogUtils.warn(DebugUtils.getStackTraceAsString(e));
        return "";
      }
    }
  }

  /**
   * get file suffix, for example, java file suffix is .java, python file suffix is .py
   * @return string
   */
  public String getFileTypeSuffix() {
    for (LangType langType : LangType.values()) {
      if (myState.langType.equals(langType.getLangType())) {
        return langType.getSuffix();
      }
    }
    return null;
  }

  public String getLocale() {
    return myState.locale;
  }

  public I18nTypeEnum getI18nType() {
    return I18nTypeEnum.getI18N(AppSettings.getInstance().getLocale());
  }

  public boolean isZh() {
    return I18nTypeEnum.ZH.getValue().equals(AppSettings.getInstance().getLocale());
  }

  public boolean isEn() {
    return I18nTypeEnum.EN.getValue().equals(AppSettings.getInstance().getLocale());
  }

  @Override
  public State getState() {
    return myState;
  }

  @Override
  public void loadState(@NotNull State state) {
    myState = state;
  }

  public boolean initOrNot() {
    return !myState.isEmptyFilePath() && !myState.isEmptyLangType();
  }

  public String getReposition() {
    String rePositionSetting = myState.rePositionSetting;
    if (StringUtils.isBlank(rePositionSetting)) {
      rePositionSetting = REPOSITION_DEFAULT;
    }
    if (isZh()) {
      try {
        return LanguageConvertor.toZh(rePositionSetting);
      } catch (NoLanguageError e) {
        LogUtils.warn(DebugUtils.getStackTraceAsString(e));
        return LanguageConvertor.REPOSITION_DEFAULT_ZH;
      }
    } else {
      try {
        return LanguageConvertor.toEn(rePositionSetting);
      } catch (NoLanguageError e) {
        return LanguageConvertor.REPOSITION_DEFAULT_EN;
      }
    }
  }

  public String getSecretKey() {
    return myState.secretKey;
  }

  public boolean getEncryptOrNot() {
    return myState.encryptOrNot;
  }
}

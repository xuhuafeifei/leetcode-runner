package com.xhf.leetcode.plugin.setting;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.xhf.leetcode.plugin.bus.ClearCacheEvent;
import com.xhf.leetcode.plugin.debug.output.OutputType;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.utils.LangType;
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

  static class State {

    public String filePath = EMPTY_FILE_PATH;

    public String langType = EMPTY_LANGUAGE_TYPE;

    /**
     * core path stores the cache file, and this path will be init only once in a certain project
     */
    public String coreFilePath = EMPTY_FILE_PATH;

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

    @Override
    public String toString() {
      return "State{" +
              "filePath='" + filePath + '\'' +
              ", langType='" + langType + '\'' +
              ", coreFilePath='" + coreFilePath + '\'' +
              ", readTypeName='" + readTypeName + '\'' +
              ", outputTypeName='" + outputTypeName + '\'' +
              '}';
    }
  }

  /**
   * get the absolute path of the cache file path
   * <p>
   * this method will keep the cache file exists one no matter how the filePath is change
   *
   * @return
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
   * @return
   */
  public String getReadTypeName() {
    return myState.readTypeName;
  }

  /**
   * 获取debug输出到什么地方
   * @return
   */
  public String getOutputTypeName() {
    return myState.outputTypeName;
  }

  /**
   * get file suffix, for example, java file suffix is .java, python file suffix is .py
   * @return
   */
  public String getFileTypeSuffix() {
    for (LangType langType : LangType.values()) {
      if (myState.langType.equals(langType.getLangType())) {
        return langType.getSuffix();
      }
    }
    return null;
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

  /**
   * 允许用户重新修改coreFilePath的路径
   * @param event event
   */
  public void clearCacheEventListener(ClearCacheEvent event) {
    myState.coreFilePath = "";
  }
}

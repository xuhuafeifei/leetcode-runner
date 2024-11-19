package com.xhf.leetcode.plugin.setting;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
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

  static class State {

    public String filePath = "E:\\java_code\\leetcode-runner\\src\\main\\resources\\";

    public String langType = "java";
  }

  private State myState = new State();

  public static AppSettings getInstance() {
    return ApplicationManager.getApplication()
            .getService(AppSettings.class);
  }

  public String getFilePath() {
    return myState.filePath;
  }

  public String getLangType() {
    return myState.langType;
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

}

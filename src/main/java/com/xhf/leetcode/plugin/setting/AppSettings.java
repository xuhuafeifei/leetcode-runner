// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.xhf.leetcode.plugin.setting;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.xhf.leetcode.plugin.utils.LangType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/*
 * Supports storing the application settings in a persistent way.
 * The {@link com.intellij.openapi.components.State State} and {@link Storage}
 * annotations define the name of the data and the filename where these persistent
 * application settings are stored.
 */

@State(
        name = "AppSettings",
        storages = @Storage("SdkSettingsPlugin.xml")
)
public final class AppSettings
        implements PersistentStateComponent<AppSettings.State> {

  static class State {
    @NonNls
    public String userId = "John Smith";

    public String filePath = "E:\\java_code\\leetcode-runner\\src\\main\\resources\\";

    public boolean ideaStatus = false;
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

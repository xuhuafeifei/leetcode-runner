// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xhf.leetcode.plugin.setting;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/**
 * Provides controller functionality for application settings.
 */
final class AppSettingsConfigurable implements Configurable {

  private AppSettingsComponent mySettingsComponent;

  // A default constructor with no arguments is required because
  // this implementation is registered as an applicationConfigurable

  @Nls(capitalization = Nls.Capitalization.Title)
  @Override
  public String getDisplayName() {
    return "SDK: Application Settings Example";
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return mySettingsComponent.getPreferredFocusedComponent();
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    mySettingsComponent = new AppSettingsComponent();
    return mySettingsComponent.getPanel();
  }

  @Override
  public boolean isModified() {
    AppSettings.State state =
        Objects.requireNonNull(AppSettings.getInstance().getState());
    return !mySettingsComponent.getUserNameText().equals(state.userId) ||
        mySettingsComponent.getIdeaUserStatus() != state.ideaStatus ||
            !mySettingsComponent.getLangType().equals(state.langType) ||
            !mySettingsComponent.getFilePath().equals(state.filePath)
            ;
  }

  /**
   * 持久化数据
   */
  @Override
  public void apply() {
    AppSettings.State state =
        Objects.requireNonNull(AppSettings.getInstance().getState());
    state.userId = mySettingsComponent.getUserNameText();
    state.ideaStatus = mySettingsComponent.getIdeaUserStatus();
    state.langType = mySettingsComponent.getLangType();
    state.filePath = mySettingsComponent.getFilePath();
  }

  /**
   * 持久化数据赋值给settings
   */
  @Override
  public void reset() {
    AppSettings.State state =
        Objects.requireNonNull(AppSettings.getInstance().getState());
    mySettingsComponent.setUserNameText(state.userId);
    mySettingsComponent.setIdeaUserStatus(state.ideaStatus);
    mySettingsComponent.setLangType(state.langType);
    mySettingsComponent.setFilePath(state.filePath);
  }

  @Override
  public void disposeUIResources() {
    mySettingsComponent = null;
  }

}

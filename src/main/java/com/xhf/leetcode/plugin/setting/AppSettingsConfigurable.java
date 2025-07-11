// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xhf.leetcode.plugin.setting;

import com.intellij.openapi.options.Configurable;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.bus.SecretKeyUpdateEvent;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import java.util.Objects;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
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
        return !mySettingsComponent.getLangType().equals(state.langType) ||
            !mySettingsComponent.getFilePath().equals(state.filePath) ||
            !mySettingsComponent.getReadTypeName().equals(state.readTypeName) ||
            !mySettingsComponent.getOutputTypeName().equals(state.outputTypeName) ||
            !mySettingsComponent.getReposition().equals(state.rePositionSetting) ||
            !mySettingsComponent.getLocale().equals(state.locale) ||
            !mySettingsComponent.getSecretKey().equals(state.secretKey) ||
            !mySettingsComponent.getEncryptOrNot() == state.encryptOrNot ||
            !mySettingsComponent.getEnableFloatingToolbar() == state.enableFloatingToolbar
            ;
    }

    /**
     * 持久化数据
     */
    @Override
    public void apply() {
        AppSettings.State state =
            Objects.requireNonNull(AppSettings.getInstance().getState());
        state.langType = mySettingsComponent.getLangType();
        // check
        state.filePath = mySettingsComponent.getFilePath();
        // make sure that the core file path only init once and the path is valid
        if (state.isEmptyCoreFilePath() && FileUtils.isPath(mySettingsComponent.getFilePath())) {
            state.coreFilePath = mySettingsComponent.getFilePath();
        }
        // debug setting
        state.readTypeName = mySettingsComponent.getReadTypeName();
        state.outputTypeName = mySettingsComponent.getOutputTypeName();

        state.rePositionSetting = mySettingsComponent.getReposition();
        state.locale = mySettingsComponent.getLocale();

        boolean secretKeyUpdateSend = !Objects.equals(mySettingsComponent.getSecretKey(), state.secretKey);
        state.secretKey = mySettingsComponent.getSecretKey();
        state.encryptOrNot = mySettingsComponent.getEncryptOrNot();

        // todo: 发送密钥修改的事件, 通知别的服务update之间使用old secret key存储的数据
        if (secretKeyUpdateSend) {
            LCEventBus.getInstance().post(new SecretKeyUpdateEvent(state.secretKey));
        }

        state.enableFloatingToolbar = mySettingsComponent.getEnableFloatingToolbar();
    }

    /**
     * 持久化数据赋值给settings
     */
    @Override
    public void reset() {
        AppSettings.State state =
            Objects.requireNonNull(AppSettings.getInstance().getState());
        mySettingsComponent.setLangType(state.langType);
        mySettingsComponent.setFilePath(state.filePath);
        state.coreFilePath = mySettingsComponent.getFilePath();
        // debug setting
        mySettingsComponent.setReadTypeName(state.readTypeName);
        mySettingsComponent.setOutputTypeName(state.outputTypeName);

        mySettingsComponent.setReposition(state.rePositionSetting);
        mySettingsComponent.setLocale(state.locale);

        mySettingsComponent.setSecretKey(state.secretKey);
        mySettingsComponent.setEncryptOrNot(state.encryptOrNot);

        mySettingsComponent.setEnableFloatingToolbar(state.enableFloatingToolbar);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}

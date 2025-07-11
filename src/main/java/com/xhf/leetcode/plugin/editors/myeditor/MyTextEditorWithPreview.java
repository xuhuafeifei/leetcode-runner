package com.xhf.leetcode.plugin.editors.myeditor;

import static com.intellij.openapi.actionSystem.ActionPlaces.TEXT_EDITOR_WITH_PREVIEW;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorComponentImpl;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.TextEditorWithPreview;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.pom.Navigatable;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLayeredPane;
import com.intellij.util.Alarm;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.editors.text.CustomTextEditor;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.LogUtils;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.function.Supplier;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * feigebuge改了, 因为UI需要, 需要调整{@link TextEditorWithPreview}的源码
 *
 * Two panel editor with three states: Editor, Preview and Editor with Preview.
 * Based on SplitFileEditor by Valentin Fondaratov
 *
 * @author Konstantin Bulenkov feigebuge
 */
public class MyTextEditorWithPreview extends UserDataHolderBase implements TextEditor {

    public static final Key<Layout> DEFAULT_LAYOUT_FOR_FILE = Key.create("MyTextEditorWithPreview.DefaultLayout");
    protected final TextEditor myEditor;
    protected final FileEditor myPreview;
    @NotNull
    private final MyListenersMultimap myListenersGenerator = new MyListenersMultimap();
    private final Layout myDefaultLayout;
    private final @Nls String myName;
    private Layout myLayout;
    private boolean myIsVerticalSplit;
    private JComponent myComponent;
    private final JBSplitter mySplitter;
    private MySplitEditorToolbar myToolbarWrapper;

    public MyTextEditorWithPreview(@NotNull TextEditor editor,
        @NotNull FileEditor preview,
        @NotNull @Nls String editorName,
        @NotNull Layout defaultLayout,
        boolean isVerticalSplit) {
        myEditor = editor;
        if (editor instanceof CustomTextEditor) {
            CustomTextEditor cte = (CustomTextEditor) editor;
            cte.setToolbar(createViewActionGroup());
        }
        myPreview = preview;
        myName = editorName;
        myDefaultLayout = ObjectUtils.notNull(getLayoutForFile(myEditor.getFile()), defaultLayout);
        myIsVerticalSplit = isVerticalSplit;
        mySplitter = new JBSplitter(myIsVerticalSplit, 0.5f, 0.15f, 0.85f);
        mySplitter.setSplitterProportionKey(getSplitterProportionKey());
        mySplitter.setFirstComponent(myPreview.getComponent());
        mySplitter.setSecondComponent(myEditor.getComponent());
        mySplitter.setDividerWidth(3);
        mySplitter.getDivider().setBackground(JBColor.border());
    }

    public MyTextEditorWithPreview(@NotNull TextEditor editor,
        @NotNull FileEditor preview,
        @NotNull @Nls String editorName,
        @NotNull Layout defaultLayout) {
        this(editor, preview, editorName, defaultLayout, false);
    }

    public MyTextEditorWithPreview(@NotNull TextEditor editor, @NotNull FileEditor preview,
        @NotNull @Nls String editorName) {
        this(editor, preview, editorName, Layout.SHOW_EDITOR_AND_PREVIEW);
    }

    public MyTextEditorWithPreview(@NotNull TextEditor editor, @NotNull FileEditor preview) {
        this(editor, preview, "MyTextEditorWithPreview");
    }

    @Nullable
    private static Layout getLayoutForFile(@Nullable VirtualFile file) {
        if (file != null) {
            return file.getUserData(DEFAULT_LAYOUT_FOR_FILE);
        }
        return null;
    }

    public static void openPreviewForFile(@NotNull Project project, @NotNull VirtualFile file) {
        file.putUserData(DEFAULT_LAYOUT_FOR_FILE, Layout.SHOW_PREVIEW);
        FileEditorManager.getInstance(project).openFile(file, true);
    }

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return myEditor.getBackgroundHighlighter();
    }

    @Nullable
    @Override
    public FileEditorLocation getCurrentLocation() {
        return myEditor.getCurrentLocation();
    }

    @Nullable
    @Override
    public StructureViewBuilder getStructureViewBuilder() {
        return myEditor.getStructureViewBuilder();
    }

    @Override
    public void dispose() {
        Disposer.dispose(myEditor);
        Disposer.dispose(myPreview);
    }

    @Override
    public void selectNotify() {
        myEditor.selectNotify();
        myPreview.selectNotify();
    }

    @Override
    public void deselectNotify() {
        myEditor.deselectNotify();
        myPreview.deselectNotify();
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        if (myComponent != null) {
            return myComponent;
        }

        myToolbarWrapper = createMarkdownToolbarWrapper(mySplitter);

        if (myLayout == null) {
            String lastUsed = PropertiesComponent.getInstance().getValue(getLayoutPropertyName());
            myLayout = Layout.fromId(lastUsed, myDefaultLayout);
        }
        adjustEditorsVisibility();

        BorderLayoutPanel panel = JBUI.Panels.simplePanel(mySplitter);
        // 使用普通的 JPanel 来包装组件
        if (!isShowFloatingToolbar()) {
            myComponent = panel;
            return myComponent;
        }
        // 悬浮窗
        myToolbarWrapper.setVisible(false);
        MyEditorLayeredComponentWrapper layeredPane = new MyEditorLayeredComponentWrapper(panel);
        myComponent = layeredPane;
        MyLayoutActionsFloatingToolbar toolbar = new MyLayoutActionsFloatingToolbar(myComponent,
            new DefaultActionGroup(myToolbarWrapper.getRightToolbar().getActions()));
        Disposer.register(this, toolbar);
        layeredPane.add(panel, JLayeredPane.DEFAULT_LAYER);
        myComponent.add(toolbar, JLayeredPane.POPUP_LAYER);
        registerToolbarListeners(panel, toolbar);
        return myComponent;
    }

    private void registerToolbarListeners(JComponent actualComponent, MyLayoutActionsFloatingToolbar toolbar) {
        UIUtil.addAwtListener(new MyMouseListener(toolbar), AWTEvent.MOUSE_MOTION_EVENT_MASK, toolbar);
        final var actualEditor = UIUtil.findComponentOfType(actualComponent, EditorComponentImpl.class);
        if (actualEditor != null) {
            final var editorKeyListener = new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent event) {
                    toolbar.getVisibilityController().scheduleHide();
                }
            };
            actualEditor.getEditor().getContentComponent().addKeyListener(editorKeyListener);
            Disposer.register(toolbar, () -> {
                actualEditor.getEditor().getContentComponent().removeKeyListener(editorKeyListener);
            });
        }
    }

    protected boolean isShowFloatingToolbar() {
        boolean flag;
        try {
            flag = AppSettings.getInstance().getEnableFloatingToolbar();
        } catch (Exception e) {
            LogUtils.warn(DebugUtils.getStackTraceAsString(e));
            flag = false;
        }

        if (!flag) {
            return false;
        }
        try {
            return Registry.is("ide.text.editor.with.preview.show.floating.toolbar");
        } catch (MissingResourceException e) {
            LogUtils.warn("ide.text.editor.with.preview.show.floating.toolbar registry key not found");
            return false;
        }
    }

    public boolean isVerticalSplit() {
        return myIsVerticalSplit;
    }

    public void setVerticalSplit(boolean verticalSplit) {
        myIsVerticalSplit = verticalSplit;
        mySplitter.setOrientation(verticalSplit);
    }

    @NotNull
    private MySplitEditorToolbar createMarkdownToolbarWrapper(@NotNull JComponent targetComponentForActions) {
        final ActionToolbar leftToolbar = createToolbar();
        if (leftToolbar != null) {
            leftToolbar.setTargetComponent(targetComponentForActions);
            leftToolbar.setReservePlaceAutoPopupIcon(false);
        }

        final ActionToolbar rightToolbar = createRightToolbar();
        rightToolbar.setTargetComponent(targetComponentForActions);
        rightToolbar.setReservePlaceAutoPopupIcon(false);

        return new MySplitEditorToolbar(leftToolbar, rightToolbar);
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
        if (state instanceof MyFileEditorState) {
            final MyFileEditorState compositeState = (MyFileEditorState) state;
            if (compositeState.getFirstState() != null) {
                myEditor.setState(compositeState.getFirstState());
            }
            if (compositeState.getSecondState() != null) {
                myPreview.setState(compositeState.getSecondState());
            }
            if (compositeState.getSplitLayout() != null) {
                myLayout = compositeState.getSplitLayout();
                invalidateLayout();
            }
        }
    }

    protected void onLayoutChange(Layout oldValue, Layout newValue) {
    }

    private void adjustEditorsVisibility() {
        if (myPreview == null || myEditor == null || myLayout == null) {
            LogUtils.warn("myPreview or myEditor or myLayout is null");
            return;
        }
        // 单独处理Preview模式, 不能全部铺满, 否则悬浮框会被遮挡
        if (myLayout == Layout.SHOW_PREVIEW) {
            myPreview.getComponent().setVisible(true);
            myEditor.getComponent().setVisible(true);
            // 调整80%的宽度
            mySplitter.setProportion(0.8f);
        } else if (myLayout == Layout.SHOW_EDITOR) {
            myPreview.getComponent().setVisible(false);
            myEditor.getComponent().setVisible(true);
        } else if (myLayout == Layout.SHOW_EDITOR_AND_PREVIEW) {
            myPreview.getComponent().setVisible(true);
            myEditor.getComponent().setVisible(true);
            mySplitter.setProportion(0.4f);
        }
        //    myEditor.getComponent().setVisible(myLayout == Layout.SHOW_EDITOR || myLayout == Layout.SHOW_EDITOR_AND_PREVIEW);
        //    myPreview.getComponent().setVisible(myLayout == Layout.SHOW_PREVIEW || myLayout == Layout.SHOW_EDITOR_AND_PREVIEW);
    }

    private void invalidateLayout() {
        adjustEditorsVisibility();
        if (myToolbarWrapper != null) {
            myToolbarWrapper.refresh();
        }
        if (myComponent != null) {
            myComponent.repaint();
        }

        final JComponent focusComponent = getPreferredFocusedComponent();
        Component focusOwner = IdeFocusManager.findInstance().getFocusOwner();
        if (focusComponent != null && focusOwner != null && SwingUtilities.isDescendingFrom(focusOwner,
            getComponent())) {
            IdeFocusManager.findInstanceByComponent(focusComponent).requestFocus(focusComponent, true);
        }
    }

    @NotNull
    protected String getSplitterProportionKey() {
        return "MyTextEditorWithPreview.SplitterProportionKey";
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        switch (myLayout) {
            case SHOW_EDITOR_AND_PREVIEW:
            case SHOW_EDITOR:
                return myEditor.getPreferredFocusedComponent();
            case SHOW_PREVIEW:
                return myPreview.getPreferredFocusedComponent();
            default:
                throw new IllegalStateException(myLayout.myId);
        }
    }

    @NotNull
    @Override
    public String getName() {
        return myName;
    }

    @NotNull
    @Override
    public FileEditorState getState(@NotNull FileEditorStateLevel level) {
        return new MyFileEditorState(myLayout, myEditor.getState(level), myPreview.getState(level));
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        myEditor.addPropertyChangeListener(listener);
        myPreview.addPropertyChangeListener(listener);

        final DoublingEventListenerDelegate delegate = myListenersGenerator.addListenerAndGetDelegate(listener);
        myEditor.addPropertyChangeListener(delegate);
        myPreview.addPropertyChangeListener(delegate);
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        myEditor.removePropertyChangeListener(listener);
        myPreview.removePropertyChangeListener(listener);

        final DoublingEventListenerDelegate delegate = myListenersGenerator.removeListenerAndGetDelegate(listener);
        if (delegate != null) {
            myEditor.removePropertyChangeListener(delegate);
            myPreview.removePropertyChangeListener(delegate);
        }
    }

    @NotNull
    public TextEditor getTextEditor() {
        return myEditor;
    }

    @NotNull
    public FileEditor getPreviewEditor() {
        return myPreview;
    }

    public Layout getLayout() {
        return myLayout;
    }

    protected void setLayout(@NotNull Layout layout) {
        Layout oldLayout = myLayout;
        myLayout = layout;
        PropertiesComponent.getInstance().setValue(getLayoutPropertyName(), myLayout.myId, myDefaultLayout.myId);
        adjustEditorsVisibility();
        onLayoutChange(oldLayout, myLayout);
    }

    @Override
    public boolean isModified() {
        return myEditor.isModified() || myPreview.isModified();
    }

    @Override
    public boolean isValid() {
        return myEditor.isValid() && myPreview.isValid();
    }

    @Nullable
    protected ActionToolbar createToolbar() {
        ActionGroup actionGroup = createLeftToolbarActionGroup();
        if (actionGroup != null) {
            return ActionManager.getInstance().createActionToolbar(TEXT_EDITOR_WITH_PREVIEW, actionGroup, true);
        } else {
            return null;
        }
    }

    @Nullable
    protected ActionGroup createLeftToolbarActionGroup() {
        return null;
    }

    @NotNull
    private ActionToolbar createRightToolbar() {
        final ActionGroup viewActions = createViewActionGroup();
        final ActionGroup group = createRightToolbarActionGroup();
        final ActionGroup rightToolbarActions = group == null
            ? viewActions
            :
                AppSettings.getInstance().getEnableFloatingToolbar() ?
                    // 如果开启悬浮窗, 则右侧工具栏显示在悬浮窗上, 否则显示在编辑器上
                    new DefaultActionGroup(group, Separator.create(), viewActions) :
                    // 否则, 只显示viewActions
                    new DefaultActionGroup(viewActions);

        return ActionManager.getInstance().createActionToolbar(TEXT_EDITOR_WITH_PREVIEW, rightToolbarActions, true);
    }

    @NotNull
    protected ActionGroup createViewActionGroup() {
        return new DefaultActionGroup(
            getShowEditorAction(),
            getShowEditorAndPreviewAction(),
            getShowPreviewAction()
        );
    }

    @Nullable
    protected ActionGroup createRightToolbarActionGroup() {
        return null;
    }

    @NotNull
    protected ToggleAction getShowEditorAction() {
        return new ChangeViewModeAction(Layout.SHOW_EDITOR);
    }

    @NotNull
    protected ToggleAction getShowPreviewAction() {
        return new ChangeViewModeAction(Layout.SHOW_PREVIEW);
    }

    @NotNull
    protected ToggleAction getShowEditorAndPreviewAction() {
        return new ChangeViewModeAction(Layout.SHOW_EDITOR_AND_PREVIEW);
    }

    @NotNull
    private String getLayoutPropertyName() {
        return myName + "Layout";
    }

    @Override
    public @Nullable VirtualFile getFile() {
        return getTextEditor().getFile();
    }

    @Override
    public @NotNull Editor getEditor() {
        return getTextEditor().getEditor();
    }

    @Override
    public boolean canNavigateTo(@NotNull Navigatable navigatable) {
        return getTextEditor().canNavigateTo(navigatable);
    }

    @Override
    public void navigateTo(@NotNull Navigatable navigatable) {
        getTextEditor().navigateTo(navigatable);
    }

    protected void handleLayoutChange(boolean isVerticalSplit) {
      if (myIsVerticalSplit == isVerticalSplit) {
        return;
      }
        myIsVerticalSplit = isVerticalSplit;

        myToolbarWrapper.refresh();
        mySplitter.setOrientation(myIsVerticalSplit);
        myComponent.repaint();
    }

    public enum Layout {
        SHOW_EDITOR("Editor only", IdeBundle.messagePointer("tab.title.editor.only")),
        SHOW_PREVIEW("Preview only", IdeBundle.messagePointer("tab.title.preview.only")),
        SHOW_EDITOR_AND_PREVIEW("Editor and Preview", IdeBundle.messagePointer("tab.title.editor.and.preview"));

        private final @NotNull Supplier<@Nls String> myName;
        private final String myId;

        Layout(String id, @NotNull Supplier<String> name) {
            myId = id;
            myName = name;
        }

        public static Layout fromId(String id, Layout defaultValue) {
            for (Layout layout : values()) {
                if (layout.myId.equals(id)) {
                    return layout;
                }
            }
            return defaultValue;
        }

        public @Nls String getName() {
            return myName.get();
        }

        public Icon getIcon(@Nullable MyTextEditorWithPreview editor) {
          if (this == SHOW_EDITOR) {
            return AllIcons.General.LayoutEditorOnly;
          }
          if (this == SHOW_PREVIEW) {
            return AllIcons.General.LayoutPreviewOnly;
          }
            boolean isVerticalSplit = editor != null && editor.myIsVerticalSplit;
            return isVerticalSplit ? IconLoader.getIcon("expui/general/editorPreviewVertical.svg", AllIcons.class)
                : IconLoader.getIcon("expui/general/editorPreview.svg", AllIcons.class);
        }
    }

    private static class MyEditorLayeredComponentWrapper extends JBLayeredPane {

        static final int toolbarTopPadding = 25;
        static final int toolbarRightPadding = 20;
        private final JComponent editorComponent;

        private MyEditorLayeredComponentWrapper(JComponent component) {
            editorComponent = component;
        }

        @Override
        public void doLayout() {
            final var components = getComponents();
            final var bounds = getBounds();
            for (Component component : components) {
                if (component == editorComponent) {
                    component.setBounds(0, 0, bounds.width, bounds.height);
                } else {
                    final var preferredComponentSize = component.getPreferredSize();
                    var x = 0;
                    var y = 0;
                    if (component instanceof MyLayoutActionsFloatingToolbar) {
                        x = bounds.width - preferredComponentSize.width - toolbarRightPadding;
                        y = toolbarTopPadding;
                    }
                    component.setBounds(x, y, preferredComponentSize.width, preferredComponentSize.height);
                }
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return editorComponent.getPreferredSize();
        }
    }

    public static class MyFileEditorState implements FileEditorState {

        private final Layout mySplitLayout;
        private final FileEditorState myFirstState;
        private final FileEditorState mySecondState;

        public MyFileEditorState(Layout layout, FileEditorState firstState, FileEditorState secondState) {
            mySplitLayout = layout;
            myFirstState = firstState;
            mySecondState = secondState;
        }

        @Nullable
        public Layout getSplitLayout() {
            return mySplitLayout;
        }

        @Nullable
        public FileEditorState getFirstState() {
            return myFirstState;
        }

        @Nullable
        public FileEditorState getSecondState() {
            return mySecondState;
        }

        @Override
        public boolean canBeMergedWith(@NotNull FileEditorState otherState, @NotNull FileEditorStateLevel level) {
            return otherState instanceof MyFileEditorState
                && (myFirstState == null || myFirstState.canBeMergedWith(((MyFileEditorState) otherState).myFirstState,
                level))
                && (mySecondState == null || mySecondState.canBeMergedWith(
                ((MyFileEditorState) otherState).mySecondState, level));
        }
    }

    private class MyMouseListener implements AWTEventListener {

        private final MyLayoutActionsFloatingToolbar toolbar;
        private final Alarm alarm;

        MyMouseListener(MyLayoutActionsFloatingToolbar toolbar) {
            this.toolbar = toolbar;
            alarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, toolbar);
        }

        @Override
        public void eventDispatched(AWTEvent event) {
            var isMouseOutsideToolbar = toolbar.getMousePosition() == null;
            if (myComponent.getMousePosition() != null) {
                alarm.cancelAllRequests();
                toolbar.getVisibilityController().scheduleShow();
                if (isMouseOutsideToolbar) {
                    alarm.addRequest(() -> {
                        toolbar.getVisibilityController().scheduleHide();
                    }, 1400);
                }
            } else if (isMouseOutsideToolbar) {
                toolbar.getVisibilityController().scheduleHide();
            }
        }
    }

    private final class DoublingEventListenerDelegate implements PropertyChangeListener {

        @NotNull
        private final PropertyChangeListener myDelegate;

        private DoublingEventListenerDelegate(@NotNull PropertyChangeListener delegate) {
            myDelegate = delegate;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            myDelegate.propertyChange(
                new PropertyChangeEvent(MyTextEditorWithPreview.this, evt.getPropertyName(), evt.getOldValue(),
                    evt.getNewValue()));
        }
    }

    private class MyListenersMultimap {

        private final Map<PropertyChangeListener, Pair<Integer, DoublingEventListenerDelegate>> myMap = new HashMap<>();

        @NotNull
        public DoublingEventListenerDelegate addListenerAndGetDelegate(@NotNull PropertyChangeListener listener) {
            if (!myMap.containsKey(listener)) {
                myMap.put(listener, Pair.create(1, new DoublingEventListenerDelegate(listener)));
            } else {
                final Pair<Integer, DoublingEventListenerDelegate> oldPair = myMap.get(listener);
                myMap.put(listener, Pair.create(oldPair.getFirst() + 1, oldPair.getSecond()));
            }

            return myMap.get(listener).getSecond();
        }

        @Nullable
        public DoublingEventListenerDelegate removeListenerAndGetDelegate(@NotNull PropertyChangeListener listener) {
            final Pair<Integer, DoublingEventListenerDelegate> oldPair = myMap.get(listener);
            if (oldPair == null) {
                return null;
            }

            if (oldPair.getFirst() == 1) {
                myMap.remove(listener);
            } else {
                myMap.put(listener, Pair.create(oldPair.getFirst() - 1, oldPair.getSecond()));
            }
            return oldPair.getSecond();
        }
    }

    private class ChangeViewModeAction extends ToggleAction implements DumbAware {

        private final Layout myActionLayout;

        ChangeViewModeAction(Layout layout) {
            super(layout.getName(), layout.getName(), layout.getIcon(MyTextEditorWithPreview.this));
            myActionLayout = layout;
        }

        @Override
        public boolean isSelected(@NotNull AnActionEvent e) {
            return myLayout == myActionLayout;
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.BGT;
        }

        @Override
        public void setSelected(@NotNull AnActionEvent e, boolean state) {
            if (state) {
                setLayout(myActionLayout);
            } else {
                if (myActionLayout == Layout.SHOW_EDITOR_AND_PREVIEW) {
                    mySplitter.setOrientation(!myIsVerticalSplit);
                    myIsVerticalSplit = !myIsVerticalSplit;
                }
            }
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setIcon(myActionLayout.getIcon(MyTextEditorWithPreview.this));
        }
    }
}
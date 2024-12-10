package com.xhf.leetcode.plugin.comp;

import com.intellij.openapi.ui.ComboBox;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.window.filter.DifficultyFilter;
import com.xhf.leetcode.plugin.window.filter.Filter;
import com.xhf.leetcode.plugin.window.filter.QFilter;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 封装了搜索条件面板的逻辑. 服务于SearchPanel
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class MySearchConditionPanel extends JPanel {
    private final QFilter myFilter;
    private final ComboBox<String> conditionComb;
    private final String defaultText;
    private String previousSelection;
    private final OptionConvert convert;

    /**
     * 创建convert, 该方法下放到子类, 由具体实现类进行创建.
     * 该方法提供转换器, 允许Combox显示的文本和实际过滤的内容不一致
     * @return
     */
    public abstract OptionConvert createConvert();

    /**
     * 创建不同的过滤器, 执行不同过滤行为
     * @return
     */
    public abstract QFilter createFilter();

    public MySearchConditionPanel(Runnable runnable, String defaultText) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.defaultText = defaultText;
        myFilter = createFilter();
        convert = createConvert();

        conditionComb = new ComboBox<>(convert.getOptions());
        conditionComb.setToolTipText(defaultText);

        // 设置默认选择项为空 (类似于显示默认文本)
        conditionComb.setSelectedIndex(-1); // 没有选择项时，显示默认文本

//        // 设置 ComboBox 的渲染器，模拟显示提示文本
//        conditionComb.setRenderer(new DefaultListCellRenderer() {
//            @Override
//            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//                if (index == -1 && value == null) {
//                    value = "Difficulty"; // 默认显示文本
//                }
//                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//            }
//        });

        conditionComb.addActionListener(e -> {
            String selectedItem = (String) conditionComb.getSelectedItem();
            // 清空filter内容(comobox只支持单选, 因此无论做出何种行为, 此前的过滤条件都要清除)
            myFilter.removeAllItems();

            // 判断当前选择的项是否与之前的选择项相同
            if (selectedItem != null && selectedItem.equals(previousSelection)) {
                // 如果选择的是之前的项，清空选择
                conditionComb.setSelectedIndex(-1);
                // 清除记录的选中项
                previousSelection = null;
            } else {
                // 否则，更新选中的项
                previousSelection = selectedItem;
                // 根据ComboBox显示的内容, 为filter添加不同的过滤条件
                myFilter.addItem(convert.doConvert(selectedItem));
            }
            // 执行后置操作, 按照道理, 此处方法应该执行SearchPanel::updateText
            runnable.run();
        });

        // 设置组件的对齐方式为居中
        JLabel label = new JLabel(defaultText);
        label.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        conditionComb.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        this.add(label);
        this.add(conditionComb);
    }

    public QFilter getFilter() {
        return myFilter;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        conditionComb.setEnabled(enabled);
    }

    /**
     * 选项转换器, 服务于ComboBox显示的选项和实际内容之间的转换
     */
    public interface OptionConvert {
        void addPair(String option);

        /**
         * @param option 用于显示的选项
         * @param converted 显示选项对应的实际内容
         */
        void addPair(String option, String converted);
        String[] getOptions();
        String doConvert(String option);
    }

    public class MapOptionConverter implements OptionConvert {
        private Map<String, String> map = new HashMap<>();

        public MapOptionConverter() {
            this(20);
        }

        public MapOptionConverter(int size) {
            map = new HashMap<>(size);
        }

        @Override
        public void addPair(String operation) {
            map.put(operation, operation);
        }

        @Override
        public void addPair(String option, String converted) {
            map.put(option, converted);
        }

        @Override
        public String[] getOptions() {
            return map.keySet().toArray(new String[0]);
        }

        @Override
        public String doConvert(String option) {
            return map.get(option);
        }
    }

    public class ArrayOptionConverter implements OptionConvert {
        private class Pair {
            String option;
            String converted;

            public Pair(String option, String converted) {
                this.option = option;
                this.converted = converted;
            }
        }

        private final Pair[] array;
        private int cursor;
        private int actualSize;

        public ArrayOptionConverter(int size) {
            if (size > 6) {
                LogUtils.warn("option may be large, this may lead to slow");
            }
            array = new Pair[size];
            cursor = 0;
            actualSize = 0;
        }

        /**
         * 默认大小为5, 超过这个数就需要使用有参构造
         */
        public ArrayOptionConverter() {
            this(5);
        }

        @Override
        public void addPair(String option) {
            addPair(option, option);
        }

        @Override
        public void addPair(String option, String converted) {
            if (cursor >= array.length) {
                throw new IllegalArgumentException("Too many options");
            }
            array[cursor] = new Pair(option, converted);
            cursor += 1;
            actualSize += 1;
        }

        @Override
        public String[] getOptions() {
            String[] options = new String[array.length];
            for (int i = 0; i < actualSize; i++) {
                options[i] = array[i].option;
            }
            return options;
        }

        @Override
        public String doConvert(String option) {
            for (int i = 0; i < actualSize; i++) {
                if (array[i].option.equals(option)) {
                    return array[i].converted;
                }
            }
            throw new IllegalArgumentException("Unknown option: " + option);
        }
    }

}

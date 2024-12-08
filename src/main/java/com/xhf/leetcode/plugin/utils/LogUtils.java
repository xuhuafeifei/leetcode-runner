package com.xhf.leetcode.plugin.utils;

import com.intellij.ide.script.IDE;
import com.intellij.openapi.diagnostic.LogLevel;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LogUtils {
    private static final Logger IDEA_LOGGER = Logger.getInstance(LogUtils.class);
    // private static final Log LOG = LogFactory.getLog(LogUtils.class);

    static {
        IDEA_LOGGER.setLevel(LogLevel.INFO);
    }

    public static void info(String message) {
        IDEA_LOGGER.info(message);
//        LOG.info(message);
    }

    public static void debug(String message) {
        IDEA_LOGGER.debug(message);
//        LOG.debug(message);
    }

    public static void error(String message) {
        IDEA_LOGGER.error(message);
//        LOG.error(message);
    }

    public static void error(String message, Throwable e) {
        IDEA_LOGGER.error(message, e);
//        LOG.error(message, e);
    }

    public static void warn(String message) {
        IDEA_LOGGER.warn(message);
//        LOG.warn(message);
    }
}

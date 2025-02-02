package com.xhf.leetcode.plugin.io.file.utils;

import com.xhf.leetcode.plugin.utils.LogUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class FileUtils {

    /**
     * read resource file by relative path
     * @param relativeFilePath
     * @return
     */
    public static URL getResourceFileUrl(String relativeFilePath) {
        // deal with a file path (make all \ into /)
        // because Class.getResource only allow / as separator
        relativeFilePath = unUnifyPath(relativeFilePath);

        Class<?> clazz = FileUtils.class;

        URL resourceUrl = clazz.getResource(relativeFilePath);


        if (resourceUrl == null) {
            throw new IllegalArgumentException("File not found! " + relativeFilePath);
        }

        return resourceUrl;
    }

    public static String readContentFromFile(URL url) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public static String readContentFromFile(String path) {
        File file = new File(path);
        if (! file.exists()) {
            return null;
        }
        return readContentFromFile(file);
    }

    public static String readContentFromFile(File file) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public static Properties readPropertiesFileContent(String path) {
        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream(path)) {
            properties.load(fis);
        } catch (IOException e) {
            LogUtils.warn("no properties file exist in path = " + path);
        }
        return properties;
    }

    public static void writePropertiesFileContent(String path, Properties properties) throws IOException {
        boolean flag = checkAndCreateNewFile(path);
        if (! flag) {
            LogUtils.warn("文件持久化失败!");
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(path)) {
            // write properties to file
            properties.store(fos, "persist content");
        }
    }

    private static boolean checkAndCreateNewFile(String path) throws IOException {
        if (! isPath(path)) {
            LogUtils.warn("path is not valid ! path = " + path);
            return false;
        }
        // check file exists
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        File parentDir = file.getParentFile();
        // create parent dirs
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new RuntimeException("dir create error..");
            }
        }
        // create new file
        return file.createNewFile();
    }

    public static boolean fileExists(String path) {
        return new File(path).exists();
    }

    /**
     * create a file if it does not exist and return the file
     * @param path
     * @return
     * @throws IOException
     */
    public static File createAndGetFile(String path) throws IOException {
        // check file exists
        File file = new File(path);
        if (file.exists()) return file;
        File parentDir = file.getParentFile();
        // create parent dirs
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new RuntimeException("dir create error..");
            }
        }
        // create new file
        file.createNewFile();
        return file;
    }

    /**
     * this method will create a file if it does not exist
     * but will overwrite the content if the file exists
     *
     * @param path
     * @param content
     * @throws IOException
     */
    public static void createAndWriteFile(String path, String content) throws IOException {
        File file = createAndGetFile(path);

        // write content
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * find target file
     * @param directory
     * @param targetFileName
     * @return
     */
    public static List<File> findTargetFiles(String directory, String targetFileName) {
        File dir = new File(directory);
        List<File> targetFiles = new ArrayList<>();

        if (dir.exists() && dir.isDirectory()) {
            File[] entries = dir.listFiles();
            if (entries != null) {
                for (File entry : entries) {
                    if (entry.isDirectory()) {
                        targetFiles.addAll(findTargetFiles(entry.getAbsolutePath(), targetFileName));
                    } else if (entry.getName().equals(targetFileName)) {
                        targetFiles.add(entry);
                    }
                }
            }
        }
        return targetFiles;
    }

    /**
     * unify path separator
     * if path contains / such as E:/demo/a.text, change it into E:\demo\a.text
     * @param path
     * @return
     */
    public static String unifyPath(String path) {
        return path.replaceAll("/", "\\\\");
    }

    public static String unUnifyPath(String path) {
        return path.replaceAll("\\\\", "/");
    }

    public static class BackslashEscape {
        /**
         * 检测字符串中是否存在未转义的反斜杠，并进行转义。
         *
         * @param input 输入的字符串
         * @return 转义后的字符串
         */
        public static String escapeBackslash(String input) {
            // 用于存储结果的 StringBuilder
            StringBuilder result = new StringBuilder();

            // 遍历字符串的每个字符
            for (int i = 0; i < input.length(); i++) {
                char currentChar = input.charAt(i);

                // 如果当前字符是反斜杠
                if (currentChar == '\\') {
                    // 检查下一个字符是否是反斜杠或转义字符
                    if (i + 1 < input.length()) {
                        char nextChar = input.charAt(i + 1);

                        // 如果下一个字符不是反斜杠或转义字符，则转义当前反斜杠
                        if (nextChar != '\\') {
                            result.append('\\'); // 添加一个额外的反斜杠
                        }
                    } else {
                        // 如果反斜杠是最后一个字符，则转义它
                        result.append('\\');
                    }
                }

                // 添加当前字符到结果中
                result.append(currentChar);
            }

            return result.toString();
        }

        /**
         * 判断字符是否是转义字符（如 \n, \t, \r 等）。
         *
         * @param c 需要判断的字符
         * @return 如果是转义字符，返回 true；否则返回 false
         */
        private static boolean isEscapeCharacter(char c) {
            return c == 'n' || c == 't' || c == 'r' || c == 'b' || c == 'f' || c == '\'' || c == '\"' || c == '\\';
        }
    }

    /**
     * judge whether the content is a file path or not
     * @param content
     * @return
     */
    public static boolean isPath(String content) {
        String regex = "^[a-zA-Z]:\\\\[^<>:\"/|?*]+(\\\\[^<>:\"/|?*]+)*$"; // Windows path
        regex += "|^/[^<>:\"/|?*]+(/[^<>:\"/|?*]+)*$"; // Unix/Linux path
        return content.matches(regex);
    }

    public static void deleteFile(String path) {
        File file = new File(path);
        file.deleteOnExit();
    }


    public static void copyFile(File resource, String targetPath) throws IOException {
        // 将resource 复制到solutionPyPath
        try (InputStream inputStream = new FileInputStream(resource);
            OutputStream outputStream = new FileOutputStream(targetPath)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }

    public static void copyFile(URL resource, String targetPath) throws IOException {
        // 将resource 复制到solutionPyPath
        try (InputStream inputStream = resource.openStream();
            OutputStream outputStream = new FileOutputStream(targetPath)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }

    public static boolean copyFile(InputStream inputStream, String targetPath) {
        try {
            // 判断targetPath是否存在, 不存在就创建
            File file = createAndGetFile(targetPath);
            try (OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }
            return true;
        } catch (Exception e) {
            LogUtils.error(e);
            return false;
        }
    }


    /**
     * build a file path and make sure the path is unified
     */
    public static class PathBuilder {
        private final StringBuffer sb;

        public PathBuilder(String path) {
            this.sb = new StringBuffer(unifyPath(path));
        }

        public PathBuilder append(String path) {
            int len = sb.length();
            if (len == 0) {
                sb.append(path);
                return this;
            }
            char c = sb.charAt(len - 1);
            if (c != '\\' && c != '/') {
                sb.append('\\');
            }
            sb.append(path);
            return this;
        }

        public String build() {
            String path = sb.toString();
            return FileUtils.unifyPath(path);
        }

        public String buildUnUnify() {
            String path = sb.toString();
            return FileUtils.unUnifyPath(path);
        }

        /**
         * 转义
         * @return 转义后的字符串
         */
        public String buildWithEscape() {
            String path = sb.toString();
            return BackslashEscape.escapeBackslash(path);
        }
    }
}

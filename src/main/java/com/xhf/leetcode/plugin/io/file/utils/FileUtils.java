package com.xhf.leetcode.plugin.io.file.utils;

import java.io.*;
import java.net.URL;
import java.util.Properties;

public class FileUtils {

    public static URL getResourceFileUrl(String relativeFilePath) {
        // deal with file path (make all \ into /)
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
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
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
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
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
            System.err.println("no properties file exist in path = " + path);
        }
        return properties;
    }

    public static void writePropertiesFileContent(String path, Properties properties) throws IOException {
        checkAndCreateNewFile(path);

        try (FileOutputStream fos = new FileOutputStream(path)) {
            // write properties to file
            properties.store(fos, "persist content");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkAndCreateNewFile(String path) throws IOException {
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
        file.createNewFile();
    }

    /**
     * create file if it does not exist and return the file
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
            fos.write(content.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    /**
     * build path
     */
    public static class PathBuilder {
        private StringBuffer sb;

        public PathBuilder(String path) {
            this.sb = new StringBuffer(unifyPath(path));
        }

        public PathBuilder append(String path) {
            int len = sb.length();
            char c = sb.charAt(len - 1);
            if (c != '\\' && c != '/') {
                sb.append('\\');
            }
            sb.append(path);
            return this;
        }

        public String build() {
            String path = sb.toString();
            String s = FileUtils.unifyPath(path);
            return s;
        }
    }
}

package com.xhf.learning.store.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class FileUtils {

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
        File parentDir = file.getParentFile();
        // create parent dirs
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new RuntimeException("dir create error..");
            }
        }
        // create new file
        file.delete();
        file.createNewFile();
    }

}

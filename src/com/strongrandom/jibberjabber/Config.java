package com.strongrandom.jibberjabber;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: strongrandom
 * Date: 3/4/12
 * Time: 3:09 PM
 * <p/>
 * This is a simple class that reads in configuration from a typical Java-style properties file. You can override
 * the configuration file at run-time by specifying -Dconfig=filename.properties
 */
public class Config {
    private final String DEFAULT_CONFIG_FILE_NAME = "jibberjabber.properties";

    private Map<String, String> values;

    // Private constructor prevents instantiation from other classes
    private Config() {
        Properties myProps = new Properties();
        FileInputStream MyInputStream = null;
        values = new HashMap<String, String>();

        try {
            MyInputStream = new FileInputStream(getConfigFileName());
            myProps.load(MyInputStream);

            String key;
            String value;

            for (Map.Entry<Object, Object> propItem : myProps.entrySet()) {
                key = (String) propItem.getKey();
                value = (String) propItem.getValue();

                values.put(key, value);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (MyInputStream != null) {
                try {
                    MyInputStream.close(); // better in finally block
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        public static final Config instance = new Config();
    }

    public static Config getInstance() {
        return SingletonHolder.instance;
    }

    public String getString(String key) {
        return values.get(key);
    }

    public String getString(String key, String def) {
        if (!values.containsKey(key)) return def;
        return getString(key);
    }

    public int getInteger(String key) {
        return Integer.parseInt(values.get(key));
    }

    public int getInteger(String key, int def) {
        if (!values.containsKey(key)) return def;
        return getInteger(key);
    }

    private String getConfigFileName() {
        String result = System.getProperty("config");
        if (result == null) result = DEFAULT_CONFIG_FILE_NAME;
        return result;
    }
}
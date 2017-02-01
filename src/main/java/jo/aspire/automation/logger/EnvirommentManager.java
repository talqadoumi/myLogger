package jo.aspire.automation.logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FilenameUtils;

public class EnvirommentManager {

    private Properties defaultProps = new Properties();
    private Properties appProps = null;
    public static String platformVersion = "5.0";
    private Hashtable<String, ArrayList<PropertyChangeListener>> listeners = null;
    private static Class<?> initialClass;
    private static Object lock = new Object();
    private static EnvirommentManager instance = null;
    private String PropertiesLocalisaztion = "";

    private EnvirommentManager() {
    }

    /**
     *
     * @param parInitialClass
     *
     * Set initial class to be loaded
     *
     */
    public static void setInitialClass(Class<?> parInitialClass) {

        initialClass = parInitialClass;
        setIntialLog4jClass();

    }

    public static void setIntialLog4jClass() {

        try {
            Constructor<AspireLog4j> constructor = AspireLog4j.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            AspireLog4j secondOb = constructor.newInstance();
            constructor.setAccessible(false);
            AspireLog4j.setLoggerMessageLevel("Reflection Call - - - - - - - - - ", Log4jLevels.DEBUG);
        } catch (Exception ex) {
        }

    }

    /**
     *
     * @return
     *
     * Singlton EnvirommentManager object
     *
     */
    public static EnvirommentManager getInstance() {
        try {
            if (instance == null) {
                synchronized (lock) {
                    if (instance == null) {
                        instance = new EnvirommentManager();
                        instance.loadProperties();
                    }
                }
            }
        } catch (IOException e) {
            AspireLog4j.setLoggerMessageLevel("Error !!!", Log4jLevels.ERROR, e);
        }
        return (instance);

    }

    /**
     * Load main.config file
     *
     * @param path
     *
     * main Config file path
     */
    private void LoadMainConfig(String path) {
        Properties prop = new Properties();
        InputStream MainProperty = null;
        try {

            MainProperty = new FileInputStream(new File(path + File.separator + "MainConfig.properties"));

            // load a properties file
            prop.load(MainProperty);

            // get the property value and print it out
            PropertiesLocalisaztion = prop.getProperty("local");
        } catch (IOException ex) {
            AspireLog4j.setLoggerMessageLevel("Missing file MainConfig.properties " + ex, Log4jLevels.ERROR, ex);
        } finally {
            if (MainProperty != null) {
                try {
                    MainProperty.close();
                } catch (IOException exception) {

                    AspireLog4j.setLoggerMessageLevel("ERROR !", Log4jLevels.ERROR, exception);

                }
            }
        }

    }

    /**
     * Load Properties file file located inside src/test/resources/configs Run
     * with JAR file / Run with IDE
     *
     * @throws IOException
     */
    private void loadProperties() throws IOException {
        List<String> files = new ArrayList<String>();
        String path = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator
                + "resources" + File.separator + "configs";
        LoadMainConfig(path);

        final File jarFile = new File(initialClass.getProtectionDomain().getCodeSource().getLocation().getPath());

        AspireLog4j.setLoggerMessageLevel("Jar path is:", Log4jLevels.INFO);
        if (jarFile.isFile()) {
            // Run with JAR file

            AspireLog4j.setLoggerMessageLevel(" RUN with JAR --- iam jar", Log4jLevels.INFO);
            final JarFile jar = new JarFile(jarFile);
            final Enumeration<JarEntry> entries = jar.entries(); // gives ALL
            // entries
            // in jar

            while (entries.hasMoreElements()) {

                final String name = entries.nextElement().getName();
                if (name.toLowerCase()
                        .endsWith((PropertiesLocalisaztion != "" ? "." : "") + PropertiesLocalisaztion + ".properties")
                        && name.toLowerCase().contains(
                                (PropertiesLocalisaztion != "" ? "." : "") + PropertiesLocalisaztion + ".configs")) { // filter

                    defaultProps.load(this.getClass().getClassLoader().getResourceAsStream(name));

                    appProps = new Properties(defaultProps);
                }
            }
            jar.close();
        } else {
            // Run with IDE
            AspireLog4j.setLoggerMessageLevel("Run with IDE - iam not jar", Log4jLevels.INFO);

            File directory = new File(path);

            // get all the files from a directory
            File[] fList = directory.listFiles();
            for (File file : fList) {
                if (file.isFile() && FilenameUtils.getName(file.getPath()).endsWith(
                        (!PropertiesLocalisaztion.equals("") ? "." : "") + PropertiesLocalisaztion + ".properties")) {
                    files.add(file.getPath());

                    // create and load default properties
                    FileInputStream in = new FileInputStream(file.getAbsolutePath());
                    defaultProps.load(in);
                    in.close();

                    // create application properties with default
                    appProps = new Properties(defaultProps);

                    try {
                        // user/application properties
                        in = new FileInputStream(file.getAbsolutePath());
                        appProps.load(in);
                        in.close();
                    } catch (Throwable th) {

                        AspireLog4j.setLoggerMessageLevel("ERROR ", Log4jLevels.ERROR, (Exception) th);
                    }
                }
            }
        }

    }

    /**
     * Get specific value from properties file
     *
     * @param key
     *
     * String key that we need its value
     *
     * @return
     *
     * passed key Value
     */
    public String getProperty(String key) {
        String val = null;

        if (key != null) {
            val = System.getProperty(key);
            if (val == null) {
                if (appProps != null) {
                    val = (String) appProps.getProperty(key);
                }
                if (val == null) {
                    val = defaultProps.getProperty(key);
                }
            }
        }
        return (val);

    }

    /**
     * Method used to get Boolean value from properties file
     *
     * @param string
     *
     * Key that we need to get its boolean value getProperty(String key) will be
     * invoked and Parse the value of this key to Boolean
     * @return
     *
     * Value of the Key as Boolean
     *
     */
    public boolean getBoolean(String string) {

        return Boolean.parseBoolean(getProperty(string));
    }

    /**
     * Sets Application/User String properties; default property values cannot
     * be set.
     */
    public void setProperty(String key, String val) {

        ArrayList<?> list = null;
        Object oldValue = null;

        oldValue = getProperty(key);

        appProps.setProperty(key, val);
        if (listeners.containsKey(key)) {
            list = (ArrayList<?>) listeners.get(key);
            int len = list.size();
            if (len > 0) {
                PropertyChangeEvent evt = new PropertyChangeEvent(this, key, oldValue, val);
                for (int i = 0; i < len; i++) {
                    if (list.get(i) instanceof PropertyChangeListener) {
                        ((PropertyChangeListener) list.get(i)).propertyChange(evt);
                    }
                }
            }
        }

    }

    /**
     * Method used to add Property-change Listener events occur whenever the
     * value of a bound property(Key changed) changed
     *
     * https://docs.oracle.com/javase/tutorial/uiswing/events/propertychangelistener.html
     *
     * @param key
     *
     * Add a property-change listener for a specific property
     *
     * @param listener
     *
     * The listener is called only when there is a change to the specified
     * property.
     *
     * @return
     *
     * check if the listener added or not
     *
     */
    public boolean addListener(String key, PropertyChangeListener listener) {
        boolean added = false;
        ArrayList<PropertyChangeListener> list = null;
        if (listeners == null) {
            listeners = new Hashtable<String, ArrayList<PropertyChangeListener>>();
        }

        if (!listeners.contains(key)) {
            list = new ArrayList<PropertyChangeListener>();
            added = list.add(listener);
            listeners.put(key, list);
        } else {
            list = (ArrayList<PropertyChangeListener>) listeners.get(key);
            added = list.add(listener);
        }
        return (added);
    }

    /**
     *
     * @param listener
     *
     *
     * remove specific listener
     *
     */
    public void removeListener(PropertyChangeListener listener) {
        if (listeners != null && listeners.size() > 0) {
            listeners.remove(listener);
        }
    }
}

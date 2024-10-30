import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.io.File;

public class PropertiesUtil {
    private static final Properties PROPERTIES = new Properties();
    static {
        loadProperties();
    }
    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }

    private static void loadProperties(){
        File applicationProperties = new File("resources/application.properties");
        try {
            FileInputStream inputStream = new FileInputStream(applicationProperties);
            PROPERTIES.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

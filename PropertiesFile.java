package utilities;

import java.io.*;
import java.util.Properties;

import org.apache.logging.log4j.*;


public class PropertiesFile {
    private InputStream inputStream;
    private Properties prop;
    private static final String pathToConfigFile =  System.getProperty("user.dir")+"\\resources\\"+"config.properties";
    private static Logger logger = LogManager.getLogger(PropertiesFile.class.getName());
    
    public PropertiesFile() throws IOException {
        prop = new Properties();
        String propFileName = "config.properties";

        inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

        if (inputStream != null) {
            prop.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }
    }

    public String getPropertyValue(String propertyName) {
        return prop.getProperty(propertyName);
    }

    public void setPropertyValue(String propertyName, String propertyValue) {
        FileInputStream fileIn = null;
        FileOutputStream fileOut = null;
        
        try {
            Properties properties = new Properties();
            File file = new File(pathToConfigFile);
            fileIn = new FileInputStream(file);
            properties.load(fileIn);    

            properties.setProperty(propertyName, propertyValue);

            fileOut = new FileOutputStream(file);
            properties.store(fileOut,"Connection Properties");
            fileOut.close();
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public void removePropertyValue(String propertyName) {
        FileOutputStream fileOut = null;
        FileInputStream fileIn = null;
        
        try {
            Properties properties = new Properties();
            File file = new File(pathToConfigFile);
            fileIn = new FileInputStream(file);
            properties.load(fileIn);    

            properties.remove(propertyName);

            fileOut = new FileOutputStream(file);
            properties.store(fileOut,"Connection Properties");
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void finalize() throws IOException {
        inputStream.close();
    }
}

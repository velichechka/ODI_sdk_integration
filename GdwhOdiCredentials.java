package odiApi;

import java.io.*;
import org.apache.logging.log4j.*;

import utilities.PropertiesFile;


public class GdwhOdiCredentials {

    private String odiLogin;
    private String odiPassword;
    private static Logger logger = LogManager.getLogger(GdwhOdiCredentials.class.getName());

    public GdwhOdiCredentials(String pLogin, String pPassword) {
        odiLogin = pLogin;
        odiPassword = pPassword;
    }

    public String getLogin() {
        return odiLogin;
    }

    public String getPassword() {
        return odiPassword;
    }

    public void saveToPropertiesFile() {
        try {
            PropertiesFile propFile = new PropertiesFile();
            propFile.setPropertyValue("OdiUserName", odiLogin);
            propFile.setPropertyValue("OdiPassword", odiPassword);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public void removePassFromProperties() {
        try {
            PropertiesFile propFile = new PropertiesFile();
            propFile.removePropertyValue("OdiPassword");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}

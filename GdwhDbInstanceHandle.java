package utilities;


import java.io.*;

import java.sql.*;

import org.apache.logging.log4j.*;


import utilities.PropertiesFile;


public class GdwhDbInstanceHandle {


    private String username;

    private String password;

    private String url;

    private Connection connection;

    private static GdwhDbInstanceHandle instance;

    private static Logger logger = LogManager.getLogger(GdwhDbInstanceHandle.class.getName());


    private GdwhDbInstanceHandle() {

        try {

            PropertiesFile propFile = new PropertiesFile();

            url = propFile.getPropertyValue("MasterRepositoryUrl");

            username = propFile.getPropertyValue("DbUser");

            password = propFile.getPropertyValue("DbPassword");


            connection = DriverManager.getConnection(url, username, password);

        } catch (Exception e) {

            logger.error("Can't connect to " + url);

            logger.error(e.getMessage());

        }

    }

    

    public static GdwhDbInstanceHandle getInstance() {

        if (instance == null) {

            instance = new GdwhDbInstanceHandle();

        }

        return instance;

    }

    

    public PreparedStatement prepareStatement(String query) throws SQLException {

        return connection.prepareStatement(query);

    }


    protected void finalize() throws SQLException, IOException {

        connection.close();

    }

}



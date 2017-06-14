package odiApi;


import oracle.odi.core.OdiInstance;

import oracle.odi.publicapi.samples.SimpleOdiInstanceHandle;


import java.io.IOException;

import org.apache.logging.log4j.*;


import utilities.PropertiesFile;


public class GdwhOdiInstanceHandle {


    private SimpleOdiInstanceHandle odiInstanceHandle;

    private String masterReposUrl;

    private String masterReposDriver;

    private String masterReposUser;

    private String masterReposPassword; 

    private String workReposName;

    private static Logger logger = LogManager.getLogger(GdwhOdiInstanceHandle.class.getName());

    

    public void create(String pOdiUsername,String pOdiPassword) throws IOException {

        GdwhOdiInstanceHandle.this.getPropValues();

        odiInstanceHandle = SimpleOdiInstanceHandle.create(masterReposUrl, masterReposDriver, 

                masterReposUser, masterReposPassword, workReposName, pOdiUsername, pOdiPassword);

    }

    

    public OdiInstance getOdiInstance() {

        return odiInstanceHandle.getOdiInstance();

    } 

    

    public void release() {

        odiInstanceHandle.release();

    }

    

    public void getPropValues() throws IOException {

        try {

            PropertiesFile propFile = new PropertiesFile();

            masterReposUrl = propFile.getPropertyValue("MasterRepositoryUrl");

            masterReposDriver = propFile.getPropertyValue("MasterRepositoryDriver");

            masterReposUser = propFile.getPropertyValue("MasterRepositoryUser");

            masterReposPassword = propFile.getPropertyValue("MasterRepositoryPassword");

            workReposName = propFile.getPropertyValue("WorkRepositoryName");

        } catch (Exception e) {

            logger.error(e.getMessage());

        }

    }

}



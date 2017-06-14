package main;


import oracle.odi.core.persistence.transaction.ITransactionStatus;

import oracle.odi.core.persistence.transaction.support.TransactionCallbackWithoutResult;

import oracle.odi.core.persistence.transaction.support.TransactionTemplate;

import oracle.odi.interfaces.TargetIsTemporaryException;

import oracle.odi.interfaces.basic.NoSourceSetException;


import java.util.*;

import java.io.IOException;


import gui.UserInterface;

import utilities.GdwhSvnRepo;

import odiApi.GdwhOdiInstanceHandle;

import odiApi.GdwhOdiInterface;

import odiApi.GdwhXlsMappingFile;


public class main {


    public static final boolean testing_run = true;


    public static void main(String[] args) throws NoSourceSetException, TargetIsTemporaryException, Exception {

/*

        GdwhSvnRepo svnRepo = new GdwhSvnRepo();

        svnRepo.update();

        

        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                UserInterface app;


                try {

                    app = new UserInterface();

                    app.setVisible(true);

                } catch (IOException e) {

                }


            }

        });


        while (UserInterface.buttonPress != 1) {

            Thread.sleep(10);



            if(UserInterface.buttonPress == 1){


                final String pPath = UserInterface.inputpath.getText();

                final String pInterfaceName = UserInterface.inputpopname.getText();

                String pOdiUsername = UserInterface.textUsername.getText();

                String pOdiPassword = String.valueOf(UserInterface.fieldPassword.getPassword());

                final String pProjectCode = UserInterface.textProjectName.getText();

                //TODO: folderName should be defined from pPath

                final String folderName;



                if (testing_run) {

                    folderName = "UNUSED";

                }

                // Connect the Repository

                final GdwhOdiInstanceHandle odiInstanceHandle = new GdwhOdiInstanceHandle();

                odiInstanceHandle.create(pOdiUsername,pOdiPassword);


                try {

                    TransactionTemplate transaction = new TransactionTemplate(odiInstanceHandle.getOdiInstance().getTransactionManager());

                    transaction.execute(new TransactionCallbackWithoutResult() {


                        protected void doInTransactionWithoutResult(ITransactionStatus pStatus) {


                            GdwhOdiInterface odiInterface = new GdwhOdiInterface(pInterfaceName, folderName, pProjectCode, odiInstanceHandle);

                            odiInterface.createInterfaceByExcelMapping(pPath);

                        }

                    });

                } catch(NoSuchElementException e) {

                    System.out.println("ERROR: IF wasn't found");

                } finally {

                    odiInstanceHandle.release();

                }


                UserInterface.buttonPress = 0;

            }

        }*/

        

        GdwhXlsMappingFile file = new GdwhXlsMappingFile("C:/Users/IUAD18GC/Desktop/PS_APPRAISAL_CMS_ver_1.xls", "mapping");

        String testStr = file.getLeadSrc();

        String[] arr = testStr.split(" ");

        for (String ss : arr) {

            System.out.println(ss);

        }

    } 


}



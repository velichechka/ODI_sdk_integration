package odiApi;


import oracle.odi.interfaces.interactive.support.*;

import oracle.odi.interfaces.interactive.support.actions.*;

import oracle.odi.interfaces.interactive.support.actions.InterfaceActionSetKM.KMType;

import oracle.odi.interfaces.interactive.support.mapping.matchpolicy.MappingMatchPolicyColumnName;

import oracle.odi.interfaces.interactive.support.mapping.automap.*;

import oracle.odi.interfaces.interactive.support.targetkeychoosers.TargetKeyChooserLazy;

import oracle.odi.interfaces.interactive.support.aliascomputers.AliasComputerFixed;

import oracle.odi.interfaces.interactive.support.clauseimporters.ClauseImporterDefault;

import oracle.odi.interfaces.interactive.support.references.breakers.*;

import oracle.odi.interfaces.interactive.support.km.optionretainer.*;

import oracle.odi.interfaces.interactive.exceptions.VetoActionException;

import oracle.odi.domain.project.interfaces.*;

import oracle.odi.domain.project.*;

import oracle.odi.domain.project.finder.*;

import oracle.odi.domain.project.OdiInterface.ExecutionLocation;

import oracle.odi.domain.model.OdiDataStore;

import oracle.odi.domain.model.finder.IOdiDataStoreFinder;

import oracle.odi.domain.DomainRuntimeException;

import oracle.odi.domain.topology.OdiContext;

import oracle.odi.domain.topology.finder.IOdiContextFinder;


import java.sql.*;

import java.util.*;

import org.apache.logging.log4j.*;


import test.TestData;

import utilities.GdwhDbInstanceHandle;

import odiApi.GdwhOdiInstanceHandle;

import odiApi.GdwhXlsMappingFile;



public class GdwhOdiInterface {


    public enum JoinType {CROSS, NATURAL, FULL, LEFT, RIGHT, INNER};


    private OdiInterface gdwhOdiInterface;

    private GdwhOdiInstanceHandle gdwhOdiInstanceHandle;

    private DataSet dataSet;

    private InteractiveInterfaceHelperWithActions interactiveHelper;

    private static int nextClauseOrder = 10;

    private static Logger logger = LogManager.getLogger(GdwhOdiInterface.class.getName());


    public static class StructMapping {

        private String columnName;

        private String columnMapping;


        StructMapping(String pColumnName, String pcolumnMapping) {

            StructMapping.this.columnName = pColumnName.toUpperCase();

            StructMapping.this.columnMapping = pcolumnMapping.toUpperCase();

        }

    }


    public static class StructDataStore {


        private String dataStoreName;

        private String modelCode;

        private String alias;


        public StructDataStore(String pDataStoreName, String pPhysicalSchemaName, String pAlias) {

            StructDataStore.this.dataStoreName = pDataStoreName.toUpperCase();

            StructDataStore.this.alias = pAlias.toUpperCase();

            StructDataStore.this.modelCode = getOdiModel(pPhysicalSchemaName.toUpperCase(), pDataStoreName.toUpperCase());

        }


        public StructDataStore(String pDataStoreName, String pPhysicalSchemaName) {

            StructDataStore.this.dataStoreName = pDataStoreName.toUpperCase();

            StructDataStore.this.modelCode = getOdiModel(pPhysicalSchemaName.toUpperCase(), pDataStoreName.toUpperCase());

        }

    }


    public static class StructJoin {


        private String srcDataStore1Alias;

        private String srcDataStore2Alias;

        private String joinCondition;

        private JoinType joinType;

        private int clauseOrder;


        public StructJoin(String pSrcDataStore1Alias,

                String pSrcDataStore2Alias,

                String pJoinCondition,

                GdwhOdiInterface.JoinType pJoinType,

                int pClauseOrder) {


            StructJoin.this.srcDataStore1Alias = pSrcDataStore1Alias.toUpperCase();

            StructJoin.this.srcDataStore2Alias = pSrcDataStore2Alias.toUpperCase();

            StructJoin.this.joinCondition = pJoinCondition.toUpperCase();

            StructJoin.this.joinType = pJoinType;

            StructJoin.this.clauseOrder = pClauseOrder;

        }


        public StructJoin(String pSrcDataStore1Alias,

                String pSrcDataStore2Alias,

                String pJoinCondition,

                GdwhOdiInterface.JoinType pJoinType) {

            this(pSrcDataStore1Alias, pSrcDataStore2Alias, pJoinCondition, pJoinType, nextClauseOrder);

            nextClauseOrder += 10;

        }

    }



    public GdwhOdiInterface(String pInterfaceName, String pFolderName, String pProjectCode, GdwhOdiInstanceHandle pOdiInstanceHandle) {

        gdwhOdiInstanceHandle = pOdiInstanceHandle;


        try {

            // Search of the interface

            gdwhOdiInterface = ((IOdiInterfaceFinder)pOdiInstanceHandle.getOdiInstance().getTransactionalEntityManager().getFinder(OdiInterface.class)).findByName(pInterfaceName, pProjectCode).iterator().next();

        } catch (NoSuchElementException e) {

            createNewInterface(pInterfaceName, pFolderName, pProjectCode);

        }


        dataSet = gdwhOdiInterface.getDataSets().get(0);

        interactiveHelper = new InteractiveInterfaceHelperWithActions(gdwhOdiInterface, gdwhOdiInstanceHandle.getOdiInstance(), gdwhOdiInstanceHandle.getOdiInstance().getTransactionalEntityManager());

    };


    public void createInterfaceByExcelMapping(String pPath, String pSheetName) {


        //reading the mapping file

        GdwhXlsMappingFile file = new GdwhXlsMappingFile(pPath, pSheetName);

        ArrayList<GdwhOdiInterface.StructMapping> mapping = file.readMappingsData();

        StructDataStore targetDataStore = file.readTargetDataStore();

        String filter = file.readFilter();

        

        //setup the IF according to data from file

        addTargetDataStore(targetDataStore);

        

        if (main.main.testing_run) {


            TestData.generateTestData();


            //addTargetDataStore(new StructDataStore("PS_APPRAISAL", "DWHPS"));

            addSourceDataStore(TestData.getTestDataStore());

            addJoin(TestData.getTestJoin());

            //addFilter(TestData.getTestFilter());

            setKM();

        }


        addFilter(filter);

        setMapping(mapping);

        if (file.existsDistinctOption()) {

            setDistinct();

        }

    }


    public void createInterfaceByExcelMapping(String pPath ) {

        createInterfaceByExcelMapping(pPath, "mapping");

    }


    private void createNewInterface(String pInterfaceName, String pFolderName, String pProjectCode) {

        try {

            OdiContext context = ((IOdiContextFinder)gdwhOdiInstanceHandle.getOdiInstance().getTransactionalEntityManager().getFinder(OdiContext.class)).findDefaultContext();

            OdiFolder folder = ((IOdiFolderFinder)gdwhOdiInstanceHandle.getOdiInstance().getTransactionalEntityManager().getFinder(OdiFolder.class)).findByName(pFolderName, pProjectCode).iterator().next();

            gdwhOdiInterface = new OdiInterface(folder, pInterfaceName, context);

        } catch (NoSuchElementException e) {

            logger.error(e.getMessage());

        }

    }


    private void removeAllSourceDataStore() {


        //for deleting all the dependencies

        IDataStoreReferenceBreaker[] internalBreakers = new IDataStoreReferenceBreaker[2];

        internalBreakers[0] = new DataStoreReferenceBreakerClauses();

        internalBreakers[1] = new DataStoreReferenceBreakerTargetMapping();


        for (SourceDataStore i : dataSet.getSourceDataStores()) {

            interactiveHelper.performAction(new InterfaceActionRemoveSourceDataStore(i, new DataStoreReferenceBreakerComposer(internalBreakers)));

        }

        interactiveHelper.computeSourceSets();

        interactiveHelper.preparePersist();

    }


    private void removeAllMapping() {

        for (TargetColumn i : gdwhOdiInterface.getTargetDataStore().getColumns()) {

            interactiveHelper.performAction(new InterfaceActionOnTargetMappingSetSql(i.getName(), null, dataSet));

        }

        interactiveHelper.computeSourceSets();

        interactiveHelper.preparePersist();

    }


    private void setMapping(ArrayList<StructMapping> pMapping) {


        ExecutionLocation exLocation;


        removeAllMapping();


        for (int i = 0; i < pMapping.size(); i++) {


            try {

                interactiveHelper.performAction(new InterfaceActionOnTargetMappingSetSql(pMapping.get(i).columnName, pMapping.get(i).columnMapping, dataSet));


                if (pMapping.get(i).columnMapping.matches("#(.*)PRM(.*)")) {

                    exLocation = ExecutionLocation.WORK;

                } else if (pMapping.get(i).columnMapping.contains("ORA_HASH(@)")) {

                    //TODO: set UD2 as true for fields that aren't used in checksum

                    interactiveHelper.performAction(new InterfaceActionOnTargetColumnSetUD(pMapping.get(i).columnName, 1, true));

                    //TODO: change exLocation to TARGET

                    //exLocation = ExecutionLocation.TARGET;

                    exLocation = ExecutionLocation.SOURCE;

                }

                else {

                    exLocation = ExecutionLocation.SOURCE;

                }


                interactiveHelper.performAction(new InterfaceActionOnTargetMappingSetLocation(pMapping.get(i).columnName, exLocation, dataSet));

            } catch (VetoActionException e) {

                logger.error(e.getMessage());

            }

        }


        if (interactiveHelper.areSourceSetsDirty()) {

            interactiveHelper.computeSourceSets();

        }


        interactiveHelper.preparePersist();


        logger.info("Mapping was successfully updated");

    }


    private void addSourceDataStore(ArrayList<StructDataStore> pDataStores) {


        removeAllSourceDataStore();


        for (int i = 0; i < pDataStores.size(); i++) {


            if (pDataStores.get(i).dataStoreName == null || pDataStores.get(i).dataStoreName.isEmpty()) {

                logger.error("Source data store with alias " + pDataStores.get(i).alias + " wasn't added: "

                        + "name of the data store wasn't specified");

                return;

            }


            if (pDataStores.get(i).modelCode == null || pDataStores.get(i).modelCode.isEmpty()) {

                logger.error("Source data store with alias " + pDataStores.get(i).alias + " wasn't added: "

                        + "model code wasn't specified");

                return;

            }


            if (pDataStores.get(i).alias == null || pDataStores.get(i).alias.isEmpty()) {

                pDataStores.get(i).alias = pDataStores.get(i).dataStoreName;

            }


            try {

                // Search of the datastore by its name and model code

                OdiDataStore odiDataStore = ((IOdiDataStoreFinder)gdwhOdiInstanceHandle.getOdiInstance().getTransactionalEntityManager().getFinder(OdiDataStore.class)).findByName(pDataStores.get(i).dataStoreName, pDataStores.get(i).modelCode);

                // Adding datastore to the interface

                interactiveHelper.performAction(new InterfaceActionAddSourceDataStore(odiDataStore, dataSet, new AliasComputerFixed(pDataStores.get(i).alias), new ClauseImporterDefault(), new AutoMappingComputerColumnName()));


                logger.info(pDataStores.get(i).dataStoreName + " " + pDataStores.get(i).alias + " was successfully added as a source data store");

            } catch (IllegalArgumentException e) {

                logger.error("Source data store with alias " + pDataStores.get(i).alias + " wasn't added: "

                        + pDataStores.get(i).dataStoreName + " doesn't exist in model " + pDataStores.get(i).modelCode);

            } catch (VetoActionException e) {

                logger.error("Source data store with alias " + pDataStores.get(i).alias + " wasn't added: "

                        + e.getMessage());

            }

        }


        interactiveHelper.computeSourceSets();

        interactiveHelper.preparePersist();

    }


    private void addJoin(ArrayList<StructJoin> pJoins) {


        boolean outer1 = false;

        boolean outer2 = false;

        Join join;


        SourceDataStore srcDataStore1 = null;

        SourceDataStore srcDataStore2 = null;


        for (int i = 0; i < pJoins.size(); i++) {


            //Search of 2 SourceDataStores which take part in join by their alias

            for (SourceDataStore iterableSrc : dataSet.getSourceDataStores()) {


                if (iterableSrc.getAlias().equalsIgnoreCase(pJoins.get(i).srcDataStore1Alias)) {

                    srcDataStore1 = iterableSrc;

                }

                if (iterableSrc.getAlias().equalsIgnoreCase(pJoins.get(i).srcDataStore2Alias)) {

                    srcDataStore2 = iterableSrc;

                }

                if (srcDataStore1 != null && srcDataStore2 != null) {

                    break;

                }

            }


            try {


                if (pJoins.get(i).joinType == JoinType.CROSS || pJoins.get(i).joinType == JoinType.NATURAL) {

                    //Adding new CROSS/NATURAL join

                    join = new Join(srcDataStore1, srcDataStore2, ExecutionLocation.SOURCE);

                } else {

                    //Adding new INNER/OUTER join

                    join = new Join(dataSet, pJoins.get(i).joinCondition, true, ExecutionLocation.SOURCE);

                    join.setAttachedDataStore1(srcDataStore1);

                    join.setAttachedDataStore2(srcDataStore2);


                    switch (pJoins.get(i).joinType) {

                    case FULL:

                        outer1 = true;

                        outer2 = true;

                        break;

                    case LEFT:

                        outer1 = true;

                        outer2 = false;

                        break;

                    case RIGHT:

                        outer1 = false;

                        outer2 = true;

                        break;

                    case INNER:

                        outer1 = false;

                        outer2 = false;

                        break;

                    }

                    //Setting type of the join (FULL/LEFT/RIGHT/INNER)

                    interactiveHelper.performAction(new InterfaceActionOnJoinSetJoinProperties(join, true, outer1, outer2));

                }

                //Set the join order value

                join.setOrdered(true);

                join.setClauseOrder(pJoins.get(i).clauseOrder);


                logger.info("Join for " + pJoins.get(i).srcDataStore1Alias + " and " + pJoins.get(i).srcDataStore2Alias 

                        + " was succesfully added");

            } catch (DomainRuntimeException e) {


                switch (e.getErrorID()) {

                case "ODI-17637":

                    logger.error("Join for " + pJoins.get(i).srcDataStore1Alias + " and " + pJoins.get(i).srcDataStore2Alias 

                            + " wasn't added: source data store isn't used in IF\n");

                    break;

                case "ODI-17640":

                    logger.error("Join for " + pJoins.get(i).srcDataStore1Alias + " and " + pJoins.get(i).srcDataStore2Alias 

                            + " wasn't added: condition for join must be set up");

                    break;

                default:

                    logger.error("Join for " + pJoins.get(i).srcDataStore1Alias + " and " + pJoins.get(i).srcDataStore2Alias 

                            + " wasn't added: " + e.getMessage());

                }

            }

        }


        interactiveHelper.computeSourceSets();

        interactiveHelper.preparePersist();

    }


    private void addTargetDataStore(StructDataStore pDataStore) {

        //Search of the datastore by its name and model code

        OdiDataStore odiDataStore = ((IOdiDataStoreFinder)gdwhOdiInstanceHandle.getOdiInstance().getTransactionalEntityManager().getFinder(OdiDataStore.class)).findByName(pDataStore.dataStoreName, pDataStore.modelCode);

        //Set target datastore

        interactiveHelper.performAction(new InterfaceActionSetTargetDataStore(odiDataStore, new MappingMatchPolicyColumnName(), new AutoMappingComputerLazy(), new AutoMappingComputerLazy(), new TargetKeyChooserLazy()));

        interactiveHelper.computeSourceSets();

        interactiveHelper.preparePersist();


        logger.info(pDataStore.dataStoreName + " was successfully added as a target data store");

    }


    private void addFilter(String pFilterCondition) {

        if (pFilterCondition == null || pFilterCondition.isEmpty()) {

            return;

        }


        ExecutionLocation exLocation;

        boolean containsOdiFunction = false;

        java.util.Collection<OdiUserFunction> userOdiFunctions = ((IOdiUserFunctionFinder)gdwhOdiInstanceHandle.getOdiInstance().getTransactionalEntityManager().getFinder(OdiUserFunction.class)).findAll();


        for (OdiUserFunction userFuntion : userOdiFunctions) {

            if (pFilterCondition.toUpperCase().contains(userFuntion.getName().toUpperCase() + "(")

                    && userFuntion.getName().length() > 1) {

                containsOdiFunction = true;

            }

        }


        if (containsOdiFunction) {

            exLocation = ExecutionLocation.WORK;

        } else {

            exLocation = ExecutionLocation.SOURCE;

        }


        interactiveHelper.performAction(new InterfaceActionAddFilter(dataSet, pFilterCondition.toUpperCase(), exLocation));

        interactiveHelper.computeSourceSets();

        interactiveHelper.preparePersist();


        logger.info("Filter was successfully added");

    }


    private void setKM() {

        String projectCode = gdwhOdiInterface.getFolder().getProject().getCode();

        OdiIKM ikm;

        String ikmName;


        if (projectCode.equals("LANDING")) {

            ikmName = "IKM Oracle Landing Insert";

        } else {

            if (gdwhOdiInterface.getName().matches("^(PS_|ST_)(.*)")) {

                ikmName = "IKM Oracle Checksum";

            } else {

                ikmName = "IKM Oracle (Mat) View / Table";

            }

        }


        ikm = ((IOdiIKMFinder)gdwhOdiInstanceHandle.getOdiInstance().getTransactionalEntityManager().getFinder(OdiIKM.class)).findByName(ikmName, projectCode).iterator().next();

        interactiveHelper.performAction(new InterfaceActionSetKM(ikm, gdwhOdiInterface.getTargetDataStore(), KMType.IKM, new KMOptionRetainerLazy()));

    }

    

    private void setDistinct() {

        interactiveHelper.performAction(new InterfaceActionOnStagingAreaSetIndicator(true, InterfaceActionOnStagingAreaSetIndicator.StagingAreaIndicator.DISTINCT_ROW));

    }


    private static String getOdiModel (String pSchemaName, String pDataStoreName) {

        try {

            String result;

            String query = "select model_name from groupdwh.odi_pschema_model_link_wo_dupl where schema_name = ?1 and res_name = ?2";


            //connect to the database for getting ODImodel based on physical schema and table name

            GdwhDbInstanceHandle dbConn = GdwhDbInstanceHandle.getInstance();


            PreparedStatement stmt = dbConn.prepareStatement(query);

            stmt.setString(1, pSchemaName.toUpperCase());

            stmt.setString(2, pDataStoreName.toUpperCase());

            ResultSet rs = stmt.executeQuery();

            rs.next();

            result = rs.getString("MODEL_NAME");

            return result;

        } catch (SQLException e) {

            logger.error("ODI model for " + pSchemaName + "." + pDataStoreName + " wasn't found: "

                    + e.getMessage());

            return null;

        }

    }

}




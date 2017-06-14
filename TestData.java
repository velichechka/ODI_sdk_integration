package test;


import java.util.ArrayList;


import odiApi.GdwhOdiInterface;

import odiApi.GdwhOdiInterface.StructDataStore;

import odiApi.GdwhOdiInterface.StructJoin;


public class TestData {

    

    static private ArrayList<StructDataStore> testDataStore = new ArrayList<StructDataStore>();

    static private ArrayList<StructJoin> testJoin = new ArrayList<StructJoin>();

    static private String testFilter;


    static public void generateTestData() {

        

        //test data for addSourceDataStore function

        String testDataStoreName = "VI_LS";

        String testPhysSchemaName = "DWHPS";

        String testAlias = "VI_LS2";


        testDataStore.add(new StructDataStore(testDataStoreName, testPhysSchemaName, testAlias));

        testDataStore.add(new StructDataStore("CMS_T_BD_TYPECOLLATERAL", "DWHLH", "T_BD_TYPECOLLATERAL2"));


        //test data for addJoin function

        String srcDataStore1Alias = "T_BD_TYPECOLLATERAL2";

        String srcDataStore2Alias = "VI_LS2";

        String joinCondition = "VI_LS2.LS_ID = T_BD_TYPECOLLATERAL2.ID";

        GdwhOdiInterface.JoinType joinType = GdwhOdiInterface.JoinType.RIGHT;


        testJoin = new ArrayList<StructJoin>();

        testJoin.add(new StructJoin(srcDataStore1Alias, srcDataStore2Alias, joinCondition, joinType));

        testJoin.add(new StructJoin("VI_LS", "VI_LS85", "", GdwhOdiInterface.JoinType.LEFT));

        

        testFilter = "VI_LS2.LS_ID > 100 OR T_BD_TYPECOLLATERAL2.CODEKEY = 'BLA' and filter_account_vicont(VI_LS2.LS_DATZAK, VI_LS2.LS_VH2, 'N')";

    }

    

    static public ArrayList<StructDataStore> getTestDataStore() {

        return testDataStore;

    }

    

    static public ArrayList<StructJoin> getTestJoin() {

        return testJoin;

    }

    

    static public String getTestFilter() {

        return testFilter;

    }

}



package odiApi;

import java.io.*;
import java.util.*;

import org.apache.logging.log4j.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import odiApi.GdwhOdiInterface;


public class GdwhXlsMappingFile {

    private class StructXlsFileText {

        class FieldMapping {
            String attributeName;
            String mapping;    

            FieldMapping (String pAttributeName, String pMapping) {
                attributeName = pAttributeName;
                mapping = pMapping;
            }
        }

        String leadingSource;
        String generalConstraint;
        String entityName;
        ArrayList<FieldMapping> mappingList = new ArrayList<FieldMapping>();
    }

    private String filePath;
    private final String leadingSourceFieldName = "Leading source";
    private final String generalConstrFieldName = "General constraint";
    private final String mapTableStartFieldName = "Entity Name";
    private static Logger logger = LogManager.getLogger(GdwhXlsMappingFile.class.getName());
    private StructXlsFileText xlsFileText = new StructXlsFileText();

    public GdwhXlsMappingFile(String pFilePath, String pSheetName) {
        filePath = pFilePath;
        readAllXlsFile(pSheetName);
    }

    public GdwhOdiInterface.StructDataStore readTargetDataStore() {

        //TODO: find the way to determine the schema
        String schemaName = "DWHPS";
        GdwhOdiInterface.StructDataStore targetDataStore = new GdwhOdiInterface.StructDataStore(xlsFileText.entityName, schemaName);
        return targetDataStore;
    }

    public ArrayList<GdwhOdiInterface.StructMapping> readMappingsData() {
        
        ArrayList<GdwhOdiInterface.StructMapping> mapping = new ArrayList<GdwhOdiInterface.StructMapping>();
        String columnName;
        String columnMapping;

        for (int columnNum = 0; columnNum < xlsFileText.mappingList.size(); columnNum++) {
            if (xlsFileText.mappingList.get(columnNum).mapping == null
                    || xlsFileText.mappingList.get(columnNum).mapping.isEmpty())
            {
                continue;
            }

            columnName = xlsFileText.mappingList.get(columnNum).attributeName;
            columnMapping = xlsFileText.mappingList.get(columnNum).mapping;
            mapping.add(new GdwhOdiInterface.StructMapping(columnName, columnMapping));
        }
        return mapping;
    }

    public String readFilter() {
        String excessWord = "WHERE";
        
        if (xlsFileText.generalConstraint.toUpperCase().indexOf(excessWord) == 0) {
            xlsFileText.generalConstraint = xlsFileText.generalConstraint.substring(excessWord.length());
        }
        return xlsFileText.generalConstraint;
    }

    public boolean existsDistinctOption() {
        if (xlsFileText.leadingSource.toUpperCase().indexOf("DISTINCT") == 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getLeadSrc() {
        String returnStr;
        String excessWord;

        returnStr = xlsFileText.leadingSource.toUpperCase();

        if (existsDistinctOption()) {
            excessWord = "DISTINCT";
            returnStr = returnStr.substring(excessWord.length()).trim();
        }

        excessWord = "FROM";

        if (returnStr.indexOf(excessWord) == 0) {
            returnStr = returnStr.substring(excessWord.length()).trim();
        }
        return returnStr;
    }

    private void readAllXlsFile(String pSheetName) {
        try {
            InputStream in = new FileInputStream(filePath);
            HSSFWorkbook wb = new HSSFWorkbook(in);
            Sheet sheet = wb.getSheet(pSheetName);
            Iterator<Row> rows = sheet.rowIterator();
            boolean isMappingTextFields = false;

            while (rows.hasNext()) {
                Row row = rows.next();
                Cell firstCell = row.getCell(row.getFirstCellNum());

                try {
                    if (firstCell.getCellType() == Cell.CELL_TYPE_STRING) {
                        if (xlsFileText.entityName == null
                                || xlsFileText.leadingSource == null
                                || xlsFileText.generalConstraint == null)
                        {
                            switch (firstCell.getStringCellValue()) {
                            case leadingSourceFieldName:
                                xlsFileText.leadingSource = row.getCell(row.getFirstCellNum() + 1).getStringCellValue().trim();
                                break;
                            case generalConstrFieldName:
                                xlsFileText.generalConstraint = row.getCell(row.getFirstCellNum() + 1).getStringCellValue().trim();
                                break;
                            case mapTableStartFieldName:
                                row = rows.next();
                                firstCell = row.getCell(row.getFirstCellNum());
                                xlsFileText.entityName = firstCell.getStringCellValue().trim();
                                isMappingTextFields = true;
                                break;
                            }
                        }

                        if (isMappingTextFields) {
                            String attributeName;
                            String mapping;

                            if (row.getCell(row.getFirstCellNum() + 2) != null) {
                                attributeName = row.getCell(row.getFirstCellNum() + 1).getStringCellValue();
                                mapping = row.getCell(row.getFirstCellNum() + 2).getStringCellValue();

                                xlsFileText.mappingList.add(xlsFileText.new FieldMapping(attributeName, mapping));
                            }
                        }
                    }
                } catch (NullPointerException e) {}
            }
        } catch (FileNotFoundException e) {
            logger.error("Path to mapping file is invalid");
        } catch(Exception e) {
            //FIXME: add adequate exception handle
            logger.error("Some strange exception was catched in AvalXlsMappingFile class");
        }
    }
}

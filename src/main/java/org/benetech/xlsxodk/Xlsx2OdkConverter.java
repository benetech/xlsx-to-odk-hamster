package org.benetech.xlsxodk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.benetech.xlsxjson.Converter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Xlsx2OdkConverter {

  private static Log logger = LogFactory.getLog(Xlsx2OdkConverter.class);

  public static final String ROW_NUM_KEY = "_row_num";

  public boolean dotsToNested = true;

  public boolean addRowNums = true;
  
  private Converter xls2Json;


  public Xlsx2OdkConverter(boolean dotsToNested, boolean addRowNums) {
    this.dotsToNested = dotsToNested;
    this.addRowNums = addRowNums;
  }

  public String convertToJson(File file) throws FileNotFoundException, IOException {


    return xls2Json.convertToJson(file);

  }

  public Map<String, List<Map<String, Object>>> convertToJavaCollections(File file)
      throws FileNotFoundException, IOException {

    final Map<String, List<Map<String, Object>>> workbookMap = xls2Json.convertToJavaCollections(file);

    return workbookMap;

  }


  public boolean isDotsToNested() {
    return dotsToNested;
  }

  public void setDotsToNested(boolean dotsToNested) {
    this.dotsToNested = dotsToNested;
  }

  public boolean isAddRowNums() {
    return addRowNums;
  }

  public void setAddRowNums(boolean addRowNums) {
    this.addRowNums = addRowNums;
  }
  
  

}

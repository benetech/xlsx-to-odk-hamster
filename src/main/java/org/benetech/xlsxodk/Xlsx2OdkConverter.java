package org.benetech.xlsxodk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.xlsxjson.Xlsx2JsonConverter;
import org.benetech.xlsxjson.Xlsx2JsonConverterBuilder;
import org.benetech.xlsxodk.conversion.SpecificationConverter;
import org.benetech.xlsxodk.util.CsvConversionUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Xlsx2OdkConverter {

  private static Log logger = LogFactory.getLog(Xlsx2OdkConverter.class);

  public static final String ROW_NUM_KEY = "_row_num";

  public boolean dotsToNested = true;

  public boolean addRowNums = true;

  private Xlsx2JsonConverter xlsx2Json;

  private Map<String, List<Map<String, Object>>> xlsxSection;
  
  private String definitionsCsv;
  
  private String propertiesCsv;

  public Xlsx2OdkConverter(boolean dotsToNested, boolean addRowNums) {
    this.dotsToNested = dotsToNested;
    this.addRowNums = addRowNums;
    Xlsx2JsonConverterBuilder builder = new Xlsx2JsonConverterBuilder();
    builder.dotsToNested(dotsToNested);
    builder.addRowNums(addRowNums);
    xlsx2Json = builder.build();
  }

  public String convertToJson(File file) throws FileNotFoundException, IOException {
    Map<String, Object> formDef = convertToJavaCollections(file);
    final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    return gson.toJson(formDef);
  }

  public Map<String, Object> convertToJavaCollections(File file)
      throws FileNotFoundException, IOException {
    xlsxSection = xlsx2Json.convertToJavaCollections(file);
    SpecificationConverter specificationConverter = new SpecificationConverter(xlsxSection);
    Map<String, Object> formDef = new LinkedHashMap<String, Object>();
    formDef.put("xlsx", xlsxSection);
    formDef.put("specification", specificationConverter.getSpecification());
    definitionsCsv = CsvConversionUtils.createDefinitionCsvFromDataTableModel(specificationConverter.getDataTableModel());
    propertiesCsv = CsvConversionUtils.createPropertiesCsv(specificationConverter.getProperties());
    return formDef;
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

  public String getDefinitionsCsv() {
    return definitionsCsv;
  }

  public String getPropertiesCsv() {
    return propertiesCsv;
  }

  
  
}

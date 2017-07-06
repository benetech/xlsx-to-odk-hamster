package org.benetech.xlsxodk.conversion;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.benetech.xlsxodk.Xlsx2OdkConverter;
import org.benetech.xlsxodk.Xlsx2OdkConverterBuilder;
import org.benetech.xlsxodk.exception.OdkXlsValidationException;
import org.benetech.xlsxodk.util.ConversionUtils;
import org.benetech.xlsxodk.validation.SettingsValidator;

import com.google.gson.Gson;


import org.junit.*;

public class SettingsConverterTest {

  Log logger = LogFactory.getLog(SettingsConverterTest.class);


  public void testToSettingsMap() throws Exception {

    List<Map<String, Object>> settingsRowList = new ArrayList<Map<String, Object>>();
    Map<String, Object> row1 = new HashMap<String, Object>();
    row1.put("setting_name", "color");
    row1.put("value", "blue");
    settingsRowList.add(row1);
    Map<String, Object> row2 = new HashMap<String, Object>();
    row2.put("setting_name", "shape");
    row2.put("value", "circle");
    settingsRowList.add(row2);

    SettingsConverter converter = new SettingsConverter(settingsRowList);
    Map<String, Map<String, Object>> result = converter.toSettingsMap(settingsRowList);
    logger.info(result);

    assertThat(result.get("shape").get("value"), is("circle"));
    assertThat(result.get("color").get("value"), is("blue"));


  }

  public void testOmitRowsWithMissingField() throws Exception {

    List<Map<String, Object>> settingsRowList = new ArrayList<Map<String, Object>>();
    Map<String, Object> row1 = new HashMap<String, Object>();
    row1.put("setting_name", "color");
    row1.put("value", "blue");
    settingsRowList.add(row1);
    Map<String, Object> row2 = new HashMap<String, Object>();
    row2.put("value", "circle");
    settingsRowList.add(row2);
    Map<String, Object> row3 = new HashMap<String, Object>();
    row3.put("setting_name", "size");
    row3.put("value", "large");
    settingsRowList.add(row3);

    SettingsConverter converter = new SettingsConverter(settingsRowList);
    List<Map<String, Object>> result = ConversionUtils.omitRowsWithMissingField(settingsRowList, "setting_name");
    logger.info(result);
    assertThat(result.size(), is(2));

  }

}

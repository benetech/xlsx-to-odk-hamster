package org.benetech.xlsxodk.validation;

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
import org.benetech.xlsxodk.conversion.SettingsConverter;
import org.benetech.xlsxodk.exception.OdkXlsValidationException;

import com.google.gson.Gson;


import org.junit.*;

public class SettingsValidatorTest {

  Log logger = LogFactory.getLog(SettingsValidatorTest.class);



  public void testCheckFormId() throws Exception {

    List<Map<String, Object>> settingsRowList = new ArrayList<Map<String, Object>>();
    Map<String, Object> row1 = new HashMap<String, Object>();
    row1.put("setting_name", "form_id");
    row1.put("value", "blue");
    settingsRowList.add(row1);


    SettingsConverter converter = new SettingsConverter(settingsRowList);

    SettingsValidator.checkFormId(converter.getSettingsMap(), settingsRowList);
  }


  @Test(expected = OdkXlsValidationException.class)
  public void testCheckMissingTableId() throws Exception {

    List<Map<String, Object>> settingsRowList = new ArrayList<Map<String, Object>>();
    Map<String, Object> row1 = new HashMap<String, Object>();
    row1.put("setting_name", "form_id");
    row1.put("value", "blue");
    settingsRowList.add(row1);


    SettingsConverter converter = new SettingsConverter(settingsRowList);

    SettingsValidator.checkTableId(converter.getSettingsMap());
  }

  public void testCheckMissingFormId() throws Exception {

    List<Map<String, Object>> settingsRowList = new ArrayList<Map<String, Object>>();
    Map<String, Object> row1 = new HashMap<String, Object>();
    row1.put("setting_name", "table_id");
    row1.put("value", "blue");
    settingsRowList.add(row1);
    Map<String, Object> row2 = new HashMap<String, Object>();
    row2.put("setting_name", "shape");
    row2.put("value", "circle");
    settingsRowList.add(row2);

    SettingsConverter converter = new SettingsConverter(settingsRowList);
    SettingsValidator.checkFormId(converter.getSettingsMap(), settingsRowList);

    logger.info(settingsRowList);
    logger.info(converter.getSettingsMap());

    assertThat(converter.getSettingsMap().get("form_id"), is(converter.getSettingsMap().get("table_id")));

  }
}

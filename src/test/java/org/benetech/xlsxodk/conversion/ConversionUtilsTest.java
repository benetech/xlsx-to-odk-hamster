package org.benetech.xlsxodk.conversion;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.xlsxodk.PredefSheet;
import org.benetech.xlsxodk.exception.OdkXlsValidationException;
import org.benetech.xlsxodk.util.ConversionUtils;
import org.benetech.xlsxodk.util.ValidationUtils;
import org.junit.Test;

public class ConversionUtilsTest {

  Log logger = LogFactory.getLog(ConversionUtilsTest.class);


  public void testToMap() throws Exception {

    List<Map<String, Object>> settingsRowList = new ArrayList<Map<String, Object>>();
    Map<String, Object> row1 = new HashMap<String, Object>();
    row1.put(SettingsConverter.SETTING_NAME_FIELD, "color");
    row1.put("value", "blue");
    settingsRowList.add(row1);
    Map<String, Object> row2 = new HashMap<String, Object>();
    row2.put(SettingsConverter.SETTING_NAME_FIELD, "shape");
    row2.put("value", "circle");
    settingsRowList.add(row2);

    Map<String, Map<String, Object>> result =
        ConversionUtils.toMap(settingsRowList, SettingsConverter.SETTING_NAME_FIELD);
    logger.info(result);

    assertThat((String)result.get("shape").get("value"), is("circle"));
    assertThat((String)result.get("color").get("value"), is("blue"));


  }

  public void testOmitRowsWithMissingField() throws Exception {

    List<Map<String, Object>> settingsRowList = new ArrayList<Map<String, Object>>();
    Map<String, Object> row1 = new HashMap<String, Object>();
    row1.put(SettingsConverter.SETTING_NAME_FIELD, "color");
    row1.put("value", "blue");
    settingsRowList.add(row1);
    Map<String, Object> row2 = new HashMap<String, Object>();
    row2.put("value", "circle");
    settingsRowList.add(row2);
    Map<String, Object> row3 = new HashMap<String, Object>();
    row3.put(SettingsConverter.SETTING_NAME_FIELD, "size");
    row3.put("value", "large");
    settingsRowList.add(row3);

    SettingsConverter converter = new SettingsConverter(settingsRowList);
    List<Map<String, Object>> result = ConversionUtils.omitRowsWithMissingField(settingsRowList,
        SettingsConverter.SETTING_NAME_FIELD);
    logger.info(result);
    assertThat(result.size(), is(2));

  }

  @Test
  public void testToMapOfGroups() throws Exception {

    List<Map<String, Object>> settingsRowList = new ArrayList<Map<String, Object>>();
    Map<String, Object> row = new HashMap<String, Object>();
    row.put(ChoicesConverter.CHOICE_LIST_NAME, "color");
    row.put("value", "blue");
    settingsRowList.add(row);
    row = new HashMap<String, Object>();
    row.put(ChoicesConverter.CHOICE_LIST_NAME, "color");
    row.put("value", "red");
    settingsRowList.add(row);
    row = new HashMap<String, Object>();
    row.put(ChoicesConverter.CHOICE_LIST_NAME, "color");
    row.put("value", "yellow");
    settingsRowList.add(row);
    row = new HashMap<String, Object>();
    row.put(ChoicesConverter.CHOICE_LIST_NAME, "color");
    row.put("value", "green");
    settingsRowList.add(row);
    row = new HashMap<String, Object>();
    row.put(ChoicesConverter.CHOICE_LIST_NAME, "shape");
    row.put("value", "circle");
    settingsRowList.add(row);
    row = new HashMap<String, Object>();
    row.put(ChoicesConverter.CHOICE_LIST_NAME, "shape");
    row.put("value", "square");
    settingsRowList.add(row);
    row = new HashMap<String, Object>();
    row.put(ChoicesConverter.CHOICE_LIST_NAME, "shape");
    row.put("value", "rectangle");
    settingsRowList.add(row);

    Map<String, List<Map<String, Object>>> result = ConversionUtils.toMapOfGroups(PredefSheet.CHOICES.getSheetName(),
        settingsRowList, ChoicesConverter.CHOICE_LIST_NAME);
    logger.info(result);

    // assertThat(result.get("shape").get("value"), is("circle"));
    // assertThat(result.get("color").get("value"), is("blue"));


  }

  @Test
  public void testCheckDuplicatesWithDuplicates() throws Exception {

    List<Map<String, Object>> rowList = new ArrayList<Map<String, Object>>();
    Map<String, Object> row = new HashMap<String, Object>();
    row.put(ChoicesConverter.CHOICE_LIST_NAME, "color");
    row.put("value", "blue");
    rowList.add(row);
    row = new HashMap<String, Object>();
    row.put(ChoicesConverter.CHOICE_LIST_NAME, "color");
    row.put("value", "red");
    rowList.add(row);
    row = new HashMap<String, Object>();
    row.put(ChoicesConverter.CHOICE_LIST_NAME, "color");
    row.put("value", "yellow");
    rowList.add(row);
    row = new HashMap<String, Object>();
    row.put(ChoicesConverter.CHOICE_LIST_NAME, "color");
    row.put("value", "green");
    rowList.add(row);
    row = new HashMap<String, Object>();
    row.put(ChoicesConverter.CHOICE_LIST_NAME, "shape");
    row.put("value", "circle");
    rowList.add(row);
    row = new HashMap<String, Object>();
    row.put(ChoicesConverter.CHOICE_LIST_NAME, "shape");
    row.put("value", "square");
    rowList.add(row);
    row = new HashMap<String, Object>();
    row.put(ChoicesConverter.CHOICE_LIST_NAME, "shape");
    row.put("value", "rectangle");
    rowList.add(row);

    try {
      ValidationUtils.checkDuplicates(PredefSheet.CHOICES.getSheetName(), rowList, ChoicesConverter.CHOICE_LIST_NAME);
    } catch (OdkXlsValidationException e) {
      assertThat(e.getMessage(), containsString("The following setting(s) are duplicated"));
      assertThat(e.getMessage(), containsString("color"));
      assertThat(e.getMessage(), containsString("shape"));

    }

  }

  @Test
  public void testCheckDuplicatesNoDuplicates() throws Exception {

    List<Map<String, Object>> rowList = new ArrayList<Map<String, Object>>();
    Map<String, Object> row = new HashMap<String, Object>();
    row.put(ChoicesConverter.CHOICE_LIST_NAME, "color");
    row.put("value", "blue");
    rowList.add(row);
    row = new HashMap<String, Object>();
    row.put(ChoicesConverter.CHOICE_LIST_NAME, "size");
    row.put("value", "XXL");
    rowList.add(row);
    row = new HashMap<String, Object>();
    row.put(ChoicesConverter.CHOICE_LIST_NAME, "make");
    row.put("value", "sedan");
    rowList.add(row);
    row = new HashMap<String, Object>();
    row.put(ChoicesConverter.CHOICE_LIST_NAME, "model");
    row.put("value", "V5");
    rowList.add(row);
    row = new HashMap<String, Object>();
    row.put(ChoicesConverter.CHOICE_LIST_NAME, "shape");
    row.put("value", "circle");


    ValidationUtils.checkDuplicates(PredefSheet.CHOICES.getSheetName(), rowList, ChoicesConverter.CHOICE_LIST_NAME);

    // Should not throw an exception

  }
}

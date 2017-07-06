package org.benetech.xlsxodk.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.benetech.xlsxodk.exception.OdkXlsValidationException;

public class SettingsValidator {
  public static final int TABLE_NAME_LENGTH = 50;
  

  public static void validate(List<Map<String, Object>> settingsRowList,
      Map<String, Map<String, Object>> settingsMap) {

    checkDuplicates(settingsRowList);
    checkTableId(settingsMap);
    checkFormId(settingsMap, settingsRowList);
    checkSurveyDisplayFields(settingsMap);

  }

  static void checkTableId(Map<String, Map<String, Object>> settingsMap) {
    if (settingsMap.get("table_id") == null || settingsMap.get("table_id").get("value") == null) {
      throw new OdkXlsValidationException(
          "Please define a 'table_id' setting_name on the settings sheet and specify the unique table id");
    }
    String tableIdValue = (String) settingsMap.get("table_id").get("value");
    if (tableIdValue.length() > TABLE_NAME_LENGTH) {
      throw new OdkXlsValidationException(
          "The value of the 'table_id' setting_name on the settings sheet cannot be more than "
              + TABLE_NAME_LENGTH + " characters long");
    }

  }

  static void checkFormId(Map<String, Map<String, Object>> settingsMap,
      List<Map<String, Object>> settingsRowList) {
    if (settingsMap.get("form_id") == null || settingsMap.get("form_id").get("value") == null) {
      String tableIdValue = (String) settingsMap.get("table_id").get("value");
      Map<String, Object> newSettingRow = new HashMap<String, Object>();
      newSettingRow.put("setting_name", "form_id");
      newSettingRow.put("value", tableIdValue);
      settingsRowList.add(newSettingRow);
      settingsMap.put("form_id", newSettingRow);
    }
    String formIdValue = (String) settingsMap.get("form_id").get("value");

    if (formIdValue.length() > TABLE_NAME_LENGTH) {
      throw new OdkXlsValidationException(
          "The value of the 'form_id' setting_name on the settings sheet cannot be more than "
              + TABLE_NAME_LENGTH + " characters long");
    }
  }

  static void checkSurveyDisplayFields(Map<String, Map<String, Object>> settingsMap) {
    Map<String, Object> surveyRow = settingsMap.get("survey");
    if (surveyRow == null) {
      throw new OdkXlsValidationException(
          "Please define a 'survey' setting_name on the settings sheet and specify the survey title under display.title");
    }
    Object display = surveyRow.get("display");
    if (display == null || (display instanceof String && StringUtils.isEmpty((String) display))) {
      throw new OdkXlsValidationException(
          "Please specify the survey title under the display.title column for the 'survey' setting_name on the settings sheet");
    }
    Map<String, Object> displayMap = (Map<String, Object>) display;
    if (displayMap.get("title") == null && displayMap.get("text") != null) {
      throw new OdkXlsValidationException(
          "Please specify the survey title under the display.title column for the 'survey' setting_name on the settings sheet");
    }
  }



  static void checkDuplicates(List<Map<String, Object>> settingsRowList) {
    Map<String, Integer> counts = new HashMap<String, Integer>();
    for (Map<String, Object> row : settingsRowList) {
      if (counts.get(row.get("setting_name")) == null) {
        counts.put((String) row.get("setting_name"), new Integer(1));
      } else {
        counts.put((String) row.get("setting_name"), counts.get(row.get("setting_name")) + 1);
      }
    }

    List<String> duplicateSettings = counts.entrySet().stream().filter(map -> map.getValue() > 1)
        .map(map -> map.getKey()).collect(Collectors.toList());

    if (duplicateSettings.size() > 0) {
      throw new OdkXlsValidationException(
          "The following settings are duplicated on the duplicates page: "
              + StringUtils.join(duplicateSettings));
    }
  }


  
}

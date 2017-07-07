package org.benetech.xlsxodk.validation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.benetech.xlsxodk.PredefSheet;
import org.benetech.xlsxodk.conversion.SettingsConverter;
import org.benetech.xlsxodk.exception.OdkXlsValidationException;
import org.benetech.xlsxodk.util.SettingsSheetUtils;
import org.benetech.xlsxodk.util.ValidationUtils;

public class SettingsValidator {
  public static final int TABLE_NAME_LENGTH = 50;


  public static void validate(List<Map<String, Object>> settingsRowList,
      Map<String, Map<String, Object>> settingsMap) {

    ValidationUtils.checkDuplicates(PredefSheet.SETTINGS.getSheetName(), settingsRowList,
        SettingsConverter.SETTING_NAME_FIELD);
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
      Map<String, Object> newSettingRow = new LinkedHashMap<String, Object>();
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
    Map<String, Object> surveyRow = SettingsSheetUtils.safeGetSurveySettingsRow(settingsMap);
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



}

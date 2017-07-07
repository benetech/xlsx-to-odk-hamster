package org.benetech.xlsxodk.util;

import java.util.Map;

import org.benetech.xlsxodk.exception.OdkXlsValidationException;

public class SettingsSheetUtils {
  public static Map<String, Object> safeGetSurveySettingsRow(
      Map<String, Map<String, Object>> anySettingsMap) {
    if (anySettingsMap.get("survey") == null
        || anySettingsMap.get("survey").get("display") == null) {
      // This was checked in validation. Something is very wrong.
      throw new OdkXlsValidationException(
          "Please define a 'survey' setting_name on the settings sheet and specify the survey title under display.title");
    }
    return anySettingsMap.get("survey");
  }
}

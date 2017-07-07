package org.benetech.xlsxodk.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.benetech.xlsxjson.Xlsx2JsonConverter;
import org.benetech.xlsxodk.exception.OdkXlsConversionException;
import org.benetech.xlsxodk.util.ConversionUtils;
import org.benetech.xlsxodk.util.SettingsSheetUtils;
import org.benetech.xlsxodk.validation.SettingsValidator;

public class SettingsConverter {
  
  public static final String SETTING_NAME_FIELD = "setting_name";

  List<Map<String, Object>> settingsRowList;

  Map<String, Map<String, Object>> settingsMap;
  
  Map<String, Object> locales;
  
  Map<String, Object> defaultLocale;

  // Package-scope constructor for testing
  SettingsConverter() {}

  public SettingsConverter(List<Map<String, Object>> incomingSettingsRowList) {
    this.settingsRowList = ConversionUtils.omitRowsWithMissingField(incomingSettingsRowList, SETTING_NAME_FIELD);
    this.settingsMap = ConversionUtils.toMap(settingsRowList, SETTING_NAME_FIELD);
    SettingsValidator.validate(settingsRowList, settingsMap);
    constructLocaleBlock();
    constructInitialBlock();
  }

  void constructInitialBlock() {
    Map<String, Object> surveySettingRow = SettingsSheetUtils.safeGetSurveySettingsRow(settingsMap);
    settingsMap.put("initial", surveySettingRow);
  }
  
  void constructLocaleBlock() {
    List<Map<String, Object>> locales = new ArrayList<Map<String, Object>>();
    Map<String, Object> defaultLocale = null;


    Map<String, Object> surveySettingRow = SettingsSheetUtils.safeGetSurveySettingsRow(settingsMap);
    Object formTitleObj = ((Map<String, Object>)surveySettingRow.get("display")).get("title");
    if (formTitleObj == null || formTitleObj instanceof String) {
      // No internationalization
      defaultLocale = new LinkedHashMap<String, Object>();
      defaultLocale.put("name", "default");
      defaultLocale.put("display", new LinkedHashMap<String, Object>());
      ((Map<String, Object>) defaultLocale.get("display")).put("text", "default");
      locales.add(defaultLocale);
    } else {
      Object firstTranslation = null;
      if (!(formTitleObj instanceof Map<?, ?>)) {
        throw new OdkXlsConversionException(
            "Something is wrong with the formatting of the multi-lingual form title in the settings worksheet.  Can't construct list of locales.");
      }
      Map<String, Object> formTitleMap = (Map<String, Object>) formTitleObj;
      for (Map.Entry<String, Object> formTitleEntry : formTitleMap.entrySet()) {
        String languageName = formTitleEntry.getKey();

        if (settingsMap.get(languageName) == null
            || settingsMap.get(languageName).get("display") == null) {
          final Map<String, Object> newLocale = new LinkedHashMap<String, Object>();
          newLocale.put("name", languageName);
          newLocale.put("display", new HashMap<String, Object>());
          ((Map<String, Object>) newLocale.get("display")).put("text", languageName);
          locales.add(newLocale);

        } else {
          Map<String, Object> languageRow = settingsMap.get(languageName);
          final Map<String, Object> newLocale = new LinkedHashMap<String, Object>();
          newLocale.put("name", languageName);
          newLocale.put("display", languageRow.get("display"));
          newLocale.put(Xlsx2JsonConverter.ROW_NUM_KEY, languageRow.get(Xlsx2JsonConverter.ROW_NUM_KEY));
          locales.add(newLocale);
        }
      }
    }
    
    sortLocales(locales);
    defaultLocale = locales.get(0);
    final Map<String, Object> localeRow = new LinkedHashMap<String, Object>();
    localeRow.put(SETTING_NAME_FIELD, "_locales");
    localeRow.put(Xlsx2JsonConverter.ROW_NUM_KEY, surveySettingRow.get(Xlsx2JsonConverter.ROW_NUM_KEY));
    localeRow.put("value", locales);
    
    final Map<String, Object> defaultLocaleRow = new LinkedHashMap<String, Object>();
    defaultLocaleRow.put(SETTING_NAME_FIELD, "_default_locale");
    defaultLocaleRow.put(Xlsx2JsonConverter.ROW_NUM_KEY, surveySettingRow.get(Xlsx2JsonConverter.ROW_NUM_KEY));

    defaultLocaleRow.put("value", defaultLocale.get("name"));

    this.locales = localeRow;
    this.defaultLocale = defaultLocaleRow;
    settingsMap.put("_locales", localeRow);
    settingsMap.put("_default_locale", defaultLocaleRow);
  }

  static void sortLocales(List<Map<String, Object>> locales) {
    Collections.sort(locales, new Comparator<Map<String, Object>>() {
      public int compare(Map<String, Object> locale1, Map<String, Object> locale2) {
        if (locale1.get(Xlsx2JsonConverter.ROW_NUM_KEY) != null && locale2.get(Xlsx2JsonConverter.ROW_NUM_KEY) != null) {
          Integer row1 = (Integer) (locale1.get(Xlsx2JsonConverter.ROW_NUM_KEY));
          Integer row2 = (Integer) (locale2.get(Xlsx2JsonConverter.ROW_NUM_KEY));
          return row1.compareTo(row2);
        } else if (locale1.get(Xlsx2JsonConverter.ROW_NUM_KEY) == null && locale2.get(Xlsx2JsonConverter.ROW_NUM_KEY) != null) {
          return 1;
        } else if (locale1.get(Xlsx2JsonConverter.ROW_NUM_KEY) != null && locale2.get(Xlsx2JsonConverter.ROW_NUM_KEY) == null) {
          return -1;
        } else {
          return 0;
        }
      }
    });
  }

  public List<Map<String, Object>> getSettingsRowList() {
    return settingsRowList;
  }

  public Map<String, Map<String, Object>> getSettingsMap() {
    return settingsMap;
  }

}

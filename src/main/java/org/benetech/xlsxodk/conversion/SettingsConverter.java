package org.benetech.xlsxodk.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.benetech.xlsxjson.Converter;
import org.benetech.xlsxodk.exception.OdkXlsConversionException;
import org.benetech.xlsxodk.exception.OdkXlsValidationException;
import org.benetech.xlsxodk.model.Locale;
import org.benetech.xlsxodk.util.ConversionUtils;
import org.benetech.xlsxodk.validation.SettingsValidator;

public class SettingsConverter {

  List<Map<String, Object>> settingsRowList;

  Map<String, Map<String, Object>> settingsMap;
  
  Map<String, Object> locales;
  
  Map<String, Object> defaultLocale;

  // Package-scope constructor for testing
  SettingsConverter() {}

  public SettingsConverter(List<Map<String, Object>> settingsRowList) {
    this.settingsRowList = ConversionUtils.omitRowsWithMissingField(settingsRowList, "setting_name");
    this.settingsMap = toSettingsMap(settingsRowList);
    SettingsValidator.validate(settingsRowList, settingsMap);

  }

  static Map<String, Map<String, Object>> toSettingsMap(List<Map<String, Object>> settingsRowList) {
    // Map<String,Map<String, Object>> newSettingsMap = new HashMap<String,Map<String, Object>>();
    Map<String, Map<String, Object>> newSettingsMap = settingsRowList.stream()
        .collect(Collectors.toMap(x -> (String) (x.get("setting_name")), x -> x));
    return newSettingsMap;
  }


  /*
   * "_locales": { "setting_name": "_locales", "_row_num": 12, "value": [ { "display": { "text": {
   * "default": "English", "spanish": "English" } }, "_row_num": 16, "name": "default" }, {
   * "display": { "text": { "default": "Español", "spanish": "Español" } }, "_row_num": 17, "name":
   * "spanish" } ] }, "_default_locale": { "setting_name": "_default_locale", "_row_num": 12,
   * "value": "default" },
   */
  void constructLocaleBlock() {
    List<Map<String, Object>> locales = new ArrayList<Map<String, Object>>();
    Map<String, Object> defaultLocale = null;

    if (settingsMap.get("survey") == null || settingsMap.get("survey").get("display") == null) {
      // This was checked in validation. Something is very wrong.
      throw new OdkXlsValidationException(
          "Please define a 'survey' setting_name on the settings sheet and specify the survey title under display.title");
    }

    Map<String, Object> surveySettingRow = settingsMap.get("survey");
    Object formTitleObj = ((Map<String, Object>)surveySettingRow.get("display")).get("title");
    if (formTitleObj == null || formTitleObj instanceof String) {
      // No internationalization
      defaultLocale = new HashMap<String, Object>();
      defaultLocale.put("name", "default");
      defaultLocale.put("display", new HashMap<String, Object>());
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
          final Map<String, Object> newLocale = new HashMap<String, Object>();
          newLocale.put("name", languageName);
          newLocale.put("display", new HashMap<String, Object>());
          ((Map<String, Object>) newLocale.get("display")).put("text", languageName);
          locales.add(newLocale);

        } else {
          Map<String, Object> languageRow = settingsMap.get(languageName);
          final Map<String, Object> newLocale = new HashMap<String, Object>();
          newLocale.put("name", languageName);
          newLocale.put("display", languageRow.get("display"));
          newLocale.put("_row_num", languageRow.get("_row_num"));
          locales.add(newLocale);
        }
      }


    }
    sortLocales(locales);
    defaultLocale = locales.get(0);
    // Not copying over row num; example formDef.json copies over title row num
    final Map<String, Object> localeRow = new HashMap<String, Object>();
    localeRow.put("setting_name", "_locales");
    localeRow.put("value", locales);
    
    final Map<String, Object> defaultLocaleRow = new HashMap<String, Object>();
    defaultLocaleRow.put("setting_name", "_default_locale");
    defaultLocaleRow.put("value", defaultLocale.get("name"));

    this.locales = defaultLocaleRow;
    this.defaultLocale = localeRow;
  }

  static void sortLocales(List<Map<String, Object>> locales) {
    Collections.sort(locales, new Comparator<Map<String, Object>>() {
      public int compare(Map<String, Object> locale1, Map<String, Object> locale2) {
        if (locale1.get("_row_num") != null && locale2.get("_row_num") != null) {
          Integer row1 = (Integer) (locale1.get("_row_num"));
          Integer row2 = (Integer) (locale2.get("_row_num"));
          return row1.compareTo(row2);
        } else if (locale1.get("_row_num") == null && locale2.get("_row_num") != null) {
          return 1;
        } else if (locale1.get("_row_num") != null && locale2.get("_row_num") == null) {
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

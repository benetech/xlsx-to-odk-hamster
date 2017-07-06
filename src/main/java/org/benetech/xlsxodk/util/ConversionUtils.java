package org.benetech.xlsxodk.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.benetech.xlsxjson.Converter;
import org.benetech.xlsxodk.exception.OdkXlsValidationException;

public class ConversionUtils {


  public static List<Map<String, Object>> omitRowsWithMissingField(
      List<Map<String, Object>> settingsRowList, String fieldName) {
    List<Map<String, Object>> newSettingsRowList = new ArrayList<Map<String, Object>>();

    for (Map<String, Object> row : settingsRowList) {
      if (row.get(fieldName) != null && !StringUtils.isEmpty((String) row.get(fieldName))) {
        if (!(row.get(fieldName) instanceof String)) {
          throw new OdkXlsValidationException(fieldName
              + " does not equal a string at settings sheet row " + row.get(Converter.ROW_NUM_KEY));
        }
        newSettingsRowList.add(row);
      }
    }
    return newSettingsRowList;
  }

}

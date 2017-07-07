package org.benetech.xlsxodk.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.benetech.xlsxjson.Xlsx2JsonConverter;
import org.benetech.xlsxodk.exception.OdkXlsValidationException;

public class ConversionUtils {


  public static List<Map<String, Object>> omitRowsWithMissingField(
      List<Map<String, Object>> settingsRowList, String fieldName) {
    List<Map<String, Object>> newSettingsRowList = new ArrayList<Map<String, Object>>();
    if (settingsRowList != null) {
      for (Map<String, Object> row : settingsRowList) {
        if (row.get(fieldName) != null && !StringUtils.isEmpty((String) row.get(fieldName))) {
          if (!(row.get(fieldName) instanceof String)) {
            throw new OdkXlsValidationException(
                fieldName + " does not equal a string at settings sheet row "
                    + row.get(Xlsx2JsonConverter.ROW_NUM_KEY));
          }
          newSettingsRowList.add(row);
        }
      }
    }
    return newSettingsRowList;
  }


  public static Map<String, Map<String, Object>> toMap(List<Map<String, Object>> rowList,
      String field) {
    Map<String, Map<String, Object>> newSettingsMap =
        rowList.stream().collect(Collectors.toMap(x -> (String) (x.get(field)), x -> x));
    return newSettingsMap;
  }


  public static Map<String, List<Map<String, Object>>> toMapOfGroups(String sheetName,
      List<Map<String, Object>> rowList, String field) {
    Map<String, List<Map<String, Object>>> newSettingsMap =
        new LinkedHashMap<String, List<Map<String, Object>>>();

    for (Map<String, Object> row : rowList) {
      if (row.get(field) == null) {
        throw new OdkXlsValidationException(field + " is unexpectedly null at sheet " + sheetName
            + " row " + row.get(Xlsx2JsonConverter.ROW_NUM_KEY));
      }
      String newGroupKey = (String) row.get(field);

      if (newSettingsMap.get(newGroupKey) == null) {
        List<Map<String, Object>> groupedList = new ArrayList<Map<String, Object>>();
        newSettingsMap.put(newGroupKey, groupedList);
      }
      newSettingsMap.get(newGroupKey).add(row);
    }
    return newSettingsMap;
  }
}

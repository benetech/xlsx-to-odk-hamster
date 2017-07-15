package org.benetech.xlsxodk.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.benetech.xlsxjson.Xlsx2JsonConverter;
import org.benetech.xlsxodk.exception.OdkXlsValidationException;

import com.rits.cloning.Cloner;

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
        new LinkedHashMap<String, Map<String, Object>>();
    for (Map<String, Object> row : rowList) {
      newSettingsMap.put((String) row.get(field), row);
    }
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


  public static Object deepExtendObject(Object o1, Object o2) {
    // stomp on o1 if o2 is not an object or array...
    if (o2 == null || !isMapOrList(o2)) {
      // don't need to deeply copy these non-array, non-objects.
      return o2;
    }
    // stomp on o1 if o1 is not an object or array...
    if (o1 == null || !isMapOrList(o1)) {
      // deep copy it...
      return deepCopyObject(o2);
    }
    // if o1 or o2 are arrays, stomp on o1 (don't merge arrays)...
    if (isList(o1) || isList(o2)) {
      // deep copy it...
      return deepCopyObject(o2);
    }

    // OK. they are both objects...
    if (o1 instanceof Map && o2 instanceof Map) {
      Map<String, Object> map1 = (Map<String, Object>) o1;
      Map<String, Object> map2 = (Map<String, Object>) o2;

      for (String key : map2.keySet()) {
        if (map1.containsKey(key)) {
          deepExtendObject(map1.get(key), map2.get(key));
        } else {
          map1.put(key, map2.get(key));
        }

      }

    } else {
      throw new OdkXlsValidationException(
          "Trying to copy a map that is not a map during data model creation.");
    }


    // return the extended object
    return o1;
  };

  public static Object deepCopyObject(Object o) {
    Cloner cloner = new Cloner();
    return cloner.deepClone(o);
  }

  private static boolean isMapOrList(Object o) {
    return (o instanceof List || o instanceof Map || o instanceof Object[]);
  }

  private static boolean isList(Object o) {
    return (o instanceof List || o instanceof Object[]);
  }
}

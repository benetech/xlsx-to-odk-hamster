package org.benetech.xlsxodk.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.benetech.xlsxjson.Xlsx2JsonConverter;
import org.benetech.xlsxodk.exception.OdkXlsValidationException;

public class ValidationUtils {

  public static void checkDuplicates(String sheetName, List<Map<String, Object>> settingsRowList, String fieldName) {
    Map<String, Integer> counts = new HashMap<String, Integer>();
    for (Map<String, Object> row : settingsRowList) {
      if (counts.get(row.get(fieldName)) == null) {
        counts.put((String) row.get(fieldName), new Integer(1));
      } else {
        counts.put((String) row.get(fieldName), counts.get(row.get(fieldName)) + 1);
      }
    }
  
    List<String> duplicateSettings = counts.entrySet().stream().filter(map -> map.getValue() > 1)
        .map(map -> map.getKey()).collect(Collectors.toList());
  
    if (duplicateSettings.size() > 0) {
      throw new OdkXlsValidationException(
          "The following setting(s) are duplicated on the " + sheetName + " page: "
              + StringUtils.join(duplicateSettings));
    }
  }

  public static boolean isEmptyObject(Object object) {
    if (object == null) {
      return true;
    }
    if (object instanceof Collection) {
      return CollectionUtils.isEmpty((Collection) object);
    }
    if (object instanceof String) {
      return StringUtils.isEmpty((String) object);
    }
    if (object instanceof Integer) {
      return ((Integer) object).equals(0);
    }
    if (object instanceof Map) {
      return ((Map) object).isEmpty();
    }
  
    throw new OdkXlsValidationException(
        "Unidentifiable object, can't determine whether it is empty: " + object.toString());
  }

  public static void errorIfFieldMissing(String sheetName, List<Map<String, Object>> rows,
      String requiredField, boolean nonEmpty) {
    for (Map<String, Object> row : rows) {
      if (row.get(requiredField) == null) {
        throw new OdkXlsValidationException("Missing cell value on sheet: " + sheetName
            + " for column: " + requiredField + " on row: " + row.get(Xlsx2JsonConverter.ROW_NUM_KEY));
      }
      Object value = row.get(requiredField);
      if (nonEmpty && isEmptyObject(value)) {
        throw new OdkXlsValidationException(
            "Cell value is unexpectedly empty on sheet: " + sheetName + " for column: "
                + requiredField + " on row: " + row.get(Xlsx2JsonConverter.ROW_NUM_KEY));
      }
    }
  }

  public static void errorIfFieldMissingForQueryType(String sheetName, List<Map<String, Object>> rows,
      String queryType, String requiredField, boolean nonEmpty) {
    for (Map<String, Object> row : rows) {
      if (queryType.equals(row.get("query_type")) && row.get(requiredField) == null) {
        throw new OdkXlsValidationException("Missing cell value on sheet: " + sheetName
            + " for column: " + requiredField + " on row: " + row.get(Xlsx2JsonConverter.ROW_NUM_KEY));
      }
    }
  }
  
  public static void sanityCheckFieldTypes( ) {
    // TODO: The JavaScript version of this depends on evaluating JavaScript functions.  
  }

}

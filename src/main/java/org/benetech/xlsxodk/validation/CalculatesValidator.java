package org.benetech.xlsxodk.validation;

import java.util.List;
import java.util.Map;

import org.benetech.xlsxodk.PredefSheet;
import org.benetech.xlsxodk.conversion.CalculatesConverter;
import org.benetech.xlsxodk.conversion.QueriesConverter;
import org.benetech.xlsxodk.util.ValidationUtils;

public class CalculatesValidator {

  public static void validate(List<Map<String, Object>> queriesRowList) {
    checkEmptyColumns(queriesRowList);
    ValidationUtils.checkDuplicates(PredefSheet.CALCULATES.getSheetName(), queriesRowList, QueriesConverter.QUERY_NAME_FIELD);

  }

  static void checkEmptyColumns(List<Map<String, Object>> queriesRowList) {
    ValidationUtils.errorIfFieldMissing(PredefSheet.CALCULATES.getSheetName(), queriesRowList, "calculation", true);


  }
}

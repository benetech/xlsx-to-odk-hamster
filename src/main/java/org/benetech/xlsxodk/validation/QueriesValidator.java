package org.benetech.xlsxodk.validation;

import java.util.List;
import java.util.Map;

import org.benetech.xlsxodk.PredefSheet;
import org.benetech.xlsxodk.conversion.QueriesConverter;
import org.benetech.xlsxodk.util.ValidationUtils;

public class QueriesValidator {

  public static void validate(List<Map<String, Object>> queriesRowList) {
    checkEmptyColumns(queriesRowList);
  }

  static void checkEmptyColumns(List<Map<String, Object>> queriesRowList) {
    ValidationUtils.errorIfFieldMissing(PredefSheet.QUERIES.getSheetName(), queriesRowList, "query_type", true);
    ValidationUtils.errorIfFieldMissingForQueryType(PredefSheet.QUERIES.getSheetName(), queriesRowList, "csv", "uri", true);
    ValidationUtils.errorIfFieldMissingForQueryType(PredefSheet.QUERIES.getSheetName(), queriesRowList, "csv", "callback",
        true);
    ValidationUtils.errorIfFieldMissingForQueryType(PredefSheet.QUERIES.getSheetName(), queriesRowList, "ajax", "uri", true);
    ValidationUtils.errorIfFieldMissingForQueryType(PredefSheet.QUERIES.getSheetName(), queriesRowList, "ajax", "callback",
        true);
    ValidationUtils.errorIfFieldMissingForQueryType(PredefSheet.QUERIES.getSheetName(), queriesRowList, "linked_table",
        "linked_form_id", true);
    ValidationUtils.errorIfFieldMissingForQueryType(PredefSheet.QUERIES.getSheetName(), queriesRowList, "linked_table",
        "selection", true);
    ValidationUtils.errorIfFieldMissingForQueryType(PredefSheet.QUERIES.getSheetName(), queriesRowList, "linked_table",
        "selectionArgs", true);
    ValidationUtils.errorIfFieldMissingForQueryType(PredefSheet.QUERIES.getSheetName(), queriesRowList, "linked_table",
        "auxillaryHash", true);
    ValidationUtils.checkDuplicates(PredefSheet.QUERIES.getSheetName(), queriesRowList, QueriesConverter.QUERY_NAME_FIELD);

  }
}

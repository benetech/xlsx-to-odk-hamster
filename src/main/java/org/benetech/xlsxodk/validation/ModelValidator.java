package org.benetech.xlsxodk.validation;

import java.util.List;
import java.util.Map;

import org.benetech.xlsxodk.PredefSheet;
import org.benetech.xlsxodk.conversion.ModelConverter;
import org.benetech.xlsxodk.conversion.QueriesConverter;
import org.benetech.xlsxodk.util.ValidationUtils;

public class ModelValidator {

  public static void validate(List<Map<String, Object>> modelRowList) {
    checkEmptyColumns(modelRowList);
  }

  static void checkEmptyColumns(List<Map<String, Object>> modelRowList) {
    ValidationUtils.errorIfFieldMissing(PredefSheet.MODEL.getSheetName(), modelRowList, "type", true);

    ValidationUtils.checkDuplicates(PredefSheet.MODEL.getSheetName(), modelRowList, ModelConverter.MODEL_NAME_FIELD);

  }
}

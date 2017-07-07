package org.benetech.xlsxodk.validation;

import java.util.List;
import java.util.Map;

import org.benetech.xlsxodk.PredefSheet;
import org.benetech.xlsxodk.conversion.PromptTypesConverter;
import org.benetech.xlsxodk.conversion.QueriesConverter;
import org.benetech.xlsxodk.util.ValidationUtils;

public class PromptTypesValidator {

  public static void validate(List<Map<String, Object>> promptTypeRowList) {
    checkEmptyColumns(promptTypeRowList);
  }

  static void checkEmptyColumns(List<Map<String, Object>> promptTypeRowList) {
    ValidationUtils.errorIfFieldMissing(PredefSheet.PROMPT_TYPES.getSheetName(), promptTypeRowList,
        "type", true);
    ValidationUtils.checkDuplicates(PredefSheet.PROMPT_TYPES.getSheetName(), promptTypeRowList,
        PromptTypesConverter.PROMPT_TYPE_NAME_FIELD);
  }
}

package org.benetech.xlsxodk.validation;

import java.util.List;
import java.util.Map;

import org.benetech.xlsxodk.PredefSheet;
import org.benetech.xlsxodk.util.ValidationUtils;

public class ChoicesValidator {

  public static void validate(List<Map<String, Object>> settingsRowList) {
    checkEmptyColumns(settingsRowList);
  }

  static void checkEmptyColumns(List<Map<String, Object>> settingsRowList) {
    ValidationUtils.errorIfFieldMissing(PredefSheet.CHOICES.getSheetName(), settingsRowList, "data_value", true);
    ValidationUtils.errorIfFieldMissing(PredefSheet.CHOICES.getSheetName(), settingsRowList, "display", true);
  }
}

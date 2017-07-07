package org.benetech.xlsxodk.validation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.benetech.xlsxodk.PredefSheet;
import org.benetech.xlsxodk.conversion.SpecificationConverter;
import org.benetech.xlsxodk.exception.OdkXlsValidationException;

public class SpecificationValidator {

  public static void validateAfterSectionDefinition(SpecificationConverter spec) {
    checkSurveySheet(spec.getSectionSheets());

  }


  public static void checkQueriesChoicesColumnsDuplicates(
      Map<String, Map<String, Object>> queriesMap,
      Map<String, List<Map<String, Object>>> choicesMap) {
    if (queriesMap != null && choicesMap != null) {
      Collection<String> overlap =
          CollectionUtils.intersection(queriesMap.keySet(), choicesMap.keySet());
      if (overlap != null && !overlap.isEmpty()) {
        throw new OdkXlsValidationException("Reuse of the name(s) " + StringUtils.join(overlap, ",")
            + ". The 'choice_list_name' on 'choices' sheet and 'query_name' on 'queries' sheet cannot be identical.");
      }
    }
  }

  public static void validateSheetNames(Collection<String> sheetNames) {
    for (String sheetName : sheetNames) {
      if (sheetName.contains(".")) {
        throw new OdkXlsValidationException("Dots are not allowed in sheet names.");
      }
      if (sheetName.startsWith("_")) {
        throw new OdkXlsValidationException("Sheet names cannot begin with '_'.");
      }
    }
  }

  public static void checkSurveySheet(Map<String, List<Map<String, Object>>> sectionSheets) {
    if (!sectionSheets.containsKey(PredefSheet.SURVEY.getSheetName())) {
      throw new OdkXlsValidationException("Missing survey sheet.");
    }
  }

}

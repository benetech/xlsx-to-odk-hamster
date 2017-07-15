package org.benetech.xlsxodk.validation;

import static org.benetech.xlsxjson.Xlsx2JsonConverter.ROW_NUM_KEY;
import static org.benetech.xlsxodk.PredefSheet.CHOICES;
import static org.benetech.xlsxodk.PredefSheet.QUERIES;
import static org.benetech.xlsxodk.SectionToken.PROMPTS;
import static org.benetech.xlsxodk.SectionToken.SECTION_NAME;
import static org.benetech.xlsxodk.Token.SECTIONS;
import static org.benetech.xlsxodk.Token.TYPE;
import static org.benetech.xlsxodk.Token.VALUES_LIST;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

  public static void checkValuesList(Map<String, Object> specification) {
    Map<String, Object> sections = (Map<String, Object>) specification.get(SECTIONS.getText());

    for (Entry<String, Object> sectionEntry : sections.entrySet()) {
      Map<String, Object> section = (Map<String, Object>) sectionEntry.getValue();
      List<Map<String, Object>> prompts =
          (List<Map<String, Object>>) section.get(PROMPTS.getText());

      if (prompts != null) {
        for (Map<String, Object> prompt : prompts) {
          if (prompt.containsKey(VALUES_LIST.getText())) {
            String name = (String) prompt.get(VALUES_LIST.getText());

            Map<String, List<Map<String, Object>>> choicesMap =
                (Map<String, List<Map<String, Object>>>) specification.get(CHOICES.getSheetName());

            Map<String, Map<String, Object>> queriesMap =
                (Map<String, Map<String, Object>>) specification.get(QUERIES.getSheetName());

            if (!(choicesMap.containsKey(name) || queriesMap.containsKey(name))) {
              throw new OdkXlsValidationException("Unrecognized 'values_list' name: '" + name
                  + "'. Prompt: '" + prompt.get(TYPE.getText()) + "' at row "
                  + prompt.get(ROW_NUM_KEY) + " on sheet: " + section.get(SECTION_NAME.getText()));
            }

          }
        }
      }
    }
  }
}

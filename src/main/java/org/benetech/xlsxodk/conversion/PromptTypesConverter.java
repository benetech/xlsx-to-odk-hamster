package org.benetech.xlsxodk.conversion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.benetech.xlsxodk.Constants;
import org.benetech.xlsxodk.util.ConversionUtils;
import org.benetech.xlsxodk.validation.ChoicesValidator;
import org.benetech.xlsxodk.validation.QueriesValidator;

public class PromptTypesConverter {
  public static final String PROMPT_TYPE_NAME_FIELD = "prompt_type_name";

  List<Map<String, Object>> promptTypeRowList;

  Map<String, Object> promptTypeMap;

  public PromptTypesConverter(List<Map<String, Object>> incomingPromptTypeRowList) {
    this.promptTypeRowList = ConversionUtils.omitRowsWithMissingField(incomingPromptTypeRowList, PROMPT_TYPE_NAME_FIELD);
    QueriesValidator.validate(promptTypeRowList);
    promptTypeMap = new  LinkedHashMap<String, Object> ();
    promptTypeMap.putAll(Constants.PROMPT_TYPES);
    Map<String, Map<String, Object>> userDefPromptTypeMap = ConversionUtils.toMap(promptTypeRowList, PROMPT_TYPE_NAME_FIELD);
    promptTypeMap.putAll(userDefPromptTypeMap);
  
  }

  public Map<String, Object> getPromptTypeMap() {
    return promptTypeMap;
  }

}

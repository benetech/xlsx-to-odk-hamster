package org.benetech.xlsxodk.conversion;

import java.util.List;
import java.util.Map;

import org.benetech.xlsxodk.PredefSheet;
import org.benetech.xlsxodk.util.ConversionUtils;
import org.benetech.xlsxodk.validation.ChoicesValidator;

public class ChoicesConverter {
  public static final String CHOICE_LIST_NAME = "choice_list_name";

  List<Map<String, Object>> choicesRowList;

  Map<String, List<Map<String, Object>>> choicesMap;

  public ChoicesConverter(List<Map<String, Object>> incomingChoicesRowList) {
    this.choicesRowList = ConversionUtils.omitRowsWithMissingField(incomingChoicesRowList, CHOICE_LIST_NAME);
    ChoicesValidator.validate(choicesRowList);
    this.choicesMap = ConversionUtils.toMapOfGroups(PredefSheet.CHOICES.getSheetName(), choicesRowList, CHOICE_LIST_NAME);
  }

  public Map<String, List<Map<String, Object>>> getChoicesMap() {
    return choicesMap;
  }

}

package org.benetech.xlsxodk.conversion;

import java.util.List;
import java.util.Map;

import org.benetech.xlsxodk.util.ConversionUtils;
import org.benetech.xlsxodk.validation.CalculatesValidator;

public class CalculatesConverter {
  public static final String CALCULATION_NAME_FIELD = "calculation_name";

  List<Map<String, Object>> calculationsRowList;

  Map<String, Map<String, Object>> getCalculationsMap;

  public CalculatesConverter(List<Map<String, Object>> incomingCalculationsRowList) {
    this.calculationsRowList = ConversionUtils.omitRowsWithMissingField(incomingCalculationsRowList, CALCULATION_NAME_FIELD);
    CalculatesValidator.validate(calculationsRowList);
    this.getCalculationsMap = ConversionUtils.toMap(calculationsRowList, CALCULATION_NAME_FIELD);
  }

  public Map<String, Map<String, Object>> getCalculationsMap() {
    return getCalculationsMap;
  }

}

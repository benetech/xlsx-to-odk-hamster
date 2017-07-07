package org.benetech.xlsxodk.conversion;

import java.util.List;
import java.util.Map;

import org.benetech.xlsxodk.util.ConversionUtils;
import org.benetech.xlsxodk.validation.ChoicesValidator;
import org.benetech.xlsxodk.validation.QueriesValidator;

public class QueriesConverter {
  public static final String QUERY_NAME_FIELD = "query_name";

  List<Map<String, Object>> queriesRowList;

  Map<String, Map<String, Object>> queriesMap;

  public QueriesConverter(List<Map<String, Object>> incomingQueriesRowList) {
    this.queriesRowList = ConversionUtils.omitRowsWithMissingField(incomingQueriesRowList, QUERY_NAME_FIELD);
    QueriesValidator.validate(queriesRowList);
    this.queriesMap = ConversionUtils.toMap(queriesRowList, QUERY_NAME_FIELD);
  }

  public Map<String, Map<String, Object>> getQueriesMap() {
    return queriesMap;
  }

}

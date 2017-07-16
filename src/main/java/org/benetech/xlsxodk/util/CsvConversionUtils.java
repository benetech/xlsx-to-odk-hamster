package org.benetech.xlsxodk.util;

import static org.benetech.xlsxodk.Token.DATA;
import static org.benetech.xlsxodk.Token.ELEMENTNAME;
import static org.benetech.xlsxodk.Token.ELEMENTSET;
import static org.benetech.xlsxodk.Token.ELEMENTTYPE;
import static org.benetech.xlsxodk.Token.ISSESSIONVARIABLE;
import static org.benetech.xlsxodk.Token.LISTCHILDELEMENTKEYS;
import static org.benetech.xlsxodk.Token.TYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CsvConversionUtils {

  private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

  private static Map<String, Object> getNewDefinition(String elementKey, String elementName,
      String elementType, String listChildElem) {
    Map<String, Object> result = new LinkedHashMap<String, Object>();
    result.put("_element_key", elementKey);
    result.put("_element_name", elementName);
    result.put("_element_type", elementType);
    result.put("_list_child_element_keys", listChildElem);
    return result;
  }

  /**
   * Create definition.csv from inverted the formDef.json for XLSXConverter processing
   */
  public static String createDefinitionCsvFromDataTableModel(Map<String, Object> dataTableModel) {
    List<Map<String, Object>> definitions = new ArrayList<Map<String, Object>>();
    Map<String, Object> jsonDefn;

    // and now traverse the dataTableModel making sure all the
    // elementSet: 'data' values have columnDefinitions entries.
    //
    for (String dbColumnName : dataTableModel.keySet()) {
      // the XLSXconverter already handles expanding complex types
      // such as geopoint into their underlying storage representation.
      jsonDefn = (Map<String, Object>) dataTableModel.get(dbColumnName);

      String elementSet = (String) jsonDefn.get(ELEMENTSET.getText());
      Boolean isSessionVariable = (Boolean) jsonDefn.get(ISSESSIONVARIABLE.getText());
      isSessionVariable = isSessionVariable == null ? Boolean.FALSE : isSessionVariable;
      String listChildElem = null;

      if (DATA.getText().equals(jsonDefn.get(ELEMENTSET.getText())) && !isSessionVariable) {
        String surveyElementName = (String) jsonDefn.get(ELEMENTNAME.getText());
        // Make sure that the listChildElementKeys have extra quotes
        // This breaks the RFC4180CsvReader otherwise
        List<String> listChildElementKeys =
            (List<String>) jsonDefn.get(LISTCHILDELEMENTKEYS.getText());
        if (listChildElementKeys != null && listChildElementKeys.size() != 0) {
          listChildElem = doubleQuoteString(gson.toJson(listChildElementKeys));
        }
      }

      listChildElem = listChildElem == null ? gson.toJson(new ArrayList<String>()) : listChildElem;
      String elementName = (String) jsonDefn.get(ELEMENTNAME.getText());
      String elementType = (String) jsonDefn.get(ELEMENTTYPE.getText());
      String type = (String) jsonDefn.get(TYPE.getText());
      elementType = elementType == null ? type : elementType;

      definitions.add(getNewDefinition(dbColumnName, elementName, elementType, listChildElem));
    }

    // Now sort the _column_definitions
    Collections.sort(definitions, new Comparator<Map<String, Object>>() {
      public int compare(Map<String, Object> definition1, Map<String, Object> definition2) {
        String key1 = (String) definition1.get("_element_key");
        String key2 = (String) definition2.get("_element_key");
        return StringUtils.compare(key1, key2);
      }
    });


    // Now write the definitions in CSV format
    StringBuilder defCsv = new StringBuilder()
        .append("_element_key,_element_name,_element_type,_list_child_element_keys\r\n");

    for (Map<String, Object> definition : definitions) {
      defCsv.append(definition.get("_element_key")).append(",");
      defCsv.append(definition.get("_element_name")).append(",");
      defCsv.append(definition.get("_element_type")).append(",");
      defCsv.append(definition.get("_list_child_element_keys")).append("\r\n");
    }
    return defCsv.toString();
  }

  /**
   * Create properties.csv from inverted the formDef.json for XLSXConverter processing
   */
  public static String createPropertiesCsv(List<Map<String, Object>> properties) {

    // Now write the definitions in CSV format
    StringBuilder propCsv = new StringBuilder().append("_partition,_aspect,_key,_type,_value\r\n");

    for (Map<String, Object> property : properties) {
      propCsv.append(property.get("_partition")).append(",");
      propCsv.append(property.get("_aspect")).append(",");
      propCsv.append(property.get("_key")).append(",");
      propCsv.append(property.get("_type")).append(",");
      propCsv.append(doubleQuoteString((String) property.get("_value"))).append("\r\n");
    }
    return propCsv.toString();
  }

  /**
   * Double quote strings if they contain a quote, carriage return, or line feed
   */
  private static String doubleQuoteString(String str) {
    if (str != null) {
      if (str.length() == 0 || str.indexOf("\r") != -1 || str.indexOf("\n") != -1
          || str.indexOf("\"") != -1) {
        str = str.replaceAll("\"", "\"\"");
        str = "\"" + str + "\"";
        return str;
      } else {
        return str;
      }
    }
    return null;
  }
}

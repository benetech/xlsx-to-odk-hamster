package org.benetech.xlsxodk.conversion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class SectionParser {

  public Map<String, List<Map<String, Object>>> parseSections(List<String> sectionNames, Map<String, List<Map<String, Object>>> sections,
      Map<String, Map<String, Object>> settings, Map<String, String> columnTypeMap) {
    
    Map<String, List<Map<String, Object>>> newSections = new LinkedHashMap<String, List<Map<String, Object>>>();
    
    return newSections;
  }
  
  public List<Map<String, Object>> parseSection(String sectionName, List<Map<String, Object>> section,
      Map<String, Map<String, Object>> settings, Map<String, String> columnTypeMap) {
  
  return null;
  }
  
  public void parseControlFlowSection(String sectionName, List<Map<String, Object>> section) {
    for (Map<String, Object> row : section) {
      if (exists(row, "branch_label")) {
        
      }
      if (exists(row, "clause")) {
        
      }
      else if (exists(row, "type")) {
        
      }
    }
  }
 
  
  private boolean exists(Map<String, Object> row, String field) {
    return !StringUtils.isEmpty((String)row.get(field));
  }
}

package org.benetech.xlsxodk;

public enum PredefSheet {
  COLUMN_TYPES("column_types"), 
  SETTINGS("settings"), 
  CHOICES("choices"), 
  QUERIES("queries"), 
  CALCULATES("calculates"), 
  PROMPT_TYPES("prompt_types"), 
  MODEL("model"),
  INITIAL("initial"),
  SURVEY("survey"),
  // SECTION_NAMES
  // SECTIONS
  // dataTableModel
  PROPERTIES("properties")
  ;


  private String sheetName;

  private PredefSheet(String sheetName) {
    this.sheetName = sheetName;
  }

  public String getSheetName() {
    return sheetName;
  }


}

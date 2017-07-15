package org.benetech.xlsxodk;

public enum Token {
  ARRAY("array"),
  ASPECT("aspect"),
  ASSIGN("assign"),
  COLUMN("Column"),
  COMMENT("comment"),
  COMMENTS("comments"),
  DATA("data"),
  DATATABLEMODEL("dataTableModel"),
  DEFAULT("default"),
  DEFAULTVIEWTYPE("defaultViewType"),
  DISPLAYCHOICESLIST("displayChoicesList"),
  DISPLAY("display"),
  DISPLAYFORMAT("displayFormat"),
  DISPLAYNAME("displayName"),
  ELEMENTKEY("elementKey"),
  ELEMENTNAME("elementName"),
  ELEMENTPATH("elementPath"),
  ELEMENTSET("elementSet"),
  ELEMENTTYPE("elementType"),
  FORMTYPE("FormType"),
  FRAMEWORK("framework"),
  ISNOTNULLABLE("isNotNullable"),
  ISSESSIONVARIABLE("isSessionVariable"),
  ITEMS("items"),
  KEY("key"),
  LISTCHILDELEMENTKEYS("listChildElementKeys"),
  MODEL("model"),
  NAME("name"),
  NOTUNITOFRETENTION("notUnitOfRetention"),
  OBJECT("object"),
  OPERATION("operation"),
  PARTITION("partition"),
  PROPERTIES("properties"),
  SECTION("section"),
  SECTIONS("sections"),
  SPREADSHEET("SPREADSHEET"),
  STRING("string"),
  SURVEYUTIL("SurveyUtil"),
  TABLE_ID("table_id"),
  TABLE("Table"),
  TITLE("title"),
  TYPE("type"),
  VALUES_LIST("values_list"),
  VALUESLIST("valuesList"), // Why?  WHY?
  VALUE("value"),
;
  


  private String text;

  private Token(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }
  
  public static Token fromText(String text) {
    return Token.valueOf(text.toUpperCase());
  }
  
  @Override
  public String toString() {
    return text;
  }
}

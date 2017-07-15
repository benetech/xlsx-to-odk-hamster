package org.benetech.xlsxodk;

public enum Token {
  ASSIGN("assign"),
  TYPE("type"),
  NAME("name"),
  PROPERTIES("properties"),
  ITEMS("items"),
  MODEL("model"),
  DISPLAYNAME("displayName"),
  DISPLAY("display"),
  TITLE("title"),
  VALUE("value"),
  VALUES_LIST("values_list"),
  VALUESLIST("valuesList"), // Why?  WHY?
  SECTION("section"),
  SECTIONS("sections"),
  OPERATION("operation"),
  ELEMENTKEY("elementKey"),
  ELEMENTNAME("elementName"),
  ELEMENTPATH("elementPath"),
  ELEMENTSET("elementSet"),
  ELEMENTTYPE("elementType"),
  ISNOTNULLABLE("isNotNullable"),
  LISTCHILDELEMENTKEYS("listChildElementKeys"),
  ISSESSIONVARIABLE("isSessionVariable"),
  NOTUNITOFRETENTION("notUnitOfRetention"),
  OBJECT("object"),
  ARRAY("array"),
  STRING("string"),
  COMMENT("comment"),
  COMMENTS("comments"),
  TABLE_ID("table_id"),
  DATATABLEMODEL("dataTableModel"),
  FRAMEWORK("framework"),
  PARTITION("partition"),
  KEY("key"),
  ASPECT("aspect"),
  COLUMN("Column"),
  TABLE("Table"),
  FORMTYPE("FormType"),
  SURVEYUTIL("SurveyUtil"),
  DEFAULT("default"),
  DISPLAYFORMAT("displayFormat"),
  DISPLAYCHOICESLIST("displayChoicesList"),
  DEFAULTVIEWTYPE("defaultViewType"),
  DATA("data"),
  SPREADSHEET("SPREADSHEET")


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

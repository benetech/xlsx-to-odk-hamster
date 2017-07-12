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
  VALUES_LIST("values_list"),
  SECTION("section"),
  OPERATION("operation"),
  ELEMENTKEY("elementKey"),
  OBJECT("object"),
  ARRAY("array");
  


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

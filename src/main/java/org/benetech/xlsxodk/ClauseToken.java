package org.benetech.xlsxodk;

public enum ClauseToken {
BEGIN("begin"),
END("end"),
IF("if"),
ELSE("else"),
GOTO("goto"),
BACK("back"),
RESUME("resume"),
DO("do"),
EXIT("exit"),
VALIDATE("validate"),
SAVE("save"),
COMMENT("comment"),
  ;


  private String text;

  private ClauseToken(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }
  
  public static ClauseToken fromText(String text) {
    return ClauseToken.valueOf(text.toUpperCase());
  }
  
  @Override
  public String toString() {
    return text;
  }
}

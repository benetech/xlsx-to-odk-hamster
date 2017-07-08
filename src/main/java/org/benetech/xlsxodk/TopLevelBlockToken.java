package org.benetech.xlsxodk;

public enum TopLevelBlockToken {
  BRANCH_LABEL("branch_label"),
  ASSIGN("assign"),
  PROMPT("prompt"),
  GOTO_LABEL("goto_label"),
  RESUME("resume"),
  BACK_IN_HISTORY("back_in_history"),
  DO_SECTION("do_section"),
  EXIT_SECTION("exit_section"),
  VALIDATE("validate"),
  SAVE_AND_TERMINATE("save_and_terminate"),
  BEGIN_SCREEN("begin_screen"),
  BEGIN_IF("begin_if"),
  ELSE("else"),
  END_IF("end_if"), 
  END_SCREEN("end_screen"), 
  RENDER("render")
  ;

  private String text;

  private TopLevelBlockToken(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public static TopLevelBlockToken fromText(String text) {
    return TopLevelBlockToken.valueOf(text.toUpperCase());
  }
}

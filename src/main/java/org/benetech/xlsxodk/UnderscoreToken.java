package org.benetech.xlsxodk;



public enum UnderscoreToken {
  _BRANCH_LABEL("_branch_label"),
  _BRANCH_LABEL_ENCLOSING_SCREEN("_branch_label_enclosing_screen"),
  _CONTENTS("_contents"),
  _DATA_TYPE("_data_type"),
  _DEFN("_defn"),
  _DO_SECTION_NAME("_do_section_name"),
  _ELSE_BLOCK("_else_block"),
  _ELSE_CLAUSE("_else_clause"),
  _END_IF_CLAUSE("_end_if_clause"),
  _END_SCREEN_CLAUSE("_end_screen_clause"),
  _FINALIZE("_finalize"),
  _SCREEN_BLOCK("_screen_block"),
  _SECTION("_section"),
  _SWEEP_NAME("_sweep_name"),
  _TAG_NAME("_tag_name"),
  _THEN_BLOCK("_then_block"),
  _TOKEN_TYPE("_token_type"),
  _TYPE("_type"),

  ;


  private String text;

  private UnderscoreToken(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }
  
  public static UnderscoreToken fromText(String text) {
    return UnderscoreToken.valueOf(text.toUpperCase());
  }
  
  @Override
  public String toString() {
    return text;
  }
}
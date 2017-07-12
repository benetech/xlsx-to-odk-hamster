package org.benetech.xlsxodk;

public enum SectionToken {
  SECTION_NAME("section_name"),
  NESTED_SECTIONS("nested_sections"),
  REACHABLE_SECTIONS("reachable_sections"),
  PROMPTS("prompts"),
  VALIDATION_TAG_MAP("validation_tag_map"),
  OPERATIONS("operations"),
  BRANCH_LABEL_MAP("branch_label_map"),

  ;


  private String text;

  private SectionToken(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }
  
  public static SectionToken fromText(String text) {
    return SectionToken.valueOf(text.toUpperCase());
  }

  @Override
  public String toString() {
    return text;
  }
  
}

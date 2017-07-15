package org.benetech.xlsxodk;

public enum ColumnToken {
  _ID("_id"),
  _ROW_ETAG("_row_etag"),
  _SYNC_STATE("_sync_state"),
  _CONFLICT_TYPE("_conflict_type"),
  _FILTER_TYPE("_filter_type"),
  _FILTER_VALUE("_filter_value"),
  _FORM_ID("_form_id"),
  _LOCALE("_locale"),
  _SAVEPOINT_TYPE("_savepoint_type"),
  _SAVEPOINT_TIMESTAMP("_savepoint_timestamp"),
  _SAVEPOINT_CREATOR("_savepoint_creator"),
  ;


  private String text;

  private ColumnToken(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }
  
  public static ColumnToken fromText(String text) {
    return ColumnToken.valueOf(text.toUpperCase());
  }
  
  @Override
  public String toString() {
    return text;
  }
}

package org.benetech.xlsxodk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class Constants {

  private static Log logger = LogFactory.getLog(Constants.class);

  public static final String[] RESERVED_FIELD_NAME_LIST = new String[] {
      /**
       * ODK processor reserved names
       *
       * For template substitution, we reserve 'calculates' so that the {{#substitute}} directive
       * can substitute values and calculates into a display string.
       */
      PredefSheet.CALCULATES.getSheetName(),
      /**
       * ODK Metadata reserved names
       */
      "row_etag", "sync_state", "conflict_type", "savepoint_timestamp", "savepoint_creator",
      "savepoint_type", "form_id", "locale",

      /**
       * SQLite keywords ( http://www.sqlite.org/lang_keywords.html )
       */
      "abort", "action", "add", "after", "all", "alter", "analyze", "and", "as", "asc", "attach",
      "autoincrement", "before", "begin", "between", "by", "cascade", "case", "cast", "check",
      "collate", "column", "commit", "conflict", "constraint", "create", "cross", "current_date",
      "current_time", "current_timestamp", "database", "default", "deferrable", "deferred",
      "delete", "desc", "detach", "distinct", "drop", "each", "else", "end", "escape", "except",
      "exclusive", "exists", "explain", "fail", "for", "foreign", "from", "full", "glob", "group",
      "having", "if", "ignore", "immediate", "in", "index", "indexed", "initially", "inner",
      "insert", "instead", "intersect", "into", "is", "isnull", "join", "key", "left", "like",
      "limit", "match", "natural", "no", "not", "notnull", "null", "of", "offset", "on", "or",
      "order", "outer", "plan", "pragma", "primary", "query", "raise", "references", "regexp",
      "reindex", "release", "rename", "replace", "restrict", "right", "rollback", "row",
      "savepoint", "select", "set", "table", "temp", "temporary", "then", "to", "transaction",
      "trigger", "union", "unique", "update", "using", "vacuum", "values", "view", "virtual",
      "when", "where"};
  public static final Set<String> RESERVED_FIELD_NAMES =
      new HashSet<String>(Arrays.asList(RESERVED_FIELD_NAME_LIST));

  public static final String[] RESERVED_SHEET_NAME_LIST =
      new String[] {PredefSheet.SETTINGS.getSheetName(), "properties",
          PredefSheet.CHOICES.getSheetName(), PredefSheet.QUERIES.getSheetName(),
          PredefSheet.CALCULATES.getSheetName(), "column_types", "prompt_types", "model"};

  public static final Set<String> RESERVED_SHEET_NAMES =
      new HashSet<String>(Arrays.asList(RESERVED_SHEET_NAME_LIST));

  public static final Map<String, String> COLUMN_TYPE_MAP = initializeColumnTypeMap();

  private static Map<String, String> initializeColumnTypeMap() {
    Map<String, String> result = new LinkedHashMap<String, String>();
    result.put("_screen_block", "function");
    result.put("condition", "formula");
    result.put("constraint", "formula");
    result.put("required", "formula");
    result.put("calculation", "formula"); // ");assign"); prompt and on calculates sheet.
    result.put("auxillaryHash", "formula"); // TODO: move to queries
    result.put("selectionArgs", "formula"); // TODO: move to queries
    result.put("url", "formula"); // external_link prompt
    result.put("uri", "formula"); // queries
    result.put("callback", "formula(context)"); // queries
    result.put("choice_filter", "formula(choice_item)"); // expects "choice" context arg.
    result.put("templatePath", "requirejs_path");
    result.put("image", "app_path_localized");
    result.put("audio", "app_path_localized");
    result.put("video", "app_path_localized");
    return result;
  }


  static Map<String, Object> initializePromptTypeMap() throws JsonIOException, JsonSyntaxException {
    URL url = Constants.class.getResource("/json_fragments/prompt_type_map.json");
    File promptTypes = new File(url.getFile());
    Gson gson = new Gson();
    Type stringObjectMap = new TypeToken<Map<String, Object>>() {}.getType();
    Map<String, Object> map = null;
    try {
      map = gson.fromJson(new FileReader(promptTypes), stringObjectMap);
    } catch (FileNotFoundException e) {
      logger.error("Unable to initialize prompt type map", e);
    }
    return map;
  }

  public static final Map<String, Object> PROMPT_TYPES = initializePromptTypeMap();

  static List<Map<String, Object>> initializeDefaultInitial()
      throws JsonIOException, JsonSyntaxException {
    URL url = Constants.class.getResource("/json_fragments/default_initial.json");
    File promptTypes = new File(url.getFile());
    Gson gson = new Gson();
    Type stringObjectMap = new TypeToken<List<DefaultInitialItem>>() {}.getType();
    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    List<DefaultInitialItem> tempList;
    try {
      tempList = gson.fromJson(new FileReader(promptTypes), stringObjectMap);
      for (DefaultInitialItem item : tempList) {
        list.add(convertDefaultInitialItem(item));
      }
    } catch (FileNotFoundException e) {
      logger.error("Unable to initialize default initial list", e);
    }
    return list;
  }

  // Abusing Gson to force number to integer conversion instead of double.
  class DefaultInitialItem {
    public Integer _row_num;
    public String type;
    public Object display;
    public String clause;
    public String condition;
  }

  private static Map<String, Object> convertDefaultInitialItem(DefaultInitialItem item) {
    Map<String, Object> result = new LinkedHashMap<String, Object>();
    if (item._row_num != null) {
      result.put("_row_num", item._row_num);
    }
    if (item.type != null) {
      result.put("type", item.type);
    }
    if (item.display != null) {
      result.put("display", item.display);
    }
    if (item.clause != null) {
      result.put("clause", item.clause);
    }
    if (item.condition != null) {
      result.put("condition", item.condition);
    }
    return result;
  }

  public static final List<Map<String, Object>> DEFAULT_INITIAL = initializeDefaultInitial();

}

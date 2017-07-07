package org.benetech.xlsxodk.conversion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.benetech.xlsxjson.Xlsx2JsonConverter;
import org.benetech.xlsxodk.util.ConversionUtils;
import org.benetech.xlsxodk.validation.ModelValidator;

public class ModelConverter {
  public static final String MODEL_NAME_FIELD = "name";

  List<Map<String, Object>> modelRowList;

  Map<String, Map<String, Object>> modelMap;

  public ModelConverter(List<Map<String, Object>> incomingModelRowList) {
    this.modelRowList = ConversionUtils.omitRowsWithMissingField(incomingModelRowList, MODEL_NAME_FIELD);
    ModelValidator.validate(modelRowList);
    this.modelMap = ConversionUtils.toMap(modelRowList, MODEL_NAME_FIELD);
    updateModelFields(this.modelMap);
  }

  public Map<String, Map<String, Object>> getModelMap() {
    return modelMap;
  }
  
  public static void updateModelFields(Map<String, Map<String, Object>> definitions) {
    for (String key : definitions.keySet()) {
      addDefnFieldToDefinition(definitions.get(key));
      removeIgnorableModelFieldsFromDefinition(definitions.get(key));
    }
  }
  
  public static void addDefnFieldToDefinition(Map<String, Object> definition) {
    Map<String, Object> defn = new LinkedHashMap<String, Object>();
    defn.put(Xlsx2JsonConverter.ROW_NUM_KEY, definition.get(Xlsx2JsonConverter.ROW_NUM_KEY));
    defn.put("section_name", "model");
    definition.put("_defn", defn);
  }

  public static void removeIgnorableModelFieldsFromDefinition(Map<String, Object> definition) {
    if (definition != null) {
      definition.remove(Xlsx2JsonConverter.ROW_NUM_KEY);
      definition.remove("comments");
      definition.remove("name");
      definition.remove("__rowNum__");
    }
  }

}

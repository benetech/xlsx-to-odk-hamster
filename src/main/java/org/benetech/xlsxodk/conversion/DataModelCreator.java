package org.benetech.xlsxodk.conversion;

import static org.benetech.xlsxodk.Token.*;
import static org.benetech.xlsxodk.SectionToken.*;
import static org.benetech.xlsxodk.UnderscoreToken.*;
import static org.benetech.xlsxjson.Xlsx2JsonConverter.ROW_NUM_KEY;

import static org.benetech.xlsxodk.PredefSheet.MODEL;
import org.unitils.reflectionassert.ReflectionComparatorFactory;
import org.unitils.reflectionassert.ReflectionComparatorMode;
import org.unitils.reflectionassert.ReflectionComparator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.benetech.xlsxodk.Constants;
import org.benetech.xlsxodk.Token;
import org.benetech.xlsxodk.exception.OdkXlsValidationException;
import org.benetech.xlsxodk.util.ValidationUtils;
import org.benetech.xlsxodk.util._Dumper;

import com.rits.cloning.Cloner;

public class DataModelCreator {



  Map<String, Object> specification;

  Map<String, Object> promptTypes;

  Map<String, Map<String, Object>> modelSection;


  public DataModelCreator(Map<String, Object> specification, Map<String, Object> promptTypes) {
    this.specification = specification;
    this.promptTypes = promptTypes;
    this.modelSection = (Map<String, Map<String, Object>>) specification.get(MODEL.getSheetName());
  }

  public void create() {
    for (String name : modelSection.keySet()) {
      Map<String, Object> defn = modelSection.get(name);
      if (defn.containsKey(TYPE.getText())) {
        // blend in the default definition for this prompt type
        String type = (String) defn.get(TYPE.getText());
        if (promptTypes.containsKey(type)) {
          // Original ODK allows you to have a key = null entry, we need to preserve this.
          // TODO: replace the null with something else, this is confusing for debugging.
          Object schema = promptTypes.get(type);
          if (schema != null) {
            // deep copy of schema because we are going to add properties recursively

            schema = deepCopyObject(schema);
            // override the promptTypes 'type' with info supplied by the prompt_type sheet.

            defn.remove(TYPE.getText());
            modelSection.put(name, (Map<String, Object>) deepExtendObject(schema, defn));

          }
        }
      }
      recursivelyResolveTypeInDataModel(modelSection.get(name), promptTypes);
    }


    Map<String, Object> model = new LinkedHashMap<String, Object>();
    List<Map<String, Object>> assigns = new ArrayList<Map<String, Object>>();


    Map<String, Object> sections = (Map<String, Object>) specification.get("sections");

    for (Entry<String, Object> sectionEntry : sections.entrySet()) {
      String sectionName = sectionEntry.getKey();
      Map<String, Object> section = (Map<String, Object>) sectionEntry.getValue();
      List<Map<String, Object>> prompts =
          (List<Map<String, Object>>) section.get(PROMPTS.getText());

      for (Map<String, Object> prompt : prompts) {
        Map<String, Object> schema;
        Object promptType = prompt.get(_TYPE.getText());
        if (promptTypes.containsKey(promptType)) {

          if (promptTypes.get(promptType) == null) {
          } else if (promptTypes.get(promptType) != null
              && promptTypes.get(promptType) instanceof Map) {
            schema = (Map<String, Object>) promptTypes.get(_TYPE.getText());
            if (prompt.containsKey(NAME.getText())) {
              String name = (String) prompt.get(NAME.getText());
              try {
                assertValidElementKey(name, name, null);
              } catch (Exception e) {
                throw new OdkXlsValidationException(e.getMessage() + " Prompt: '"
                    + prompt.get(TYPE.getText()) + "' at row " + prompt.get(ROW_NUM_KEY)
                    + " on sheet: " + section.get(SECTION_NAME.getText()));
              }
              
              // deep copy of schema because we are going to add properties recursively
              schema = (Map<String, Object>) deepCopyObject(schema);
              recursivelyResolveTypeInDataModel(schema, promptTypes);
              updateModel(section, prompt, model, schema);
            } else {
              if ( !(promptTypes.get(promptType) instanceof Map)){
                // schema === undefined
                throw new OdkXlsValidationException("Unrecognized prompt type. Prompt: '"
                    + prompt.get(TYPE.getText()) + "' at row " + prompt.get(ROW_NUM_KEY)
                    + " on sheet: " + section.get(SECTION_NAME.getText()));
              }
              throw new OdkXlsValidationException(
                  "Expected 'name' but none defined for prompt. Prompt: '"
                      + prompt.get(TYPE.getText()) + "' at row " + prompt.get(ROW_NUM_KEY)
                      + " on sheet: " + section.get(SECTION_NAME.getText()));
            }


          }
        } else {
          throw new OdkXlsValidationException(
              "Unrecognized prompt type. Prompt: '" + prompt.get(TYPE.getText()) + "' at row "
                  + prompt.get(ROW_NUM_KEY) + " on sheet: " + section.get(SECTION_NAME.getText()));
        }
      }

      _Dumper._dump_section("model_2164", model);

      /*
       * Process the 'assign' statements. Two flavors of these statements: assign | name |
       * calculation -- no prompt_type info assign integer | name | calculation -- prompt_type info
       * supplied in 'type' column.
       *
       * The initial parsing has _token_type = "assign" _data_type = null or "integer",
       * respectively, for above.
       */
      List<Map<String, Object>> operations =
          (List<Map<String, Object>>) section.get(OPERATIONS.getText());
      for (Map<String, Object> operation : operations) {
        if (ASSIGN.getText().equals(operation.get(_TOKEN_TYPE.getText()))) {
          if (operation.containsKey(NAME.getText())) {
            String name = (String) operation.get(NAME.getText());
            try {
              assertValidElementKey(name, name, null);
            } catch (OdkXlsValidationException e) {
              throw new OdkXlsValidationException(e.getMessage() + " Assign clause: '"
                  + operation.get(TYPE.getText()) + "' at row " + operation.get(ROW_NUM_KEY)
                  + " on sheet: " + section.get(SECTION_NAME.getText()));
            }
            if (operation.get(_DATA_TYPE.getText()) == null) {
              // no explicit type -- hope that the field gets a value somewhere else...
              // record name to verify that is the case.
              Map<String, Object> newSchema = new LinkedHashMap<String, Object>();
              updateModel(section, operation, model, newSchema);
              Map<String, Object> assign = new LinkedHashMap<String, Object>();
              assign.put(OPERATION.getText(), operation);
              assign.put(SECTION.getText(), section);
              assigns.add(assign);
            } else if (promptTypes.containsKey(operation.get(_DATA_TYPE.getText()))) {
              Object schema = promptTypes.get(operation.get(_DATA_TYPE.getText()));
              if (schema != null && schema instanceof Map) {
                // deep copy of schema because we are going to add properties recursively

                schema = deepCopyObject(schema);
                recursivelyResolveTypeInDataModel(schema, promptTypes);
                updateModel(section, operation, model, (Map<String, Object>) schema);

              } else if (schema != null) {
                // schema === undefined
                throw new OdkXlsValidationException("Unrecognized assign type: "
                    + operation.get(_DATA_TYPE.getText()) + ". Clause: '"
                    + operation.get(TYPE.getText()) + "' at row " + operation.get(ROW_NUM_KEY)
                    + " on sheet: " + section.get(SECTION_NAME.getText()));
              } else {
                throw new OdkXlsValidationException(
                    "Invalid assign type: " + operation.get(_DATA_TYPE.getText()) + ". Clause: '"
                        + operation.get(TYPE.getText()) + "' at row " + operation.get(ROW_NUM_KEY)
                        + " on sheet: " + section.get(SECTION_NAME.getText()));
              }
            }
          } else {
            throw new OdkXlsValidationException(
                "Expected 'name' but none defined for assign. Assign: '"
                    + operation.get(_DATA_TYPE.getText()) + ". Clause: '" + "' at row "
                    + operation.get(ROW_NUM_KEY) + " on sheet: "
                    + section.get(SECTION_NAME.getText()));
          }
        }
      }
    }

  _Dumper._dump_section("model_2217",model);

  // The model tab takes precedence over whatever was defined at the prompt layer.

  // Look for fields defined by the model tab that are also defined by prompts. If the
  // prompts declare a different data type, then warn the user. Regardless, treat the
  // model tab as the authority, overriding any inconsistency from the prompt definitions.
  // NOTE: Warnings can be suppressed by defining model.xxx overrides on each prompt.
  for(

  Entry<String, Map<String, Object>> modelEntry:modelSection.entrySet())
  {
    String name = modelEntry.getKey();
    Map<String, Object> mdef = modelEntry.getValue();
    if (model.containsKey(name)) {
      Map<String, Object> defn = (Map<String, Object>) model.get(name);
      Map<String, Object> blankMapA = new LinkedHashMap<String, Object>();
      Map<String, Object> blankMapB = new LinkedHashMap<String, Object>();

      Map<String, Object> amodb =
          (Map<String, Object>) deepExtendObject(deepExtendObject(blankMapA, defn), mdef);
      Map<String, Object> bmoda =
          (Map<String, Object>) deepExtendObject(deepExtendObject(blankMapB, mdef), defn);
      ModelConverter.removeIgnorableModelFieldsFromDefinition(amodb);
      ModelConverter.removeIgnorableModelFieldsFromDefinition(bmoda);
      amodb.remove(_DEFN.getText());
      bmoda.remove(_DEFN.getText());
      ReflectionComparatorFactory reflectionComparatorFactory = new ReflectionComparatorFactory();
      ReflectionComparator comp = reflectionComparatorFactory.createRefectionComparator(
          ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);

      if (!comp.isEqual(amodb, bmoda)) {
        // the model and prompts field definitions are incompatible.
        // Issue a warning to the user...
        StringBuilder formatted = new StringBuilder();
        List<Map<String, Object>> _defn = (List<Map<String, Object>>) defn.get(_DEFN.getText());
        if (_defn != null) {
          for (Map<String, Object> def : _defn) {
            formatted.append(", at row: ").append(def.get(ROW_NUM_KEY)).append(" on sheet: ")
                .append(def.get(SECTION_NAME.getText()));
          }

          Map<String, Object> defn0 =
              ((List<Map<String, Object>>) (mdef.get(_DEFN.getText()))).get(0);

          // TODO: warnings.warn
          throw new OdkXlsValidationException(
              name + " has different definitions at row " + defn0.get(ROW_NUM_KEY) + " on "
                  + defn0.get(SECTION_NAME.getText()) + " sheet than " + formatted.substring(2)
                  + " " + defn0.get(SECTION_NAME.getText()) + " sheet takes precedence.");
        }
      }
      // merge the definition from the survey with the model
      // such that the model takes precedence.
      List<Map<String, Object>> _defn = (List<Map<String, Object>>) defn.get(_DEFN.getText());
      Map<String, Object> defn0 = ((List<Map<String, Object>>) (mdef.get(_DEFN.getText()))).get(0);
      _defn.add(defn0);
      List<Map<String, Object>> dt = _defn;
      defn.remove(_DEFN.getText());
      deepExtendObject(defn, mdef);
      defn.put(_DEFN.getText(), dt);
      modelSection.put(name, defn);
    }
  }

  _Dumper._dump_section("model_2262",model);


  // copy over the fields in the prompt model that were not defined in the model tab...
  for(
  String name:model.keySet())
  {
    if (!modelSection.containsKey(name)) {
      modelSection.put(name, (Map<String, Object>) model.get(name));
    }
  }

  for(
  int i = 0;i<assigns.size();i++)
  {
    Map<String, Object> section = (Map<String, Object>) (assigns.get(i).get(SECTION.getText()));

    Map<String, Object> operation = (Map<String, Object>) (assigns.get(i).get(OPERATION.getText()));

    if (!model.containsKey(operation.get(NAME.getText()))) {
      throw new OdkXlsValidationException("Field name '" + operation.get(NAME.getText())
          + "' does not have a defined storage type. Clause: '" + operation.get(TYPE.getText())
          + "' at row " + operation.get(ROW_NUM_KEY) + " on sheet: "
          + section.get(SECTION_NAME.getText())
          + ". Declare the type on the model sheet or in a model.type column.");

    }
  }
  // ensure that all the model entries are not reserved words or too long

  Map<String, String> fullSetOfElementKeys = new LinkedHashMap<String, String>();for(
  String name:modelSection.keySet())
  {
    Map<String, Object> entry = modelSection.get(name);
    if (entry.containsKey(ELEMENTKEY.getText())
        && !NAME.getText().equals(entry.get(ELEMENTKEY.getText()))) {
      throw new OdkXlsValidationException(
          "elementKey is user-defined for " + name + " but does not match computed key: " + name);
    }
    assertValidElementKey(name, name, fullSetOfElementKeys);
    entry.put(ELEMENTKEY.getText(), name);
    if (OBJECT.getText().equals(entry.get(TYPE.getText()))) {
      recursiveAssignPropertiesElementKey(name, name, fullSetOfElementKeys,
          (Map<String, Object>) (entry.get(PROPERTIES.getText())));
    } else if (ARRAY.getText().equals(entry.get(TYPE.getText()))) {
      assignItemsElementKey(name, name, fullSetOfElementKeys, entry);

    }
  }



  }


  /**
   * Helper function for constructing and assigning the elementKey in the model
   */
  public static void recursiveAssignPropertiesElementKey(String elementPathPrefix,
      String elementKeyPrefix, Map<String, String> fullSetOfElementKeys,
      Map<String, Object> properties) {
    if (properties == null) {
      return;
    }
    for (String name : properties.keySet()) {
      Map<String, Object> entry = (Map<String, Object>) properties.get(name);
      String elementPath = elementPathPrefix + "." + name;
      String key = elementKeyPrefix + "_" + name;
      if (entry.containsKey(ELEMENTKEY.getText()) && !entry.get(ELEMENTKEY.getText()).equals(key)) {
        throw new OdkXlsValidationException("elementKey is user-defined for " + elementPath
            + " but does not match computed key: " + key);
      }
      assertValidElementKey(elementPath, key, fullSetOfElementKeys);
      entry.put(ELEMENTKEY.getText(), key);
      if (OBJECT.getText().equals(entry.get(TYPE.getText()))) {
        recursiveAssignPropertiesElementKey(elementPath, key, fullSetOfElementKeys,
            (Map<String, Object>) entry.get(PROPERTIES.getText()));

      } else if (ARRAY.getText().equals(entry.get(TYPE.getText()))) {
        assignItemsElementKey(elementPath, key, fullSetOfElementKeys, entry);

      }
    }

  }


  public static void assignItemsElementKey(String elementPathPrefix, String elementKeyPrefix,
      Map<String, String> fullSetOfElementKeys, Map<String, Object> enclosingElement) {

    if (!enclosingElement.containsKey(ITEMS.getText())) {
      Map<String, Object> items = new LinkedHashMap<String, Object>();
      items.put(TYPE.getText(), OBJECT.getText());
      enclosingElement.put(ITEMS.getText(), items);
    }
    String elementPath = elementPathPrefix + ".items";
    String key = elementKeyPrefix + "_items";

    Map<String, Object> items = (Map<String, Object>) enclosingElement.get(ITEMS.getText());

    if (items.containsKey(ELEMENTKEY.getText()) && !items.get(ELEMENTKEY.getText()).equals(key)) {
      throw new OdkXlsValidationException("elementKey is user-defined for '" + elementPath
          + "' (the array-element type declaration) but does not match computed key: " + key);
    }

    assertValidElementKey(elementPath, key, fullSetOfElementKeys);
    items.put(ELEMENTKEY.getText(), key);
  };


  /**
   * Helper function to ensure that all fieldNames are short enough once all recursively expanded
   * elementPaths are considered.
   */
  public static void assertValidElementKey(String elementPath, String elementKey,
      Map<String, String> fullSetOfElementKeys) {
    boolean isSimple = false;
    if (elementPath == null) {
      throw new OdkXlsValidationException("Field is not defined.");
    } else if (elementPath.indexOf('.') == -1) {
      // simple name
      isSimple = true;
      ValidationUtils.assertValidUserDefinedName("Field '" + elementPath + "' ", elementKey);
    } else {
      try {
        ValidationUtils.assertValidUserDefinedName("Fully qualified element path ", elementKey);
      } catch (OdkXlsValidationException e) {
        String name = elementPath.substring(0, elementPath.indexOf('.'));
        throw new OdkXlsValidationException(e.getMessage() + " Full elementPath: " + elementPath
            + " Consider shortening the field name '" + name + "'.");
      }
    }

    if (fullSetOfElementKeys != null) {
      if (fullSetOfElementKeys.containsKey(elementKey)) {
        String path = fullSetOfElementKeys.get(elementKey);

        if (isSimple) {
          String name = path.indexOf('.') == -1 ? path : path.substring(0, path.indexOf('.'));
          throw new OdkXlsValidationException("The framework's identifier for field '" + elementPath
              + "' collides with the identifier for the full elementPath: '" + path
              + "'. Please change one of the field names ('" + elementPath + "' or '" + name
              + "').");
        } else {
          String refname = path.indexOf('.') == -1 ? path : path.substring(0, path.indexOf('.'));

          String name = elementPath.substring(0, elementPath.indexOf('.'));
          throw new OdkXlsValidationException(
              "The framework's identifier for the full elementPath: '" + elementPath
                  + "' collides with the identifier for the full elementPath: '" + path
                  + "'. Please change one of the field names ('" + name + "' or '" + refname
                  + "').");
        }
      } else {
        fullSetOfElementKeys.put(elementKey, elementPath);
      }
    }

  }



  /**
   * Merge the definition of a field datatype across multiple prompts. The merge should be
   * order-independent. i.e., each prompt should be referring to the same storage type. They may
   * each add additional fields, but should not add fields that are not identical.
   *
   */
  void updateModel(Map<String, Object> section, Map<String, Object> promptOrAction,
      Map<String, Object> model, Map<String, Object> schema) {
    Map<String, Object> mdef = new LinkedHashMap<String, Object>();
    if (promptOrAction.containsKey(Token.MODEL.getText())) {
      // merge the model fields into the model...
      // these override the schema values
      mdef = (Map<String, Object>) promptOrAction.get(Token.MODEL.getText());
    }

    String name = (String) promptOrAction.get(NAME.getText());

    // if a displayName has not already been defined and if
    // display.title is present, then it becomes the displayName for the model.
    // otherwise, use the element name.
    if (!(mdef.containsKey(Token.DISPLAYNAME.getText()))) {
      if (promptOrAction.containsKey(DISPLAY.getText())) {
        Map<String, Object> displayMap =
            (Map<String, Object>) promptOrAction.get(DISPLAY.getText());
        if (displayMap.containsKey(TITLE.getText())) {
          mdef.put(DISPLAYNAME.getText(), displayMap.get(TITLE.getText()));
        }
      }
    }

    // if the prompt has a value_list, propagate that into the model
    // for entry as metadata for the displayChoices value in the KVS.
    //
    if (promptOrAction.containsKey(VALUES_LIST.getText())) {
      mdef.put(VALUES_LIST.getText(), promptOrAction.get(VALUES_LIST.getText()));
    }
    Map<String, Object> defn = null;

    if (model.containsKey(name)) {

      defn = (Map<String, Object>) model.get(name);
      Map<String, Object> blankMapA = new LinkedHashMap<String, Object>();
      Map<String, Object> blankMapB = new LinkedHashMap<String, Object>();

      Map<String, Object> amodb = (Map<String, Object>) deepExtendObject(
          deepExtendObject(deepExtendObject(blankMapA, defn), schema), mdef);
      Map<String, Object> bmoda = (Map<String, Object>) deepExtendObject(
          deepExtendObject(deepExtendObject(blankMapB, schema), mdef), defn);

      ModelConverter.removeIgnorableModelFieldsFromDefinition(amodb);
      ModelConverter.removeIgnorableModelFieldsFromDefinition(bmoda);
      amodb.remove(_DEFN.getText());
      bmoda.remove(_DEFN.getText());
      ReflectionComparatorFactory reflectionComparatorFactory = new ReflectionComparatorFactory();
      ReflectionComparator comp = reflectionComparatorFactory.createRefectionComparator(
          ReflectionComparatorMode.LENIENT_DATES, ReflectionComparatorMode.LENIENT_ORDER);

      if (!comp.isEqual(amodb, bmoda)) {
        StringBuilder formatted = new StringBuilder();
        List<Map<String, Object>> _defn = (List<Map<String, Object>>) defn.get(_DEFN.getText());
        if (_defn != null) {
          for (Map<String, Object> def : _defn) {
            formatted.append(", at row: ").append(def.get(ROW_NUM_KEY)).append(" on sheet: ")
                .append(def.get(SECTION_NAME.getText()));
          }

          throw new OdkXlsValidationException(promptOrAction.get(_TOKEN_TYPE.getText())
              + " 'name' is defined more than once with different data types. Clause: '"
              + promptOrAction.get(TYPE.getText()) + "' at row " + promptOrAction.get(ROW_NUM_KEY)
              + " on sheet: " + section.get(SECTION_NAME.getText()) + " and "
              + formatted.substring(2));
        }
      }
      Map<String, Object> newMap = new LinkedHashMap<String, Object>();
      newMap.put(ROW_NUM_KEY, promptOrAction.get(ROW_NUM_KEY));
      newMap.put(SECTION_NAME.getText(), section.get(SECTION_NAME.getText()));
      ((List<Map<String, Object>>) defn.get(_DEFN.getText())).add(newMap);

      List<Map<String, Object>> dt = (List<Map<String, Object>>) defn.get(_DEFN.getText());
      defn.remove(_DEFN.getText());

      deepExtendObject(deepExtendObject(defn, schema), mdef);
      defn.put(_DEFN.getText(), dt);



    } else {
      Map<String, Object> newMap = new LinkedHashMap<String, Object>();
      newMap.put(ROW_NUM_KEY, promptOrAction.get(ROW_NUM_KEY));
      newMap.put(SECTION_NAME.getText(), section.get(SECTION_NAME.getText()));
      List<Map<String, Object>> _defn = new ArrayList<Map<String, Object>>();
      _defn.add(newMap);
      Map<String, Object> newDefn = new LinkedHashMap<String, Object>();
      newDefn.put(_DEFN.getText(), _defn);

      Object newModel = deepExtendObject(deepExtendObject(newDefn, schema), mdef);
      model.put(NAME.getText(), newModel);
      defn = (Map<String, Object>) model.get(NAME.getText());
    }
    ModelConverter.removeIgnorableModelFieldsFromDefinition(defn);
  }

  /**
   * Descend into 'term' expanding the 'type' field with the promptType storage definition found in
   * the 'prompt_types' sheet; override that definition with whatever is provided in the model sheet
   * (excluding the original 'type' field).
   */
  void recursivelyResolveTypeInDataModel(Object term, Map<String, Object> promptTypes) {
    if (term != null && term instanceof Map) {
      Map<String, Object> termMap = (Map<String, Object>) term;

      if (termMap.containsKey(PROPERTIES.getText())) {
        Map<String, Object> properties = (Map<String, Object>) termMap.get(PROPERTIES.getText());
        for (Entry<String, Object> propertyEntry : properties.entrySet()) {

          Map<String, Object> defn = (Map<String, Object>) propertyEntry.getValue();
          if (defn.get(TYPE.getText()) != null) {
            // blend in the default definition for this prompt type

            String type = (String) defn.get(TYPE.getText());
            if (promptTypes.containsKey(type)) {
              Object schema = promptTypes.get(type);
              if (schema != null) {
                // deep copy of schema because we are going to add properties recursively
                schema = deepCopyObject(schema);
                // override the promptTypes 'type' with info supplied by the prompt_type sheet.
                defn.remove(TYPE.getText());
                properties.put(propertyEntry.getKey(), deepExtendObject(schema, defn));
              }
            }
          }
          recursivelyResolveTypeInDataModel(propertyEntry.getValue(), promptTypes);
        }
      }
      if (termMap.containsKey(ITEMS.getText())) {
        Map<String, Object> defn = (Map<String, Object>) termMap.get(ITEMS.getText());
        if (defn.get(TYPE.getText()) != null) {
          // blend in the default definition for this prompt type
          String type = (String) defn.get(TYPE.getText());
          if (promptTypes.containsKey(type)) {
            Object schema = promptTypes.get(type);
            if (schema != null) {
              // deep copy of schema because we are going to add properties recursively
              schema = deepCopyObject(schema);
              // override the promptTypes 'type' with info supplied by the prompt_type sheet.
              defn.remove(TYPE.getText());
              termMap.put(ITEMS.getText(), deepExtendObject(schema, defn));

            }
          }
        }
        recursivelyResolveTypeInDataModel(termMap.get(ITEMS.getText()), promptTypes);
      }
    }
  }

  static Object deepCopyObject(Object o) {
    Cloner cloner = new Cloner();
    return cloner.deepClone(o);
  }

  private static boolean isMapOrList(Object o) {
    return (o instanceof List || o instanceof Map || o instanceof Object[]);
  }

  private static boolean isList(Object o) {
    return (o instanceof List || o instanceof Object[]);
  }

  static Object deepExtendObject(Object o1, Object o2) {
    // stomp on o1 if o2 is not an object or array...
    if (o2 == null || !isMapOrList(o2)) {
      // don't need to deeply copy these non-array, non-objects.
      return o2;
    }
    // stomp on o1 if o1 is not an object or array...
    if (o1 == null || !isMapOrList(o1)) {
      // deep copy it...
      return deepCopyObject(o2);
    }
    // if o1 or o2 are arrays, stomp on o1 (don't merge arrays)...
    if (isList(o1) || isList(o2)) {
      // deep copy it...
      return deepCopyObject(o2);
    }

    // OK. they are both objects...
    if (o1 instanceof Map && o2 instanceof Map) {
      Map<String, Object> map1 = (Map<String, Object>) o1;
      Map<String, Object> map2 = (Map<String, Object>) o2;

      for (String key : map2.keySet()) {
        if (map1.containsKey(key)) {
          deepExtendObject(map1.get(key), map2.get(key));
        } else {
          map1.put(key, map2.get(key));
        }

      }

    } else {
      throw new OdkXlsValidationException(
          "Trying to copy a map that is not a map during data model creation.");
    }


    // return the extended object
    return o1;
  };
}

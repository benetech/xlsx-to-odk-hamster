package org.benetech.xlsxodk.conversion;

import static org.benetech.xlsxodk.ColumnToken._CONFLICT_TYPE;
import static org.benetech.xlsxodk.ColumnToken._FILTER_TYPE;
import static org.benetech.xlsxodk.ColumnToken._FILTER_VALUE;
import static org.benetech.xlsxodk.ColumnToken._FORM_ID;
import static org.benetech.xlsxodk.ColumnToken._ID;
import static org.benetech.xlsxodk.ColumnToken._LOCALE;
import static org.benetech.xlsxodk.ColumnToken._ROW_ETAG;
import static org.benetech.xlsxodk.ColumnToken._SAVEPOINT_CREATOR;
import static org.benetech.xlsxodk.ColumnToken._SAVEPOINT_TIMESTAMP;
import static org.benetech.xlsxodk.ColumnToken._SAVEPOINT_TYPE;
import static org.benetech.xlsxodk.ColumnToken._SYNC_STATE;
import static org.benetech.xlsxodk.PredefSheet.MODEL;
import static org.benetech.xlsxodk.Token.DATATABLEMODEL;
import static org.benetech.xlsxodk.Token.ELEMENTKEY;
import static org.benetech.xlsxodk.Token.ELEMENTNAME;
import static org.benetech.xlsxodk.Token.ELEMENTPATH;
import static org.benetech.xlsxodk.Token.ELEMENTSET;
import static org.benetech.xlsxodk.Token.ELEMENTTYPE;
import static org.benetech.xlsxodk.Token.FRAMEWORK;
import static org.benetech.xlsxodk.Token.ISNOTNULLABLE;
import static org.benetech.xlsxodk.Token.ISSESSIONVARIABLE;
import static org.benetech.xlsxodk.Token.ITEMS;
import static org.benetech.xlsxodk.Token.LISTCHILDELEMENTKEYS;
import static org.benetech.xlsxodk.Token.NOTUNITOFRETENTION;
import static org.benetech.xlsxodk.Token.PROPERTIES;
import static org.benetech.xlsxodk.Token.TABLE_ID;
import static org.benetech.xlsxodk.Token.TYPE;
import static org.benetech.xlsxodk.Token.VALUE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.benetech.xlsxodk.PredefSheet;
import org.benetech.xlsxodk.exception.OdkXlsValidationException;
import org.benetech.xlsxodk.util.ConversionUtils;

/**
 * Invert the specification in the formDef.json for properties processing
 */
public class DataTableModelCreator {

  Map<String, Object> specification;

  public DataTableModelCreator(Map<String, Object> specification) {
    this.specification = specification;
  }


  public void create() {
    // dataTableModel holds an inversion of the specification.model
    //
    // elementKey : jsonSchemaType
    //
    // with the addition of:
    // isSessionVariable : true if this is not retained across sessions
    // elementPath : pathToElement
    // elementSet : 'data' or 'instanceMetadata' for metadata columns
    // listChildElementKeys : ['key1', 'key2' ...]
    // notUnitOfRetention: true if this declares a complex type that
    // does not get stored (either in session variables or in database columns)
    //
    // within the jsonSchemaType to be used to transform to/from
    // the model contents and data table representation.
    //
    Map<String, Map<String, Object>> modelSection =
        (Map<String, Map<String, Object>>) specification.get(MODEL.getSheetName());

    Map<String, Object> dataTableModel = new LinkedHashMap<String, Object>();

    for (Entry<String, Map<String, Object>> entry : modelSection.entrySet()) {
      dataTableModel.put(entry.getKey(),
          (Map<String, Object>) ConversionUtils.deepCopyObject(entry.getValue()));
    }

    Map<String, Object> jsonDefn = new LinkedHashMap<String, Object>();

    Set<String> keys = (Set<String>)ConversionUtils.deepCopyObject(dataTableModel.keySet());

    for (String key : keys) {
      jsonDefn = flattenElementPath(dataTableModel, null, key, null,
          (Map<String, Object>) dataTableModel.get(key));
    }
    
//    Iterator<Entry<String,Object>> iterator = dataTableModel.entrySet().iterator();
//    Entry<String,Object> entry;
//    while(iterator.hasNext()) {
//      entry = iterator.next();
//      jsonDefn = flattenElementPath(dataTableModel, null, entry.getKey(), null,
//          (Map<String, Object>) entry.getValue());
//    }
    
    markUnitOfRetention(dataTableModel);

    Map<String, Object> settings = (Map<String, Object>) specification.get(PredefSheet.SETTINGS.getSheetName());
    Map<String,Object> tableId = (Map<String, Object>)settings.get(TABLE_ID.getText());
    String value = (String)tableId.get(VALUE.getText());
    
    // the framework form has no metadata...
    if (! FRAMEWORK.getText().equals(value) ) {
        // add in the metadata columns...
        // the only ones of these that are settable outside of the data layer are
        //    _form_id
        //    _locale
        //
        // the values for all other metadata are managed within the data layer.
        //
      dataTableModel.put(_ID.getText(), getNewColumn("string",  Boolean.TRUE, _ID.getText(),  _ID.getText(),  "instanceMetadata",  _ID.getText() ));
      dataTableModel.put(_ROW_ETAG.getText(), getNewColumn("string",  Boolean.FALSE, _ROW_ETAG.getText(),  _ROW_ETAG.getText(),  "instanceMetadata",  _ROW_ETAG.getText() ));
      dataTableModel.put(_SYNC_STATE.getText(), getNewColumn("string",  Boolean.TRUE, _SYNC_STATE.getText(),  _SYNC_STATE.getText(),  "instanceMetadata",  _SYNC_STATE.getText() ));
      dataTableModel.put(_CONFLICT_TYPE.getText(), getNewColumn("integer",  Boolean.FALSE, _CONFLICT_TYPE.getText(),  _CONFLICT_TYPE.getText(),  "instanceMetadata",  _CONFLICT_TYPE.getText() ));
      dataTableModel.put(_FILTER_TYPE.getText(), getNewColumn("string",  Boolean.FALSE, _FILTER_TYPE.getText(),  _FILTER_TYPE.getText(),  "instanceMetadata",  _FILTER_TYPE.getText()) );
      dataTableModel.put(_FILTER_VALUE.getText(), getNewColumn("string",  Boolean.FALSE, _FILTER_VALUE.getText(),  _FILTER_VALUE.getText(),  "instanceMetadata",  _FILTER_VALUE.getText()) );
      dataTableModel.put(_FORM_ID.getText(), getNewColumn("string",  Boolean.FALSE, _FORM_ID.getText(),  _FORM_ID.getText(),  "instanceMetadata",  _FORM_ID.getText()) );
      dataTableModel.put(_LOCALE.getText(), getNewColumn("string",  Boolean.FALSE, _LOCALE.getText(),  _LOCALE.getText(),  "instanceMetadata",  _LOCALE.getText()) );
      dataTableModel.put(_SAVEPOINT_TYPE.getText(), getNewColumn("string",  Boolean.FALSE, _SAVEPOINT_TYPE.getText(),  _SAVEPOINT_TYPE.getText(),  "instanceMetadata",  _SAVEPOINT_TYPE.getText() ));
      dataTableModel.put(_SAVEPOINT_TIMESTAMP.getText(), getNewColumn("string",  Boolean.TRUE, _SAVEPOINT_TIMESTAMP.getText(),  _SAVEPOINT_TIMESTAMP.getText(),  "instanceMetadata",  _SAVEPOINT_TIMESTAMP.getText() ));
      dataTableModel.put(_SAVEPOINT_CREATOR.getText(), getNewColumn("string",  Boolean.FALSE, _SAVEPOINT_CREATOR.getText(),  _SAVEPOINT_CREATOR.getText(),  "instanceMetadata",  _SAVEPOINT_CREATOR.getText() ));   
    }
    
    specification.put(DATATABLEMODEL.getText(), dataTableModel);
   
  }

  public Map<String, Object> getNewColumn(String type, Boolean isNotNullable, String elementKey,
      String elementName, String elementSet, String elementPath) {
    Map<String, Object> result = new LinkedHashMap<String, Object>();
    result.put(TYPE.getText(), type);
    result.put(ISNOTNULLABLE.getText(), isNotNullable);
    result.put(ELEMENTKEY.getText(), elementKey);
    result.put(ELEMENTNAME.getText(), elementName);
    result.put(ELEMENTSET.getText(), elementSet);
    result.put(ELEMENTPATH.getText(), elementPath);

    return result;
  }

  /**
   * Invert a formDef.json for XLSXConverter
   */
  public static Map<String, Object> flattenElementPath(Map<String, Object> dbKeyMap,
      String elementPathPrefix, String elementName, String elementKeyPrefix,
      Map<String, Object> jsonType) {

    // var that = this;
    String fullPath;
    String elementKey;
    int i = 0;

    // remember the element name...
    jsonType.put(ELEMENTNAME.getText(), elementName);
    // and the set is 'data' because it comes from the data model...
    jsonType.put(ELEMENTSET.getText(), "data");

    // update element path prefix for recursive elements
    elementPathPrefix = StringUtils.isEmpty(elementPathPrefix) ? elementName
        : elementPathPrefix + "." + elementName;

    // and our own element path is exactly just this prefix
    jsonType.put(ELEMENTPATH.getText(), elementPathPrefix);


    // use the user's elementKey if specified
    elementKey = (String) jsonType.get(ELEMENTKEY.getText());

    if (StringUtils.isEmpty(elementKey)) {
      elementKey = elementPathPrefix.replaceAll("\\.", "_");
      jsonType.put(ELEMENTKEY.getText(), elementKey);

    }

    if (StringUtils.isEmpty(elementKey)) {
      throw new OdkXlsValidationException(
          "elementKey is not defined for '" + jsonType.get(ELEMENTPATH.getText()) + "'.");
    }

    // simple error tests...
    // throw an error if the elementkey is longer than 62 characters
    // or if it is already being used and not by myself...
    if (elementKey.length() > 62) {
      throw new OdkXlsValidationException("supplied elementKey is longer than 62 characters");
    }
    Object dbValue = (Map<String, Object>) dbKeyMap.get(elementKey);
    if (dbValue != null && dbValue != jsonType) {
      throw new OdkXlsValidationException(
          "supplied elementKey is already used (autogenerated?) for another model element");
    }
    if ('_' == (elementKey.charAt(0))) {
      throw new OdkXlsValidationException("supplied elementKey starts with underscore");
    }

    // remember the elementKey we have chosen...
    dbKeyMap.put(elementKey, jsonType);

    // handle the recursive structures...
    if ("array".equals(jsonType.get(TYPE.getText()))) {
      Map<String, Object> items = (Map<String, Object>) (jsonType.get(ITEMS.getText()));
      // explode with subordinate elements
      Map<String, Object> flattened = (Map<String, Object>) flattenElementPath(dbKeyMap,
          elementPathPrefix, ITEMS.getText(), elementKey, items);
      jsonType.put(LISTCHILDELEMENTKEYS.getText(), flattened.get(ELEMENTKEY.getText()));
    } else if ("object".equals(jsonType.get(TYPE.getText()))) {
      // object...
      List<String> listChildElementKeys = new ArrayList<String>();
      Map<String, Map<String, Object>> properties =
          (Map<String, Map<String, Object>>) jsonType.get(PROPERTIES.getText());
      for (String propertyName : properties.keySet()) {
        Map<String, Object> flattened = flattenElementPath(dbKeyMap, elementPathPrefix,
            propertyName, elementKey, properties.get(propertyName));
        listChildElementKeys.add((String) flattened.get(ELEMENTKEY.getText()));
      }
      // the children need to be in alphabetical order in order to be repeatably comparable.
      Collections.sort(listChildElementKeys);
      jsonType.put(LISTCHILDELEMENTKEYS.getText(), listChildElementKeys);
    }

    Boolean isSessionVariable = (Boolean) jsonType.get(ISSESSIONVARIABLE.getText());
    isSessionVariable = isSessionVariable == null ? Boolean.FALSE : isSessionVariable;
    if (isSessionVariable && (jsonType.get(LISTCHILDELEMENTKEYS.getText()) != null)) {
      // we have some sort of structure that is a sessionVariable
      // Set the isSessionVariable tags on all its nested elements.
      _setSessionVariableFlag(dbKeyMap,
          (List<String>) jsonType.get(LISTCHILDELEMENTKEYS.getText()));
    }
    return jsonType;
  }

  /**
   * Mark all of the session variables for XLSXConverter
   */
  private static void _setSessionVariableFlag(Map<String, Object> dbKeyMap,
      List<String> listChildElementKeys) {
    // var that = this;
    int i;
    if (listChildElementKeys != null) {
      for (i = 0; i < listChildElementKeys.size(); ++i) {
        String f = listChildElementKeys.get(i);
        Map<String, Object> jsonType = (Map<String, Object>) dbKeyMap.get(f);
        jsonType.put(ISSESSIONVARIABLE.getText(), Boolean.TRUE);
        if ("array".equals(jsonType.get(TYPE.getText()))) {
          _setSessionVariableFlag(dbKeyMap,
              (List<String>) jsonType.get(LISTCHILDELEMENTKEYS.getText()));
        } else if ("object".equals(jsonType.get(TYPE.getText()))) {
          _setSessionVariableFlag(dbKeyMap,
              (List<String>) jsonType.get(LISTCHILDELEMENTKEYS.getText()));
        }
      }
    }
  }

  String getPrimitiveTypeOfElementType(String elementType) {
    int idxColon = elementType.indexOf(":");
    int idxParen = elementType.indexOf("(");
    if (idxParen != -1 && idxColon > idxParen) {
      idxColon = -1;
    }
    if (idxColon == -1) {
      if (idxParen != -1) {
        return elementType.substring(0, idxParen);
      } else {
        return elementType;
      }
    } else {
      if (idxParen != -1) {
        return elementType.substring(idxColon + 1, idxParen);
      } else {
        return elementType.substring(idxColon + 1);
      }
    }
  }

  /**
   * Mark the elements of an inverted formDef.json that will be retained in the database for
   * XLSXConverter
   */
  private void markUnitOfRetention(Map<String, Object> dataTableModel) {
    // for all arrays, mark all descendants of the array as not-retained
    // because they are all folded up into the json representation of the array
    Map<String, Object> jsonDefn;
    String elementTypePrimitive;
    String key;
    Map<String, Object> jsonSubDefn;

    for (String startKey : dataTableModel.keySet()) {
      jsonDefn = (Map<String, Object>) dataTableModel.get(startKey);
      Boolean notUnitOfRetention = (Boolean) jsonDefn.get(NOTUNITOFRETENTION.getText());
      if (notUnitOfRetention != null && notUnitOfRetention) {
        // this has already been processed
        continue;
      }
      String jsonElementType = (String) jsonDefn.get(ELEMENTTYPE.getText());
      elementTypePrimitive = (jsonElementType == null ? (String) jsonDefn.get(TYPE.getText())
          : getPrimitiveTypeOfElementType(jsonElementType));
      if ("array".equals(elementTypePrimitive)) {
        List<String> descendantsOfArray = new ArrayList<String>();
        List<String> listChildElementKeys =
            (List<String>) jsonDefn.get(LISTCHILDELEMENTKEYS.getText());
        descendantsOfArray =
            listChildElementKeys == null ? new ArrayList<String>() : listChildElementKeys;
        List<String> scratchArray = new ArrayList<String>();

        while (descendantsOfArray.size() != 0) {

          for (int i = 0; i < descendantsOfArray.size(); ++i) {
            key = descendantsOfArray.get(i);
            jsonSubDefn = (Map<String, Object>) dataTableModel.get(key);
            if (jsonSubDefn != null) {
              Boolean notUnitOfRetentionSub =
                  (Boolean) jsonSubDefn.get(NOTUNITOFRETENTION.getText());

              if (notUnitOfRetentionSub != null && notUnitOfRetentionSub) {
                // this has already been processed
                continue;
              }
              jsonSubDefn.put(NOTUNITOFRETENTION.getText(), Boolean.TRUE);
              List<String> listChildElementKeysSub =
                  (List<String>) jsonSubDefn.get(LISTCHILDELEMENTKEYS.getText());

              listChildElementKeysSub = ((listChildElementKeysSub == null) ? new ArrayList<String>()
                  : (List<String>) jsonSubDefn.get(LISTCHILDELEMENTKEYS.getText()));
              scratchArray.addAll(listChildElementKeysSub);
            }
          }
          descendantsOfArray = scratchArray;
          scratchArray = new ArrayList<String>();
        }
      }
    }
    // and mark any non-arrays with multiple fields as not retained
    for (String startKey : dataTableModel.keySet()) {
      jsonDefn = (Map<String, Object>) dataTableModel.get(startKey);
      Boolean notUnitOfRetention = (Boolean) jsonDefn.get(NOTUNITOFRETENTION.getText());

      if (notUnitOfRetention != null && notUnitOfRetention) {
        // this has already been processed
        continue;
      }
      String jsonElementType = (String) jsonDefn.get(ELEMENTTYPE.getText());
      elementTypePrimitive = (jsonElementType == null ? (String) jsonDefn.get(TYPE.getText())
          : getPrimitiveTypeOfElementType(jsonElementType));
      if (!"array".equals(elementTypePrimitive)) {
        List<String> jsonListChildElementKeys =
            (List<String>) jsonDefn.get(LISTCHILDELEMENTKEYS.getText());

        if (jsonListChildElementKeys != null && jsonListChildElementKeys.size() != 0) {
          jsonDefn.put(NOTUNITOFRETENTION.getText(), Boolean.TRUE);
        }
      }
    }
  };
}

package org.benetech.xlsxodk.conversion;

import java.util.List;
import java.util.Map;

import static org.benetech.xlsxjson.Xlsx2JsonConverter.ROW_NUM_KEY;
import static org.benetech.xlsxodk.PredefSheet.CHOICES;
import static org.benetech.xlsxodk.Token.*;
import static org.benetech.xlsxodk.UnderscoreToken.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.benetech.xlsxodk.PredefSheet;
import org.benetech.xlsxodk.exception.OdkXlsValidationException;
import org.benetech.xlsxodk.util.ConversionUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.benetech.xlsxodk.PredefSheet;

public class PropertiesConverter {
  Map<String, Object> specification;
  final Gson gson = new GsonBuilder().disableHtmlEscaping().create();


  public PropertiesConverter(Map<String, Object> specification,
      Map<String, List<Map<String, Object>>> xlsx) {
    this.specification = specification;
    convert(xlsx);
  }

  private boolean isString(Object str) {
    return str != null && str instanceof String;
  }

  public void convert(Map<String, List<Map<String, Object>>> xlsx) {
    Map<String, Object> settings =
        (Map<String, Object>) specification.get(PredefSheet.SETTINGS.getSheetName());
    Map<String, Object> tableId = (Map<String, Object>) settings.get(TABLE_ID.getText());
    String tableIdValue = (String) tableId.get(VALUE.getText());
    Map<String, Object> formId = (Map<String, Object>) settings.get(TABLE_ID.getText());
    String formIdValue = (String) tableId.get(VALUE.getText());

    if (!formIdValue.equals(tableIdValue)) {
      if (xlsx.containsKey(PROPERTIES.getText())) {
        throw new OdkXlsValidationException(
            "The 'properties' sheet is only allowed in the worksheet of the '" + tableIdValue
                + "' form_id.");
      }
    } else {
      // process the properties sheet if we have it...
      List<Object> processedProperties = new ArrayList<Object>();
      boolean foundTableDefaultViewType = false;
      boolean foundTableFormTypeSpec = false;
      boolean foundTableFormTypeSurveySpec = false;

      if (xlsx.containsKey(PROPERTIES.getText())) {
        List<Map<String, Object>> cleanSet = xlsx.get(PROPERTIES.getText());
        cleanSet = ConversionUtils.omitRowsWithMissingField(cleanSet, PARTITION.getText());
        cleanSet = ConversionUtils.omitRowsWithMissingField(cleanSet, ASPECT.getText());
        cleanSet = ConversionUtils.omitRowsWithMissingField(cleanSet, KEY.getText());
        cleanSet = ConversionUtils.omitRowsWithMissingField(cleanSet, TYPE.getText());
        cleanSet = ConversionUtils.omitRowsWithMissingField(cleanSet, VALUE.getText());

        for (Map<String, Object> row : cleanSet) {
          if (!isString(row.get(PARTITION.getText()))) {
            throw new OdkXlsValidationException(
                "The 'partition' column on the 'properties' sheet must be a string. Error at row: "
                    + row.get(ROW_NUM_KEY));
          }
          if (!isString(row.get(ASPECT.getText()))) {
            throw new OdkXlsValidationException(
                "The 'aspect' column on the 'properties' sheet must be a string. Error at row: "
                    + row.get(ROW_NUM_KEY));

          }
          if (!isString(row.get(KEY.getText()))) {
            throw new OdkXlsValidationException(
                "The 'key' column on the 'properties' sheet must be a string. Error at row: "
                    + row.get(ROW_NUM_KEY));

          }
          if (!isString(row.get(TYPE.getText()))) {
            throw new OdkXlsValidationException(
                "The 'type' column on the 'properties' sheet must be a string. Error at row: "
                    + row.get(ROW_NUM_KEY));

          }
          if (!isString(row.get(VALUE.getText()))) {
            throw new OdkXlsValidationException(
                "The 'value' column on the 'properties' sheet must be a string. Error at row: "
                    + row.get(ROW_NUM_KEY));

          }
          if (COLUMN.getText().equals(row.get(PARTITION.getText()))
              && DISPLAYNAME.getText().equals(row.get(KEY.getText()))) {
            throw new OdkXlsValidationException(
                "Column property 'displayName' cannot be specified on the 'properties' sheet. This is auto-generated from the model. Error at row: "
                    + row.get(ROW_NUM_KEY));

          }
          if (COLUMN.getText().equals(row.get(PARTITION.getText()))
              && DISPLAYFORMAT.getText().equals(row.get(KEY.getText()))) {
            throw new OdkXlsValidationException(
                "Column property 'displayFormat' cannot be specified on the 'properties' sheet. This is auto-generated from the model. Error at row: "
                    + row.get(ROW_NUM_KEY));

          }
          if (COLUMN.getText().equals(row.get(PARTITION.getText()))
              && DISPLAYCHOICESLIST.getText().equals(row.get(KEY.getText()))) {
            throw new OdkXlsValidationException(
                "Column property 'displayChoicesList' cannot be specified on the 'properties' sheet. This is auto-generated from the model. Error at row: "
                    + row.get(ROW_NUM_KEY));

          }
          if (TABLE.getText().equals(row.get(PARTITION.getText()))
              && DEFAULT.getText().equals(row.get(ASPECT.getText()))
              && DISPLAYNAME.getText().equals(row.get(KEY.getText()))) {
            throw new OdkXlsValidationException(
                "Table property 'displayName' cannot be specified on the 'properties' sheet. This is auto-generated from the settings. Error at row: "
                    + row.get(ROW_NUM_KEY));
          }
          if (TABLE.getText().equals(row.get(PARTITION.getText()))
              && DEFAULT.getText().equals(row.get(ASPECT.getText()))
              && DEFAULTVIEWTYPE.getText().equals(row.get(KEY.getText()))) {
            foundTableDefaultViewType = true;
          }
          if (FORMTYPE.getText().equals(row.get(PARTITION.getText()))
              && DEFAULT.getText().equals(row.get(ASPECT.getText()))
              && DEFAULTVIEWTYPE.getText().equals(row.get("FormType.formType"))) {
            foundTableFormTypeSpec = true;
          }
          if (SURVEYUTIL.getText().equals(row.get(PARTITION.getText()))
              && DEFAULT.getText().equals(row.get(ASPECT.getText()))
              && DEFAULTVIEWTYPE.getText().equals(row.get("SurveyUtil.formId"))) {
            foundTableFormTypeSurveySpec = true;
          }
          processedProperties.add(getNewProperty((String) row.get(PARTITION.getText()),
              (String) row.get(ASPECT.getText()), (String) row.get(KEY.getText()),
              (String) row.get(TYPE.getText()), (String) row.get(VALUE.getText())));
        }

      }

      // and now traverse the specification.dataTableModel making sure all the
      // elementSet: 'data' values have columnDefinitions entries drawn
      // from the model

      Map<String, Object> dataTableModel =
          (Map<String, Object>) specification.get(DATATABLEMODEL.getText());
      for (String dbColumnName : dataTableModel.keySet()) {
        // the XLSXconverter already handles expanding complex types
        // such as geopoint into their underlying storage representation.
        Map<String, Object> jsonDefn = (Map<String, Object>) dataTableModel.get(dbColumnName);


        Boolean isSessionVariable = (Boolean) jsonDefn.get(ISSESSIONVARIABLE.getText());
        isSessionVariable = isSessionVariable == null ? Boolean.FALSE : isSessionVariable;
        if (DATA.getText().equals(jsonDefn.get(ELEMENTSET.getText()))) {
          String surveyElementName = (String) jsonDefn.get(ELEMENTNAME.getText());
          String displayName = (String) jsonDefn.get(DISPLAYNAME.getText());
          if (StringUtils.isNotEmpty(displayName)) {
            processedProperties.add(getNewProperty(COLUMN.getText(), dbColumnName,
                DISPLAYNAME.getText(), OBJECT.getText(), gson.toJson(displayName)));
          }

          String valuesList = (String) jsonDefn.get(VALUESLIST.getText());

          if (StringUtils.isNotEmpty(valuesList)) {
            Map<String, List<Map<String, Object>>> choicesMap =
                (Map<String, List<Map<String, Object>>>) specification.get(CHOICES.getSheetName());
            Object ref = choicesMap.get(valuesList);
            processedProperties.add(getNewProperty(COLUMN.getText(), dbColumnName,
                DISPLAYCHOICESLIST.getText(), OBJECT.getText(), gson.toJson(ref)));
          }
          String displayFormat = (String) jsonDefn.get(DISPLAYFORMAT.getText());
          if (displayFormat != null) {
            processedProperties.add(getNewProperty(COLUMN.getText(), dbColumnName,
                DISPLAYFORMAT.getText(), STRING.getText(), displayFormat));

          }

        }
      }

      if (!(foundTableFormTypeSpec || foundTableFormTypeSurveySpec)) {
        // set this form as the default form to open when editting or adding a row...
        processedProperties.add(getNewProperty(FORMTYPE.getText(), DEFAULT.getText(),
            "FormType.formType", STRING.getText(), PredefSheet.SURVEY.name()));
        processedProperties.add(getNewProperty(SURVEYUTIL.getText(), DEFAULT.getText(),
            "SurveyUtil.formId", STRING.getText(), formIdValue));
      }

      if (!foundTableDefaultViewType) {
        // set the spreadsheet view as the default view...

        processedProperties.add(getNewProperty(TABLE.getText(), DEFAULT.getText(),
            DEFAULTVIEWTYPE.getText(), STRING.getText(), SPREADSHEET.getText()));
      }

      Map<String, Object> survey =
          (Map<String, Object>) settings.get(PredefSheet.SURVEY.getSheetName());
      Map<String, Object> display = (Map<String, Object>) survey.get(DISPLAY.getText());
      Object formTitle = display.get(TITLE.getText());

      processedProperties.add(getNewProperty(TABLE.getText(), DEFAULT.getText(),
          DISPLAYNAME.getText(), OBJECT.getText(), gson.toJson(formTitle)));



      Collections.sort((List<Object>) processedProperties, new Comparator<Object>() {

        @Override
        public int compare(final Object prop1, final Object prop2) {
          Map<String, Object> propMap1 = (Map<String, Object>) prop1;
          Map<String, Object> propMap2 = (Map<String, Object>) prop2;

          int c;
          String partition1 = (String) propMap1.get(_PARTITION.getText());
          String partition2 = (String) propMap2.get(_PARTITION.getText());
          c = StringUtils.compare(partition1, partition2);
          if (c == 0) {
            String aspect1 = (String) propMap1.get(_ASPECT.getText());
            String aspect2 = (String) propMap2.get(_ASPECT.getText());
            c = StringUtils.compare(aspect1, aspect2);
          }
          if (c == 0) {
            String key1 = (String) propMap1.get(_KEY.getText());
            String key2 = (String) propMap2.get(_KEY.getText());
            c = StringUtils.compare(key1, key2);
          }
          return c;
        }
      });

      // These are all the properties that will be written into the properties.csv file.

      // TODO: remove these from this file.
      specification.put(PROPERTIES.getText(), processedProperties);
    }
  }

  public Map<String, Object> getNewProperty(String partition, String aspect, String key,
      String type, String value) {
    Map<String, Object> result = new LinkedHashMap<String, Object>();
    result.put(_PARTITION.getText(), partition);
    result.put(_ASPECT.getText(), aspect);
    result.put(_KEY.getText(), key);
    result.put(_TYPE.getText(), type);
    result.put(_VALUE.getText(), value);
    return result;
  }

}

package org.benetech.xlsxodk.conversion;
import static org.benetech.xlsxodk.PredefSheet.CALCULATES;
import static org.benetech.xlsxodk.PredefSheet.CHOICES;
import static org.benetech.xlsxodk.PredefSheet.COLUMN_TYPES;
import static org.benetech.xlsxodk.PredefSheet.INITIAL;
import static org.benetech.xlsxodk.PredefSheet.MODEL;
import static org.benetech.xlsxodk.PredefSheet.PROMPT_TYPES;
import static org.benetech.xlsxodk.PredefSheet.QUERIES;
import static org.benetech.xlsxodk.PredefSheet.SETTINGS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.benetech.xlsxodk.Constants;
import org.benetech.xlsxodk.util._Dumper;
import org.benetech.xlsxodk.validation.SpecificationValidator;

/**
 * Ties together all of the parts of the Specification block of formDef.json
 */
public class SpecificationConverter {

  Map<String, Object> specification;

  Map<String, Object> promptTypes;

  List<String> sectionNames;

  Map<String, List<Map<String, Object>>> sectionSheets;

  public SpecificationConverter(Map<String, List<Map<String, Object>>> originalXlsx) {
    specification = new LinkedHashMap<String, Object>();
    addColumnTypesSection();
    addSettingsSection(originalXlsx);
    addChoicesSection(originalXlsx);
    addQueriesSection(originalXlsx);
    addCalculatesSection(originalXlsx);
    setPromptTypes(originalXlsx);
    addModelSection(originalXlsx);
    setSectionSheets(originalXlsx);
    addInitialSection();
    Collections.sort(sectionNames);
    addSectionsToSettings();
    parseSections();
    developDataModel();
    SpecificationValidator.checkValuesList(specification);
    createDataTableModel();

  }


  
  public void addColumnTypesSection() {
    // TODO: Add custom column types
    specification.put(COLUMN_TYPES.getSheetName(), Constants.COLUMN_TYPE_MAP);

  }

  public void addSettingsSection(Map<String, List<Map<String, Object>>> xlsx) {
    SettingsConverter settingsConverter =
        new SettingsConverter(xlsx.get(SETTINGS.getSheetName()));
    specification.put(SETTINGS.getSheetName(), settingsConverter.getSettingsMap());

  }

  public void addChoicesSection(Map<String, List<Map<String, Object>>> xlsx) {
    ChoicesConverter choicesConverter =
        new ChoicesConverter(xlsx.get(CHOICES.getSheetName()));
    specification.put(CHOICES.getSheetName(), choicesConverter.getChoicesMap());

  }

  public void addQueriesSection(Map<String, List<Map<String, Object>>> xlsx) {
    QueriesConverter queriesConverter =
        new QueriesConverter(xlsx.get(QUERIES.getSheetName()));
    SpecificationValidator.checkQueriesChoicesColumnsDuplicates(queriesConverter.getQueriesMap(),
        (Map<String, List<Map<String, Object>>>) specification
            .get(QUERIES.getSheetName()));
    specification.put(QUERIES.getSheetName(), queriesConverter.getQueriesMap());
  }

  public void addCalculatesSection(Map<String, List<Map<String, Object>>> xlsx) {
    CalculatesConverter calculatesConverter =
        new CalculatesConverter(xlsx.get(CALCULATES.getSheetName()));
    specification.put(CALCULATES.getSheetName(),
        calculatesConverter.getCalculationsMap());
  }

  public void setPromptTypes(Map<String, List<Map<String, Object>>> xlsx) {
    PromptTypesConverter promptTypesConverter =
        new PromptTypesConverter(xlsx.get(PROMPT_TYPES.getSheetName()));
    promptTypes = promptTypesConverter.getPromptTypeMap();
  }

  public void addModelSection(Map<String, List<Map<String, Object>>> xlsx) {
    ModelConverter modelConverter = new ModelConverter(xlsx.get(MODEL.getSheetName()));
    specification.put(MODEL.getSheetName(), modelConverter.getModelMap());
  }

  public void setSectionSheets(Map<String, List<Map<String, Object>>> xlsx) {
    SpecificationValidator.validateSheetNames(xlsx.keySet());
    if (sectionNames == null) {
      sectionNames = new ArrayList<String>();
    }
    if (sectionSheets == null) {
       sectionSheets = new LinkedHashMap<String, List<Map<String, Object>>>();
    }
    for (String sheetName : xlsx.keySet()) {
      if (!Constants.RESERVED_SHEET_NAMES.contains(sheetName) && !(sheetName.startsWith("-"))) {
        sectionNames.add(sheetName);
        sectionSheets.put(sheetName, xlsx.get(sheetName));
      }
    }
  }

  public void addInitialSection() {
    if (!sectionNames.contains(INITIAL.getSheetName())) {
      sectionSheets.put(INITIAL.getSheetName(), Constants.DEFAULT_INITIAL);
      sectionNames.add(INITIAL.getSheetName());
    }
  }

  public void addSectionsToSettings() {
    Map<String, Map<String, Object>> settingsMap =
        (Map<String, Map<String, Object>>) specification.get(SETTINGS.getSheetName());
    Object surveyDisplay = settingsMap.get("survey").get("display");
    for (String sectionName : sectionNames) {
      if (settingsMap.get(sectionName) == null) {
        settingsMap.put(sectionName, new LinkedHashMap<String, Object>());
      }
      if (settingsMap.get(sectionName).get("display") == null) {
        settingsMap.get(sectionName).put("display", surveyDisplay);
      }
    }
  }


  public void parseSections() {
    SectionParser sectionParser = new SectionParser();
    Map<String, Map<String, Object>> newSections = sectionParser.parseSections(sectionNames, sectionSheets, 
        (Map<String, Map<String, Object>>)specification.get(SETTINGS.getSheetName()), 
        (Map<String,String>)specification.get(COLUMN_TYPES.getSheetName()));
    specification.put("section_names", sectionNames);
    specification.put("sections", newSections);


  }


  public void developDataModel() {
    DataModelCreator dataModelCreator = new DataModelCreator(specification, promptTypes) ;
    dataModelCreator.create();
    _Dumper._dump_section("developDataModel_2914", specification.get(MODEL.getSheetName()));

  }
  
  public void createDataTableModel() {
    DataTableModelCreator dataTableModelCreator = new DataTableModelCreator(specification) ;
    dataTableModelCreator.create();
  }
  
  public Map<String, Object> getSpecification() {
    return specification;
  }


  public Map<String, Object> getPromptTypes() {
    return promptTypes;
  }


  public List<String> getSectionNames() {
    return sectionNames;
  }


  public Map<String, List<Map<String, Object>>> getSectionSheets() {
    return sectionSheets;
  }


}

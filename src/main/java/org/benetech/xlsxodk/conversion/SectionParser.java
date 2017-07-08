package org.benetech.xlsxodk.conversion;

import static org.benetech.xlsxjson.Xlsx2JsonConverter.ROW_NUM_KEY;
import static org.benetech.xlsxodk.PredefSheet.INITIAL;
import static org.benetech.xlsxodk.ClauseToken.IF;
import static org.benetech.xlsxodk.ClauseToken.*;
import static org.benetech.xlsxodk.TopLevelBlockToken.ASSIGN;
import static org.benetech.xlsxodk.TopLevelBlockToken.*;
import static org.benetech.xlsxodk.TopLevelBlockToken.BACK_IN_HISTORY;
import static org.benetech.xlsxodk.TopLevelBlockToken.BEGIN_IF;
import static org.benetech.xlsxodk.TopLevelBlockToken.BEGIN_SCREEN;
import static org.benetech.xlsxodk.TopLevelBlockToken.BRANCH_LABEL;
import static org.benetech.xlsxodk.TopLevelBlockToken.DO_SECTION;
import static org.benetech.xlsxodk.TopLevelBlockToken.EXIT_SECTION;
import static org.benetech.xlsxodk.TopLevelBlockToken.GOTO_LABEL;
import static org.benetech.xlsxodk.TopLevelBlockToken.PROMPT;
import static org.benetech.xlsxodk.TopLevelBlockToken.SAVE_AND_TERMINATE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.xlsxodk.ClauseToken;
import org.benetech.xlsxodk.FileLoadTest;
import org.benetech.xlsxodk.PredefSheet;
import org.benetech.xlsxodk.TopLevelBlockToken;
import org.benetech.xlsxodk.exception.OdkXlsValidationException;
import org.benetech.xlsxodk.util.ValidationUtils;

public class SectionParser {
  
  Log logger = LogFactory.getLog(SectionParser.class);


  // Common tokens/strings to prevent developer typos
  private static final String AND = "and";
  private static final String CALCULATION = "calculation";
  private static final String CLAUSE = "clause";
  private static final String COMMENT = "comment";
  private static final String COMMENT_SLASHES = "//";
  private static final String CONDITION = "condition";
  private static final String CONSTRAINT = "constraint";
  private static final String CONTENTS = "contents";
  private static final String DISPLAY = "display";
  private static final String FINALIZE = "finalize";
  private static final String IDX = "idx";
  private static final String NAME = "name";
  private static final String OPERATION_IDX = "operationIdx";
  private static final String PROMPT_IDX = "promptIdx";
  private static final String REQUIRED = "required";
  private static final String RESUME = "resume";
  private static final String SCREEN = "screen";
  private static final String SCREEN_BLOCK = "screen_block";
  private static final String SECTION = "section";
  private static final String SAVE = "save";
  private static final String TERMINATE = "terminate";
  private static final String TYPE = "type";
  private static final String VALIDATION_TAGS = "validation_tags";

  private static final String _BRANCH_LABEL = "_branch_label";
  private static final String _BRANCH_LABEL_ENCLOSING_SCREEN = "_branch_label_enclosing_screen";
  private static final String _CONTENTS = "_contents";
  private static final String _DATA_TYPE = "_data_type";
  private static final String _DO_SECTION_NAME = "_do_section_name";
  private static final String _ELSE_BLOCK = "_else_block";
  private static final String _ELSE_CLAUSE = "_else_clause";
  private static final String _END_IF_CLAUSE = "_end_if_clause";
  private static final String _END_SCREEN_CLAUSE = "_end_screen_clause";
  private static final String _FINALIZE = "_finalize";
  private static final String _SCREEN_BLOCK = "_screen_block";
  private static final String _SECTION = "_section";
  private static final String _SWEEP_NAME = "_sweep_name";
  private static final String _TAG_NAME = "_tag_name";
  private static final String _THEN_BLOCK = "_then_block";
  private static final String _TOKEN_TYPE = "_token_type";
  private static final String _TYPE = "_type";

  public Map<String, Map<String, Object>> parseSections(List<String> sectionNames,
      Map<String, List<Map<String, Object>>> sections, Map<String, Map<String, Object>> settings,
      Map<String, String> columnTypeMap) {

    Map<String, Map<String, Object>> newSections = new LinkedHashMap<String, Map<String, Object>>();

    for (String sectionName : sections.keySet()) {
      List<Map<String, Object>> section = sections.get(sectionName);
      Map<String, Object> newSection = parseSection(sectionName, section, settings, columnTypeMap);
      newSections.put(sectionName, newSection);
    }



    return newSections;
  }

  private Map<String, Object> parseSection(String sheetName, List<Map<String, Object>> sheet,
      Map<String, Map<String, Object>> settings, Map<String, String> columnTypeMap) {
    for (int i = 0; i < sheet.size(); i++) {
      ValidationUtils.sanityCheckFieldTypes();
    }

    List<Map<String, Object>> flow = parseControlFlowSection(sheetName, sheet);

    List<Map<String, Object>> blockFlow = parseTopLevelBlock(sheetName, flow);
    /*
     * flattenBlocks(String sheetName, List<Map<String, Object>> prompts, Map<String, List<Integer>>
     * validationTagMap, List<Map<String, Object>> flattened, List<Map<String, Object>> blockFlow,
     * int idx, Map<String, Map<String, Object>> specSettings)
     */

    Map<String, List<Integer>> validationTagMap = new HashMap<String, List<Integer>>();
    validationTagMap.put(_FINALIZE, new ArrayList<Integer>());
    List<Map<String, Object>> prompts = new ArrayList<Map<String, Object>>();

    List<Map<String, Object>> flattened = new ArrayList<Map<String, Object>>();

    flattenBlocks(sheetName, prompts, validationTagMap, flattened, blockFlow, 0, settings);

    Map<String, Integer> branchLabelMap = new LinkedHashMap<String, Integer>();
    List<Map<String, Object>> operations = new ArrayList<Map<String, Object>>();
    extractBranchLabels(sheetName, operations, branchLabelMap, flattened, 0);
    verifyReachableLabels(sheetName, operations, branchLabelMap, 0, false);

    Map<String, Boolean> nestedSections = new LinkedHashMap<String, Boolean>();

    gatherNestedSections(sheetName, operations, nestedSections, 0);

    Map<String, Boolean> reachableSections = new LinkedHashMap<String, Boolean>();
    reachableSections.putAll(nestedSections);

    Map<String, Object> parsedSection = new LinkedHashMap<String, Object>();
    parsedSection.put("section_name", sheetName);
    parsedSection.put("nested_sections", nestedSections);
    parsedSection.put("reachable_sections", reachableSections);
    parsedSection.put("prompts", prompts);
    parsedSection.put("validation_tag_map", validationTagMap);
    parsedSection.put("operations", operations);
    parsedSection.put("branch_label_map", branchLabelMap);

    return parsedSection;
  }

  /**
   * Verify that all goto_label constructs have a reachable branch label.
   */
  void verifyReachableLabels(String sheetName, List<Map<String, Object>> operations,
      Map<String, Integer> branchLabelMap, int idx, boolean inscreen) {
    int i = idx;
    while (i < operations.size()) {
      Map<String, Object> clause = operations.get(i);
      TopLevelBlockToken tokenType = safeGetTopLevelToken(clause, sheetName);
      switch (tokenType) {
        case GOTO_LABEL:
          if (branchLabelMap.get((String) clause.get(_BRANCH_LABEL)) == null) {
            if (inscreen) {
              throw new OdkXlsValidationException(
                  "Branch label is not defined or not reachable. Labels defined outside a screen cannot be referenced from within a screen. Clause: '"
                      + clause.get(CLAUSE) + "' at row " + clause.get(ROW_NUM_KEY) + " on sheet: "
                      + sheetName);
            } else {
              throw new OdkXlsValidationException(
                  "Branch label is not defined or not reachable. Labels cannot be referenced outside of their enclosing sheet. Clause: '"
                      + clause.get(CLAUSE) + "' at row " + clause.get(ROW_NUM_KEY) + " on sheet: "
                      + sheetName);
            }
          }
          ++i;
          break;
        case ASSIGN:
        case RESUME:
        case BACK_IN_HISTORY:
        case DO_SECTION:
        case EXIT_SECTION:
        case VALIDATE:
        case SAVE_AND_TERMINATE:
        case BEGIN_SCREEN:
          ++i;
          break;
        case BRANCH_LABEL:
        case RENDER:
        case PROMPT:
        case BEGIN_IF:
        case END_SCREEN:
        case ELSE:
        case END_IF:
          throw new OdkXlsValidationException("Internal error. clause: '" + clause.get(CLAUSE)
              + "' at row " + clause.get(ROW_NUM_KEY) + " on sheet: " + sheetName);
        default:
          throw new OdkXlsValidationException("Unrecognized clause: '" + clause.get(CLAUSE)
              + "' on sheet: " + sheetName + " at row " + clause.get(ROW_NUM_KEY));
      }
    }
  };

  /**
   * Verify that all goto_label constructs have a reachable branch label.
   */
  void gatherNestedSections(String sheetName, List<Map<String, Object>> operations,
      Map<String, Boolean> nestedSections, int idx) {
    int i = idx;
    while (i < operations.size()) {
      Map<String, Object> clause = operations.get(i);
      TopLevelBlockToken tokenType = safeGetTopLevelToken(clause, sheetName);
      switch (tokenType) {
        case DO_SECTION:
          nestedSections.put((String) clause.get(_DO_SECTION_NAME), Boolean.TRUE);
          ++i;
          break;
        case ASSIGN:
        case GOTO_LABEL:
        case RESUME:
        case BACK_IN_HISTORY:
        case EXIT_SECTION:
        case VALIDATE:
        case SAVE_AND_TERMINATE:
        case BEGIN_SCREEN:
          ++i;
          break;
        case BRANCH_LABEL:
        case RENDER:
        case PROMPT:
        case BEGIN_IF:
        case END_SCREEN:
        case ELSE:
        case END_IF:
          new OdkXlsValidationException("Internal error. clause: '" + clause.get(CLAUSE)
              + "' at row " + clause.get(ROW_NUM_KEY) + " on sheet: " + sheetName);
          break;
        default:
          new OdkXlsValidationException("Unrecognized clause: '" + clause.get(CLAUSE)
              + "' on sheet: " + sheetName + " at row " + clause.get(ROW_NUM_KEY));
      }
    }
  };

  private void extractBranchLabels(String sheetName, List<Map<String, Object>> operations,
      Map<String, Integer> branchLabelMap, List<Map<String, Object>> flattened, int idx) {
    int i = idx;
    while (i < flattened.size()) {
      Map<String, Object> clause = flattened.get(i);
      TopLevelBlockToken tokenType = safeGetTopLevelToken(clause, sheetName);
      switch (tokenType) {
        case BRANCH_LABEL:
          branchLabelMap.put((String) clause.get(BRANCH_LABEL), operations.size());
          ++i;
          break;
        case ASSIGN:
        case RENDER:
        case GOTO_LABEL:
        case RESUME:
        case BACK_IN_HISTORY:
        case DO_SECTION:
        case EXIT_SECTION:
        case VALIDATE:
        case SAVE_AND_TERMINATE:
        case BEGIN_SCREEN:
          clause.put(OPERATION_IDX, operations.size());
          operations.add(clause);
          ++i;
          break;
        case PROMPT:
        case BEGIN_IF:
        case END_SCREEN:
        case ELSE:
        case END_IF:
          throw new OdkXlsValidationException("Internal error. clause: '" + clause.get(CLAUSE)
              + "' at row " + clause.get(ROW_NUM_KEY) + " on sheet: " + sheetName);
        default:
          throw new OdkXlsValidationException("Unrecognized clause: '" + clause.get(CLAUSE)
              + "' on sheet: " + sheetName + " at row " + clause.get(ROW_NUM_KEY));
      }
    }
  }

  private List<Map<String, Object>> parseControlFlowSection(String sheetName,
      List<Map<String, Object>> sheet) {
    List<Map<String, Object>> flow = new ArrayList<Map<String, Object>>();
    for (Map<String, Object> row : sheet) {
      if (exists(row, BRANCH_LABEL)) {
        Map<String, Object> labelEntry = new LinkedHashMap<String, Object>();
        labelEntry.put(_TOKEN_TYPE, BRANCH_LABEL.getText());
        labelEntry.put(BRANCH_LABEL.getText(), row.get(BRANCH_LABEL.getText()));
        labelEntry.put(ROW_NUM_KEY, row.get(ROW_NUM_KEY));
        flow.add(labelEntry);

      }
      if (exists(row, "clause")) {
        Map<String, Object> clauseEntry = new LinkedHashMap<String, Object>();
        clauseEntry.putAll(row);
        clauseEntry.remove(BRANCH_LABEL.getText());
        clauseEntry.put(_TOKEN_TYPE, CLAUSE);
        String[] parts = parseClause((String) clauseEntry.get(CLAUSE), sheetName,
            getInteger(clauseEntry, ROW_NUM_KEY));
        String first = parts[0];

        if (first.length() >= 2) {
          if (COMMENT_SLASHES.equals(first.substring(0, 2))) {
            first = COMMENT;
          }
        }

        if (exists(row, TYPE) && !COMMENT.equals(first)) {
          throw new OdkXlsValidationException(
              "Exactly one of 'clause' and 'type' may be defined on any given row. Error on sheet: "
                  + sheetName + " on row: " + row.get(ROW_NUM_KEY));
        }

        ClauseToken firstToken = null;
        try {
          firstToken = ClauseToken.fromText(first);
        } catch (IllegalArgumentException e) {
          throw new OdkXlsValidationException("Unrecognized 'clause' expression: " + first
              + " Error on sheet: " + sheetName + " on row: " + row.get(ROW_NUM_KEY));

        } catch (NullPointerException e) {
          throw new OdkXlsValidationException("Null 'clause' expression: " + first
              + " Error on sheet: " + sheetName + " on row: " + row.get(ROW_NUM_KEY));

        }


        switch (firstToken) {
          case BEGIN:
            if (parts.length < 2 || !SCREEN.equals(parts[1]) || parts.length > 4
                || (parts.length == 4 && !COMMENT_SLASHES.equalsIgnoreCase(parts[2]))) {
              throw new OdkXlsValidationException(
                  "Expected 'begin screen [ // <tagname> ]' but found: " + clauseEntry.get(CLAUSE)
                      + " on sheet: " + sheetName + " on row: " + row.get(ROW_NUM_KEY));
            }
            if (exists(row, CONDITION)) {
              throw new OdkXlsValidationException(
                  "'condition' expressions are not allowed on 'begin screen' clauses. Error on sheet: "
                      + sheetName + " on row: " + row.get(ROW_NUM_KEY));
            }
            clauseEntry.put(_TOKEN_TYPE, BEGIN_SCREEN.getText());

            if (parts.length == 4) {
              clauseEntry.put(_TAG_NAME, parts[3]);
            }
            break;
          case END:
            if (parts.length < 2 || (!SCREEN.equals(parts[1]) && !IF.getText().equals(parts[1]))) {
              throw new OdkXlsValidationException(
                  "Expected 'end if' or 'end screen' but found: " + clauseEntry.get(CLAUSE)
                      + " on sheet: " + sheetName + " on row: " + row.get(ROW_NUM_KEY));
            }
            if (parts.length > 4 || (parts.length == 4 && !COMMENT_SLASHES.equals(parts[2]))) {
              throw new OdkXlsValidationException("Expected 'end " + parts[1]
                  + " [ // <tagname> ]' but found: " + clauseEntry.get(CLAUSE) + " on sheet: "
                  + sheetName + " on row: " + row.get(ROW_NUM_KEY));
            }
            if (exists(row, CONDITION)) {
              throw new OdkXlsValidationException("'condition' expressions are not allowed on 'end "
                  + parts[1] + "' clauses. Error on sheet: " + sheetName + " on row: "
                  + row.get(ROW_NUM_KEY));
            }
            clauseEntry.put(_TOKEN_TYPE, "end_" + parts[1]);
            if (parts.length == 4) {
              clauseEntry.put(_TAG_NAME, parts[3]);
            }
            break;
          case IF:
            if (parts.length > 3 || (parts.length >= 2 && !COMMENT_SLASHES.equals(parts[1]))) {
              throw new OdkXlsValidationException(
                  "Expected 'if [ // <tagname> ]' but found: " + clauseEntry.get(CLAUSE)
                      + " on sheet: " + sheetName + " on row: " + row.get(ROW_NUM_KEY));
            }
            if (!exists(row, CONDITION)) {
              throw new OdkXlsValidationException(
                  "'condition' expression is required on 'if' clauses. Error on sheet: " + sheetName
                      + " on row: " + row.get(ROW_NUM_KEY));
            }
            clauseEntry.put(_TOKEN_TYPE, BEGIN_IF.getText());
            if (parts.length == 3) {
              clauseEntry.put(_TAG_NAME, parts[2]);
            }
            break;
          case ELSE:
            if (parts.length > 3 || (parts.length >= 2 && !COMMENT_SLASHES.equals(parts[1]))) {
              throw new OdkXlsValidationException(
                  "Expected 'else [ // <tagname> ]' but found: " + clauseEntry.get(CLAUSE)
                      + " on sheet: " + sheetName + " on row: " + row.get(ROW_NUM_KEY));
            }
            if (exists(row, CONDITION)) {
              throw new OdkXlsValidationException(
                  "'condition' expressions are not allowed on 'else' clauses. Error on sheet: "
                      + sheetName + " on row: " + row.get(ROW_NUM_KEY));
            }
            clauseEntry.put(_TOKEN_TYPE, ClauseToken.ELSE.getText());
            if (parts.length == 3) {
              clauseEntry.put(_TAG_NAME, parts[2]);
            }
            break;
          case GOTO:
            if (parts.length != 2) {
              throw new OdkXlsValidationException(
                  "Expected 'goto <branchlabel>' but found: " + clauseEntry.get(CLAUSE)
                      + " on sheet: " + sheetName + " on row: " + row.get(ROW_NUM_KEY));
            }
            clauseEntry.put(_TOKEN_TYPE, GOTO_LABEL.getText());
            clauseEntry.put(BRANCH_LABEL.getText(), parts[1]);
            break;

          case BACK:
            if (parts.length != 1) {
              throw new OdkXlsValidationException(
                  "Expected 'back' but found: " + clauseEntry.get(CLAUSE) + " on sheet: "
                      + sheetName + " on row: " + row.get(ROW_NUM_KEY));
            }
            if (exists(row, CONDITION)) {
              throw new OdkXlsValidationException(
                  "'condition' expressions are not allowed on 'back' clauses. Error on sheet: "
                      + sheetName + " on row: " + row.get(ROW_NUM_KEY));
            }
            clauseEntry.put(_TOKEN_TYPE, BACK_IN_HISTORY.getText());
            break;
          case RESUME:
            if (parts.length != 1) {
              throw new OdkXlsValidationException(
                  "Expected 'RESUME' but found: " + clauseEntry.get(CLAUSE) + " on sheet: "
                      + sheetName + " on row: " + row.get(ROW_NUM_KEY));
            }
            if (exists(row, CONDITION)) {
              throw new OdkXlsValidationException(
                  "'condition' expressions are not allowed on 'RESUME' clauses. Error on sheet: "
                      + sheetName + " on row: " + row.get(ROW_NUM_KEY));
            }
            clauseEntry.put(_TOKEN_TYPE, RESUME);
            break;
          case DO:
            if (parts.length != 3 || !SECTION.equals(parts[1])) {
              throw new OdkXlsValidationException(
                  "Expected 'do section <sectionname>' but found: " + clauseEntry.get(CLAUSE)
                      + " on sheet: " + sheetName + " on row: " + row.get(ROW_NUM_KEY));
            }
            if (exists(row, CONDITION)) {
              throw new OdkXlsValidationException(
                  "'condition' expressions are not allowed on 'do section' clauses. Error on sheet: "
                      + sheetName + " on row: " + row.get(ROW_NUM_KEY));
            }
            clauseEntry.put(_TOKEN_TYPE, DO_SECTION.getText());
            clauseEntry.put(_DO_SECTION_NAME, parts[2]);
            break;
          case EXIT:
            if (parts.length != 2 || !SECTION.equals(parts[1])) {
              throw new OdkXlsValidationException(
                  "Expected 'exit section' but found: " + clauseEntry.get(CLAUSE) + " on sheet: "
                      + sheetName + " on row: " + row.get(ROW_NUM_KEY));
            }
            if (exists(row, CONDITION)) {
              throw new OdkXlsValidationException(
                  "'condition' expressions are not allowed on 'exit section' clauses. Error on sheet: "
                      + sheetName + " on row: " + row.get(ROW_NUM_KEY));
            }
            clauseEntry.put(_TOKEN_TYPE, EXIT_SECTION.getText());
            break;
          case VALIDATE:
            if (parts.length > 2) {
              throw new OdkXlsValidationException(
                  "Expected 'validate' or 'validate <sweepname>' but found: "
                      + clauseEntry.get(CLAUSE) + " on sheet: " + sheetName + " on row: "
                      + row.get(ROW_NUM_KEY));
            }
            if (exists(row, CONDITION)) {
              throw new OdkXlsValidationException(
                  "'condition' expressions are not allowed on 'validate' clauses. Error on sheet: "
                      + sheetName + " on row: " + row.get(ROW_NUM_KEY));
            }
            clauseEntry.put(_TOKEN_TYPE, "validate");
            if (parts.length == 2) {
              clauseEntry.put(_SWEEP_NAME, parts[1]);
            } else {
              clauseEntry.put(_SWEEP_NAME, FINALIZE);
            }
            break;
          case SAVE:
            if (parts.length != 3 && !AND.equals(parts[1]) && !TERMINATE.equals(parts[2])) {
              throw new OdkXlsValidationException(
                  "Expected 'save and terminate' but found: " + clauseEntry.get(CLAUSE)
                      + " on sheet: " + sheetName + " on row: " + row.get(ROW_NUM_KEY));
            }
            if (!exists(row, CALCULATION)) {
              clauseEntry.put(CALCULATION, Boolean.FALSE);
            }

            clauseEntry.put(_TOKEN_TYPE, SAVE_AND_TERMINATE.getText());
            break;
          case COMMENT:
            // Skip to the next clause entry
            continue;
          default:
            throw new OdkXlsValidationException("Unrecognized clause: '" + clauseEntry.get(CLAUSE)
                + "' on sheet: " + sheetName + " at row " + clauseEntry.get(ROW_NUM_KEY));
        }
        flow.add(clauseEntry);

      } else if (exists(row, TYPE)) {
        Map<String, Object> typeEntry = new LinkedHashMap<String, Object>();
        typeEntry.putAll(row);
        typeEntry.remove(BRANCH_LABEL.getText());
        if (exists(row, CONDITION)) {
          throw new OdkXlsValidationException(
              "'condition' expressions are not allowed on prompts. Error on sheet: " + sheetName
                  + " on row: " + row.get(ROW_NUM_KEY));
        }
        String newType =
            formatType((String) typeEntry.get(TYPE), sheetName, getInteger(typeEntry, ROW_NUM_KEY));

        String[] parts = StringUtils.split(newType, " ");

        String first = parts[0];
        if (ASSIGN.getText().equals(first)) {
          typeEntry.put(_TOKEN_TYPE, ASSIGN.getText());
          if (parts.length >= 2) {
            /* explicit type is specified */
            typeEntry.put(_DATA_TYPE, parts[1]);
          }
          if (!exists(row, NAME)) {
            throw new OdkXlsValidationException(
                "'assign' expressions must specify a field 'name' Error on sheet: " + sheetName
                    + " on row: " + row.get(ROW_NUM_KEY));
          }
          if (!exists(row, CALCULATION)) {
            throw new OdkXlsValidationException(
                "'assign' expressions must specify a 'calculation' expression. Error on sheet: "
                    + sheetName + " on row: " + row.get(ROW_NUM_KEY));
          }
        } else {
          typeEntry.put(_TOKEN_TYPE, PROMPT.getText());
          typeEntry.put(_TYPE, newType);
        }
        flow.add(typeEntry);

      }
    }
    return flow;

  }

  private List<Map<String, Object>> parseTopLevelBlock(String sheetName,
      List<Map<String, Object>> flow) {
    List<Map<String, Object>> blockFlow = new ArrayList<Map<String, Object>>();
    boolean hasContents = false;
    int i = 0;

    while (i < flow.size()) {
      Map<String, Object> clause = flow.get(i);
      TopLevelBlockToken tokenType = safeGetTopLevelToken(clause, sheetName);
      Map<String, Object> psi = null;
      switch (tokenType) {
        case BRANCH_LABEL:
          if (clause.get(BRANCH_LABEL.getText()).equals(_CONTENTS)) {
            hasContents = true;
          }
          blockFlow.add(clause);
          ++i;
          break;
        case ASSIGN:
        case PROMPT:
        case GOTO_LABEL:
        case RESUME:
        case BACK_IN_HISTORY:
        case DO_SECTION:
        case EXIT_SECTION:
        case VALIDATE:
        case SAVE_AND_TERMINATE:
          blockFlow.add(clause);
          ++i;
          break;
        case BEGIN_SCREEN:
          psi = parseScreenBlock(sheetName, flow, i);
          blockFlow.add((Map<String, Object>) psi.get(CLAUSE));
          i = getInteger(psi, "idx");
          break;
        case END_SCREEN:
          throw new OdkXlsValidationException(
              "'end screen' without preceding 'begin screen' clause: '" + clause.get(CLAUSE)
                  + "' at row " + clause.get(ROW_NUM_KEY) + " on sheet: " + sheetName);

        case BEGIN_IF:
          psi = parseTopLevelIfBlock(sheetName, flow, i);
          blockFlow.add((Map<String, Object>) psi.get(CLAUSE));
          i = getInteger(psi, "idx");
          break;
        case ELSE:
          throw new OdkXlsValidationException(
              "'else' without preceding 'if' clause: '" + clause.get(CLAUSE) + "' at row "
                  + clause.get(ROW_NUM_KEY) + " on sheet: " + sheetName);

        case END_IF:
          throw new OdkXlsValidationException(
              "'end if' without preceding 'if' clause: '" + clause.get(CLAUSE) + "' at row "
                  + clause.get(ROW_NUM_KEY) + " on sheet: " + sheetName);

        default:
          throw new OdkXlsValidationException("Unrecognized clause: '" + clause.get(CLAUSE)
              + "' on sheet: " + sheetName + " at row " + clause.get(ROW_NUM_KEY));
      }
    }

    int rowNum;
    if (flow.size() == 0) {
      rowNum = 2;
    } else {
      rowNum = getInteger(flow.get(flow.size() - 1), ROW_NUM_KEY);
    }
    blockFlow.add(getExitEnding(rowNum));

    if (!hasContents) {
      addContentsBranchLabel(rowNum, blockFlow);
    }

    if (INITIAL.getSheetName().equals(sheetName)) {
      addInitialSheetContent(rowNum, blockFlow);
    }

    return blockFlow;

  }

  private Map<String, Object> getExitEnding(Integer rowNum) {
    Map<String, Object> exitEnding = new LinkedHashMap<String, Object>();
    exitEnding.put(_TOKEN_TYPE, EXIT_SECTION.getText());
    exitEnding.put(CLAUSE, EXIT_SECTION.getText());
    exitEnding.put(ROW_NUM_KEY, rowNum);
    return exitEnding;
  }

  private void addContentsBranchLabel(Integer rowNum, List<Map<String, Object>> blockFlow) {
    Map<String, Object> newRow = new LinkedHashMap<String, Object>();
    newRow.put(_TOKEN_TYPE, BRANCH_LABEL.getText());
    newRow.put(BRANCH_LABEL.getText(), _CONTENTS);
    newRow.put(ROW_NUM_KEY, rowNum);
    blockFlow.add(newRow);

    newRow = new LinkedHashMap<String, Object>();
    newRow.put(_TOKEN_TYPE, PROMPT.getText());
    newRow.put(TYPE, CONTENTS);
    newRow.put(ROW_NUM_KEY, rowNum);
    Map<String, Object> newScreen = new LinkedHashMap<String, Object>();
    newScreen.put("hideInBackHistory", Boolean.TRUE);
    newRow.put(SCREEN, newScreen);
    blockFlow.add(newRow);

    newRow = new LinkedHashMap<String, Object>();
    newRow.put(_TOKEN_TYPE, RESUME);
    newRow.put(CLAUSE, RESUME);
    newRow.put(ROW_NUM_KEY, rowNum);
    blockFlow.add(newRow);
  }

  private void addInitialSheetContent(Integer rowNum, List<Map<String, Object>> blockFlow) {
    Map<String, Object> newRow = new LinkedHashMap<String, Object>();
    newRow.put(_TOKEN_TYPE, BRANCH_LABEL.getText());
    newRow.put(BRANCH_LABEL.getText(), _FINALIZE);
    newRow.put(ROW_NUM_KEY, rowNum);
    blockFlow.add(newRow);

    newRow = new LinkedHashMap<String, Object>();
    newRow.put(_TOKEN_TYPE, ClauseToken.VALIDATE.getText());
    newRow.put(CLAUSE, ClauseToken.VALIDATE.getText() + " " + FINALIZE);
    newRow.put(_SWEEP_NAME, FINALIZE);
    newRow.put(ROW_NUM_KEY, rowNum);
    Map<String, Object> newScreen = new LinkedHashMap<String, Object>();
    newScreen.put("hideInBackHistory", Boolean.TRUE);
    newRow.put(SCREEN, newScreen);
    blockFlow.add(newRow);

    newRow = new LinkedHashMap<String, Object>();
    newRow.put(_TOKEN_TYPE, SAVE_AND_TERMINATE.getText());
    newRow.put(CLAUSE, SAVE + " " + AND + " " + TERMINATE);
    newRow.put(CALCULATION, Boolean.TRUE);
    newRow.put(ROW_NUM_KEY, rowNum);
    newScreen = new LinkedHashMap<String, Object>();
    newScreen.put("hideInBackHistory", Boolean.TRUE);
    newRow.put(SCREEN, newScreen);
    blockFlow.add(newRow);

    newRow = new LinkedHashMap<String, Object>();
    newRow.put(_TOKEN_TYPE, RESUME);
    newRow.put(CLAUSE, RESUME);
    newRow.put(ROW_NUM_KEY, rowNum);
    blockFlow.add(newRow);
  }

  private Map<String, Object> parseTopLevelIfBlock(String sheetName, List<Map<String, Object>> flow,
      int idx) {
    int i = idx + 1;
    String tag = (String) flow.get(idx).get(_TAG_NAME);
    boolean thenFlow = false;
    List<Map<String, Object>> blockFlow = new ArrayList<Map<String, Object>>();
    while (i < flow.size()) {

      Map<String, Object> clause = flow.get(i);
      TopLevelBlockToken tokenType = safeGetTopLevelToken(clause, sheetName);
      Map<String, Object> psi = null;
      String endTag = null;
      switch (tokenType) {
        case BRANCH_LABEL:
        case ASSIGN:
        case PROMPT:
        case GOTO_LABEL:
        case RESUME:
        case BACK_IN_HISTORY:
        case DO_SECTION:
        case EXIT_SECTION:
        case VALIDATE:
        case SAVE_AND_TERMINATE:
          blockFlow.add(clause);
          ++i;
          break;
        case BEGIN_SCREEN:
          psi = parseScreenBlock(sheetName, flow, i);
          blockFlow.add((Map<String, Object>) psi.get(CLAUSE));
          i = getInteger(psi, "idx");
          break;
        case END_SCREEN:
          throw new OdkXlsValidationException(
              "'end screen' without preceding 'begin screen' clause: '" + clause.get(CLAUSE)
                  + "' at row " + clause.get(ROW_NUM_KEY) + " on sheet: " + sheetName + " within '"
                  + flow.get(idx).get(CLAUSE) + "' beginning at row "
                  + flow.get(idx).get(ROW_NUM_KEY));
        case BEGIN_IF:
          psi = parseTopLevelIfBlock(sheetName, flow, i);
          blockFlow.add((Map<String, Object>) psi.get(CLAUSE));
          i = getInteger(psi, "idx");

          break;
        case ELSE:
          endTag = (String) flow.get(i).get(_TAG_NAME);
          if (!StringUtils.equals(tag, endTag)) {
            logger.warn("Interesting: tag: " + tag + " endTag " + endTag + " rowNum "
                + flow.get(i).get(ROW_NUM_KEY) + " sheetName " + sheetName);

            throw new OdkXlsValidationException("Mismatched tag on '" + flow.get(i).get(CLAUSE)
                + "' at row " + flow.get(i).get(ROW_NUM_KEY) + ". Should match tag on '"
                + flow.get(idx).get(CLAUSE) + "' at row " + flow.get(idx).get(ROW_NUM_KEY)
                + " on sheet: " + sheetName);
          }
          if (!thenFlow) {
            throw new OdkXlsValidationException(
                "Extraneous else clause: '" + flow.get(i).get(CLAUSE) + "' at row "
                    + flow.get(i).get(ROW_NUM_KEY) + " for '" + flow.get(idx).get(CLAUSE)
                    + "' at row " + flow.get(idx).get(ROW_NUM_KEY) + " on sheet: " + sheetName);
          }
          flow.get(idx).put(_ELSE_CLAUSE, clause);
          flow.get(idx).put(_THEN_BLOCK, blockFlow);
          blockFlow = new ArrayList<Map<String, Object>>();
          thenFlow = false;
          ++i;
          break;
        case END_IF:
          endTag = (String) flow.get(i).get(_TAG_NAME);
          if (!StringUtils.equals(tag, endTag)) {
            throw new OdkXlsValidationException("Mismatched tag on '" + flow.get(i).get(CLAUSE)
                + "' at row " + flow.get(i).get(ROW_NUM_KEY) + ". Should match tag on '"
                + flow.get(idx).get(CLAUSE) + "' at row " + flow.get(idx).get(ROW_NUM_KEY)
                + " on sheet: " + sheetName);
          }
          flow.get(idx).put(_END_IF_CLAUSE, clause);

          if (thenFlow) {
            flow.get(idx).put(_THEN_BLOCK, blockFlow);
          } else {
            flow.get(idx).put(_ELSE_BLOCK, blockFlow);

          }
          return getIdxReturnValue(flow.get(idx), i + 1);

        default:
          throw new OdkXlsValidationException("Unrecognized clause: '" + clause.get(CLAUSE)
              + "' on sheet: " + sheetName + " at row " + clause.get(ROW_NUM_KEY));
      }
    }
    if (tag != null) {
      throw new OdkXlsValidationException(
          "No matching 'end if // " + tag + "' on sheet: " + sheetName + " for '"
              + flow.get(idx).get(CLAUSE) + "' at row " + flow.get(idx).get(ROW_NUM_KEY));
    } else {
      throw new OdkXlsValidationException("No matching 'end if' on sheet: " + sheetName + " for '"
          + flow.get(idx).get(CLAUSE) + "' at row " + flow.get(idx).get(ROW_NUM_KEY));
    }
  };

  private Map<String, Object> parseScreenBlock(String sheetName, List<Map<String, Object>> flow,
      int idx) {
    List<Map<String, Object>> blockFlow = new ArrayList<Map<String, Object>>();
    int i = idx + 1;
    String tag = (String) flow.get(idx).get(_TAG_NAME);
    while (i < flow.size()) {
      Map<String, Object> clause = flow.get(i);
      TopLevelBlockToken tokenType = safeGetTopLevelToken(clause, sheetName);
      switch (tokenType) {
        case ASSIGN:
        case PROMPT:
          blockFlow.add(clause);
          i++;
          break;
        case BRANCH_LABEL:
        case GOTO_LABEL:
        case RESUME:
        case BACK_IN_HISTORY:
        case DO_SECTION:
        case EXIT_SECTION:
        case VALIDATE:
        case SAVE_AND_TERMINATE:
        case BEGIN_SCREEN:
          throw new OdkXlsValidationException("Disallowed clause: '" + clause.get(CLAUSE)
              + "' at row " + clause.get(ROW_NUM_KEY) + " on sheet: " + sheetName + " within '"
              + flow.get(idx).get(CLAUSE) + "' beginning at row " + flow.get(idx).get(ROW_NUM_KEY));
        case ELSE:
          throw new OdkXlsValidationException("'else' without preceding 'if' clause: '"
              + clause.get(CLAUSE) + "' at row " + clause.get(ROW_NUM_KEY) + " on sheet: "
              + sheetName + " within '" + flow.get(idx).get(CLAUSE) + "' beginning at row "
              + flow.get(idx).get(ROW_NUM_KEY));
        case END_IF:
          throw new OdkXlsValidationException("'end if' without preceding 'if' clause: '"
              + clause.get(CLAUSE) + "' at row " + clause.get(ROW_NUM_KEY) + " on sheet: "
              + sheetName + " within '" + flow.get(idx).get(CLAUSE) + "' beginning at row "
              + flow.get(idx).get(ROW_NUM_KEY));
        case BEGIN_IF:
          Map<String, Object> psi = parseScreenIfBlock(sheetName, flow, i);
          blockFlow.add((Map<String, Object>) psi.get(CLAUSE));
          i = getInteger(psi, "idx");
          break;
        case END_SCREEN:
          String endTag = (String) flow.get(i).get(_TAG_NAME);
          if (!StringUtils.equals(tag, endTag)) {
            throw new OdkXlsValidationException("Mismatched tag on '" + flow.get(i).get(CLAUSE)
                + "' at row " + flow.get(i).get(ROW_NUM_KEY) + ". Should match tag on '"
                + flow.get(idx).get(CLAUSE) + "' at row " + flow.get(idx).get(ROW_NUM_KEY)
                + " on sheet: " + sheetName);
          }
          flow.get(idx).put(_END_SCREEN_CLAUSE, clause);
          flow.get(idx).put(_SCREEN_BLOCK, blockFlow);

          return getIdxReturnValue(flow.get(idx), i + 1);
        default:
          throw new OdkXlsValidationException("Unrecognized clause: '" + clause.get(CLAUSE)
              + "' on sheet: " + sheetName + " at row " + clause.get(ROW_NUM_KEY));
      }

    }
    if (tag != null) {
      throw new OdkXlsValidationException(
          "No matching 'end screen // " + tag + "' on sheet: " + sheetName + " for '"
              + flow.get(idx).get(CLAUSE) + "' at row " + flow.get(idx).get(ROW_NUM_KEY));
    } else {
      throw new OdkXlsValidationException("No matching 'end screen' on sheet: " + sheetName
          + " for '" + flow.get(idx).get(CLAUSE) + "' at row " + flow.get(idx).get(ROW_NUM_KEY));
    }
  }

  private Map<String, Object> parseScreenIfBlock(String sheetName, List<Map<String, Object>> flow,
      int idx) {
    int i = idx + 1;
    String tag = (String) flow.get(idx).get(_TAG_NAME);
    boolean thenFlow = false;
    List<Map<String, Object>> blockFlow = new ArrayList<Map<String, Object>>();

    while (i < flow.size()) {
      Map<String, Object> clause = flow.get(i);
      TopLevelBlockToken tokenType = safeGetTopLevelToken(clause, sheetName);

      switch (tokenType) {
        case ASSIGN:
        case PROMPT:
          blockFlow.add(clause);
          ++i;
          break;
        case BRANCH_LABEL:
        case GOTO_LABEL:
        case RESUME:
        case BACK_IN_HISTORY:
        case DO_SECTION:
        case EXIT_SECTION:
        case VALIDATE:
        case SAVE_AND_TERMINATE:
        case BEGIN_SCREEN:
        case END_SCREEN:
          throw new OdkXlsValidationException("Disallowed clause: '" + clause.get(CLAUSE)
              + "' at row " + clause.get(ROW_NUM_KEY) + " on sheet: " + sheetName + " within '"
              + flow.get(idx).get(CLAUSE) + "' beginning at row " + flow.get(idx).get(ROW_NUM_KEY));

        case BEGIN_IF:
          Map<String, Object> psi = parseScreenIfBlock(sheetName, flow, i);
          blockFlow.add((Map<String, Object>) psi.get(CLAUSE));
          i = getInteger(psi, "idx");
          break;
        case ELSE:
          String endTag = (String) flow.get(i).get(_TAG_NAME);
          if (!StringUtils.equals(tag, endTag)) {
            throw new OdkXlsValidationException("Mismatched tag on '" + flow.get(i).get(CLAUSE)
                + "' at row " + flow.get(i).get(ROW_NUM_KEY) + ". Should match tag on '"
                + flow.get(idx).get(CLAUSE) + "' at row " + flow.get(idx).get(ROW_NUM_KEY)
                + " on sheet: " + sheetName);
          }
          if (!thenFlow) {
            throw new OdkXlsValidationException(
                "Extraneous else clause: '" + flow.get(i).get(CLAUSE) + "' at row "
                    + flow.get(i).get(ROW_NUM_KEY) + " for '" + flow.get(idx).get(CLAUSE)
                    + "' at row " + flow.get(idx).get(ROW_NUM_KEY) + " on sheet: " + sheetName);
          }
          flow.get(idx).put(_ELSE_CLAUSE, clause);
          flow.get(idx).put(_THEN_BLOCK, blockFlow);
          blockFlow = new ArrayList<Map<String, Object>>();
          thenFlow = false;
          ++i;
          break;
        case END_IF:
          endTag = (String) flow.get(i).get(_TAG_NAME);
          if (!StringUtils.equals(tag, endTag)) {
            throw new OdkXlsValidationException("Mismatched tag on '" + flow.get(i).get(CLAUSE)
                + "' at row " + flow.get(i).get(ROW_NUM_KEY) + ". Should match tag on '"
                + flow.get(idx).get(CLAUSE) + "' at row " + flow.get(idx).get(ROW_NUM_KEY)
                + " on sheet: " + sheetName);
          }
          flow.get(idx).put(_END_IF_CLAUSE, clause);

          if (thenFlow) {
            flow.get(idx).put(_THEN_BLOCK, blockFlow);
          } else {
            flow.get(idx).put(_ELSE_CLAUSE, blockFlow);
          }
          return getIdxReturnValue(flow.get(idx), i + 1);
        default:
          throw new OdkXlsValidationException("Unrecognized clause: '" + clause.get(CLAUSE)
              + "' on sheet: " + sheetName + " at row " + clause.get(ROW_NUM_KEY));
      }
    }
    if (tag != null) {
      throw new OdkXlsValidationException(
          "No matching 'end if // " + tag + "' on sheet: " + sheetName + " for '"
              + flow.get(idx).get(CLAUSE) + "' at row " + flow.get(idx).get(ROW_NUM_KEY));
    } else {
      throw new OdkXlsValidationException("No matching 'end if' on sheet: " + sheetName + " for '"
          + flow.get(idx).get(CLAUSE) + "' at row " + flow.get(idx).get(ROW_NUM_KEY));
    }
  }


  String constructScreenDefn(String sheetName, List<Map<String, Object>> prompts,
      Map<String, List<Integer>> validationTagMap, List<Map<String, Object>> blockFlow, int idx,
      String enclosingScreenLabel, int rowNum) {
    if (enclosingScreenLabel == null) {
      throw new OdkXlsValidationException(
          "Internal error. No enclosing screen label defined on sheet: " + sheetName + " at row "
              + rowNum);
    }
    String defn = "";
    int i = idx;
    while (blockFlow != null && i < blockFlow.size()) {
      Map<String, Object> clause = blockFlow.get(i);
      TopLevelBlockToken tokenType = safeGetTopLevelToken(clause, sheetName);

      switch (tokenType) {
        case ASSIGN:
          // assign(valueName,value) will save the value into data(valueName)
          // and return the value for use in any enclosing expression.
          // (in this case, there is none). It is exposed via formulaFunctions.
          // The actual write to the database occurs later in processing.
          defn += "assign('" + clause.get(NAME) + "', " + clause.get(CALCULATION) + ");\n";
          ++i;
          break;
        case PROMPT:
          int promptIdx = prompts.size();
          clause.put(_BRANCH_LABEL_ENCLOSING_SCREEN, sheetName + '/' + enclosingScreenLabel);
          clause.put(PROMPT_IDX, promptIdx);
          prompts.add(clause);
          updateValidationTagMap(validationTagMap, promptIdx, clause);

          if (exists(clause, SCREEN)) {
            throw new OdkXlsValidationException("Error in clause: '" + clause.get(CLAUSE)
                + "' at row " + clause.get(ROW_NUM_KEY) + " on sheet: " + sheetName
                + " Prompts nested within begin/end screen directives cannot set 'screen' parameters ");
          }
          defn += "activePromptIndicies.push(" + promptIdx + ");\n";
          ++i;
          break;
        case BEGIN_IF:
          List<Map<String, Object>> thenBlock = (List<Map<String, Object>>) clause.get(_THEN_BLOCK);
          List<Map<String, Object>> elseBlock = (List<Map<String, Object>>) clause.get(_ELSE_BLOCK);

          defn += "if (" + clause.get(CONDITION) + ") {\n";
          defn += constructScreenDefn(sheetName, prompts, validationTagMap, thenBlock, 0,
              enclosingScreenLabel, getInteger(clause, ROW_NUM_KEY));
          defn += "}\n";
          if (elseBlock != null && elseBlock.size() > 0) {
            defn += "else {\n";
            defn += constructScreenDefn(sheetName, prompts, validationTagMap, elseBlock, 0,
                enclosingScreenLabel, getInteger(clause, ROW_NUM_KEY));
            defn += "}\n";
          }
          ++i;
          break;
        case END_SCREEN:
        case ELSE:
        case END_IF:
        case BRANCH_LABEL:
        case GOTO_LABEL:
        case RESUME:
        case BACK_IN_HISTORY:
        case DO_SECTION:
        case EXIT_SECTION:
        case VALIDATE:
        case SAVE_AND_TERMINATE:
          throw new OdkXlsValidationException("Internal error. clause: '" + clause.get(CLAUSE)
              + "' at row " + clause.get(ROW_NUM_KEY) + " on sheet: " + sheetName);
        default:
          throw new OdkXlsValidationException("Unrecognized clause: '" + clause.get(CLAUSE)
              + "' on sheet: " + sheetName + " at row " + clause.get(ROW_NUM_KEY));
      }
    }
    return defn;
  }

  /*
   * Replace if constructs with labels and conditional branches. Move prompts into prompts list and
   * extract the validation tag map.
   */
  private void flattenBlocks(String sheetName, List<Map<String, Object>> prompts,
      Map<String, List<Integer>> validationTagMap, List<Map<String, Object>> flattened,
      List<Map<String, Object>> blockFlow, int idx, Map<String, Map<String, Object>> specSettings) {
    int i = idx;
    while (i < blockFlow.size()) {
      Map<String, Object> clause = blockFlow.get(i);
      TopLevelBlockToken tokenType = safeGetTopLevelToken(clause, sheetName);
      String newScreenLabel = null;
      Map<String, Object> labelEntry = null;
      String defn = null;
      switch (tokenType) {
        case BRANCH_LABEL:
        case ASSIGN:
        case GOTO_LABEL:
        case RESUME:
        case BACK_IN_HISTORY:
        case EXIT_SECTION:
        case VALIDATE:
        case SAVE_AND_TERMINATE:
          flattened.add(clause);
          ++i;
          break;
        case DO_SECTION:
          // create a psuedo-prompt in this section that has a
          // _branch_label_enclosing_screen that references the
          // first prompt within the subsection. This is used
          // by the contents menu item to navigate to the first
          // prompt of that section.
          Map<String, Object> pseudoPrompt = new LinkedHashMap<String, Object>();
          pseudoPrompt.putAll(clause);
          pseudoPrompt.put(_TOKEN_TYPE, PROMPT.getText());
          pseudoPrompt.put(_TYPE, _SECTION);
          pseudoPrompt.put(PROMPT_IDX, prompts.size());
          pseudoPrompt.put(DISPLAY, specSettings.get(clause.get(_DO_SECTION_NAME)).get(DISPLAY));
          // TODO: Transform branch label with Gson?
          pseudoPrompt.put(_BRANCH_LABEL_ENCLOSING_SCREEN, clause.get(_DO_SECTION_NAME).toString());


          prompts.add(pseudoPrompt);
          flattened.add(clause);
          ++i;
          break;
        case PROMPT:
          newScreenLabel = "_screen" + clause.get(ROW_NUM_KEY);

          labelEntry = new LinkedHashMap<String, Object>();
          labelEntry.put(_TOKEN_TYPE, BRANCH_LABEL.getText());
          labelEntry.put(BRANCH_LABEL.getText(), newScreenLabel);
          labelEntry.put(ROW_NUM_KEY, clause.get(ROW_NUM_KEY));


          flattened.add(labelEntry);
          // inform the prompt of the tag for the enclosing screen...
          int promptIdx = prompts.size();
          clause.put(_BRANCH_LABEL_ENCLOSING_SCREEN, sheetName + '/' + newScreenLabel);
          clause.put(PROMPT_IDX, promptIdx);
          prompts.add(clause);
          updateValidationTagMap(validationTagMap, promptIdx, clause);

          defn = "activePromptIndicies.add(" + promptIdx + ");\n";
          defn = "function() {var activePromptIndicies = [];\n" + defn
              + "\nreturn activePromptIndicies;\n}\n";


          Map<String, Object> bsb = new LinkedHashMap<String, Object>();
          bsb.put(CLAUSE, clause.get(CLAUSE));
          bsb.put(ROW_NUM_KEY, clause.get(ROW_NUM_KEY));
          bsb.put(_TOKEN_TYPE, BEGIN_SCREEN.getText());
          bsb.put(_SCREEN_BLOCK, defn);


          if (clause.get(SCREEN) != null) {
            // copy the 'screen' settings into the clause
            bsb.put(SCREEN, clause.get(SCREEN));
            clause.remove(SCREEN);
          }
          flattened.add(bsb);
          ++i;
          break;
        case BEGIN_SCREEN:
          newScreenLabel = "_screen" + clause.get(ROW_NUM_KEY);
          labelEntry = new LinkedHashMap<String, Object>();
          labelEntry.put(_TOKEN_TYPE, BRANCH_LABEL.getText());
          labelEntry.put(BRANCH_LABEL.getText(), newScreenLabel);
          labelEntry.put(ROW_NUM_KEY, clause.get(ROW_NUM_KEY));
          flattened.add(labelEntry);
          // process the screen definition
          List<Map<String, Object>> screenBlock =
              (List<Map<String, Object>>) clause.get(_SCREEN_BLOCK);
          defn = constructScreenDefn(sheetName, prompts, validationTagMap, screenBlock, 0,
              newScreenLabel, getInteger(clause, ROW_NUM_KEY));
          defn = "function() {var activePromptIndicies = [];\n" + defn
              + "\nreturn activePromptIndicies;\n}\n";
          clause.put(_SCREEN_BLOCK, defn);
          flattened.add(clause);
          ++i;
          break;
        case BEGIN_IF:
          Map<String, Object> endIfClause = (Map<String, Object>) clause.get(_END_IF_CLAUSE);
          Map<String, Object> elseClause = (clause.get(_ELSE_CLAUSE) != null)
              ? (Map<String, Object>) clause.get(_ELSE_CLAUSE) : endIfClause;
          List<Map<String, Object>> thenBlock = (List<Map<String, Object>>) clause.get(_THEN_BLOCK);
          List<Map<String, Object>> elseBlock = (List<Map<String, Object>>) clause.get(_ELSE_BLOCK);
          String thenLabel = "_then" + clause.get(ROW_NUM_KEY);
          String endifLabel = "_endif" + endIfClause.get(ROW_NUM_KEY);
          String elseLabel = "_else" + elseClause.get(ROW_NUM_KEY);
          /*
           * transform clause into a simple goto... Preserve the ordering of the then and else
           * blocks (so that prompts remain in-order)
           */
          clause.remove(_ELSE_CLAUSE);
          clause.remove(_END_IF_CLAUSE);
          clause.remove(_THEN_BLOCK);
          clause.remove(_ELSE_BLOCK);
          clause.put(_TOKEN_TYPE, GOTO_LABEL.getText());
          clause.put(_BRANCH_LABEL, thenLabel);
          flattened.add(clause); // goto Then conditionally

          Map<String, Object> goElse = new LinkedHashMap<String, Object>();
          goElse.put(CLAUSE, elseClause.get(CLAUSE));
          goElse.put(_TOKEN_TYPE, GOTO_LABEL.getText());
          goElse.put(BRANCH_LABEL.getText(), elseLabel);
          goElse.put(ROW_NUM_KEY, elseClause.get(ROW_NUM_KEY));

          flattened.add(goElse); // goto Else unconditionally


          labelEntry = new LinkedHashMap<String, Object>();
          labelEntry.put(_TOKEN_TYPE, BRANCH_LABEL.getText());
          labelEntry.put(BRANCH_LABEL.getText(), thenLabel);
          labelEntry.put(ROW_NUM_KEY, clause.get(ROW_NUM_KEY));

          flattened.add(labelEntry); // Then label
          // then block...
          flattenBlocks(sheetName, prompts, validationTagMap, flattened, thenBlock, 0,
              specSettings);

          Map<String, Object> goEndIf = new LinkedHashMap<String, Object>();
          goEndIf.put(CLAUSE, endIfClause.get(CLAUSE));
          goEndIf.put(_TOKEN_TYPE, GOTO_LABEL.getText());
          goEndIf.put(BRANCH_LABEL.getText(), endifLabel);
          goEndIf.put(ROW_NUM_KEY, endIfClause.get(ROW_NUM_KEY));


          flattened.add(goEndIf); // goto EndIf unconditionally

          labelEntry = new LinkedHashMap<String, Object>();
          labelEntry.put(_TOKEN_TYPE, BRANCH_LABEL.getText());
          labelEntry.put(BRANCH_LABEL.getText(), elseLabel);
          labelEntry.put(ROW_NUM_KEY, elseClause.get(ROW_NUM_KEY));

          flattened.add(labelEntry); // Else label

          // else block...
          if (elseBlock != null) {
            flattenBlocks(sheetName, prompts, validationTagMap, flattened, elseBlock, 0,
                specSettings);
          }

          labelEntry = new LinkedHashMap<String, Object>();
          labelEntry.put(_TOKEN_TYPE, BRANCH_LABEL.getText());
          labelEntry.put(BRANCH_LABEL.getText(), endifLabel);
          labelEntry.put(ROW_NUM_KEY, endIfClause.get(ROW_NUM_KEY));

          flattened.add(labelEntry); // EndIf label
          ++i;
          break;
        case END_SCREEN:
        case ELSE:
        case END_IF:
          throw new OdkXlsValidationException("Internal error. clause: '" + clause.get(CLAUSE)
              + "' at row " + clause.get(ROW_NUM_KEY) + " on sheet: " + sheetName);
        default:
          throw new OdkXlsValidationException("Unrecognized clause: '" + clause.get(CLAUSE)
              + "' on sheet: " + sheetName + " at row " + clause.get(ROW_NUM_KEY));
      }
    }
  }

  private Map<String, Object> getIdxReturnValue(Map<String, Object> map, int idx) {
    Map<String, Object> returnValues = new HashMap<String, Object>();
    returnValues.put(CLAUSE, map);
    returnValues.put(IDX, idx);
    return returnValues;
  }

  private int getInteger(Map<String, Object> map, String intField) {
    Object intObject = map.get(intField);
    Integer intInteger = (Integer) intObject;
    return intInteger.intValue();
  }

  private String[] parseClause(String clause, String sheet, int rowNum) {
    if (clause == null) {
      throw new OdkXlsValidationException(
          "Clause body is blank at sheet " + sheet + " row num " + rowNum);
    }
    clause = clause.replace(COMMENT_SLASHES, " // ");
    clause = clause.replaceAll("\\s+", " ");
    clause = clause.trim();
    return StringUtils.split(clause, " ");
  }

  private String formatType(String _type, String sheet, int rowNum) {
    if (_type == null) {
      throw new OdkXlsValidationException(
          "PromptType body is blank at sheet " + sheet + " row num " + rowNum);
    }
    _type = _type.replaceAll("\\s+", " ");
    _type = _type.trim();
    return _type;
  }

  private boolean exists(Map<String, Object> row, String field) {
    return !StringUtils.isEmpty((String) row.get(field));
  }

  private boolean exists(Map<String, Object> row, TopLevelBlockToken field) {
    return exists(row, field.getText());
  }

  private TopLevelBlockToken safeGetTopLevelToken(Map<String, Object> clause, String sheetName) {
    TopLevelBlockToken tokenType = null;
    try {
      tokenType = TopLevelBlockToken.fromText((String) clause.get(_TOKEN_TYPE));
    } catch (IllegalArgumentException e) {
      throw new OdkXlsValidationException(
          "Unrecognized 'clause' expression: " + clause.get(_TOKEN_TYPE) + " Error on sheet: "
              + sheetName + " on row: " + clause.get(ROW_NUM_KEY));

    } catch (NullPointerException e) {
      throw new OdkXlsValidationException("Null 'clause' expression: " + clause.get(_TOKEN_TYPE)
          + " Error on sheet: " + sheetName + " on row: " + clause.get(ROW_NUM_KEY));

    }
    return tokenType;
  }

  /**
   * Split out the tags in validation_tags.
   */
  private void updateValidationTagMap(Map<String, List<Integer>> validationTagMap, int promptIdx,
      Map<String, Object> clause) {
    // we need to add this to the validationTagMap
    String tags = "";
    if (exists(clause, VALIDATION_TAGS)) {
      tags = (String) clause.get(VALIDATION_TAGS);
    }

    tags = tags.replaceAll("\\s+", " ");
    tags = tags.trim();
    String[] parts = StringUtils.split(tags, " ");
    if ("".equals(tags) || parts.length == 0 || (parts.length == 1 && "".equals(parts[0]))) {
      if (exists(clause, REQUIRED) || exists(clause, CONSTRAINT)) {
        List<Integer> finalize = (List<Integer>) validationTagMap.get(_FINALIZE);
        finalize.add(promptIdx);
      }
    } else {

      for (int j = 0; j < parts.length; ++j) {
        if (validationTagMap.get(parts[j]) == null) {

          validationTagMap.put(parts[j], new ArrayList<Integer>());
        }
        validationTagMap.get(parts[j]).add(promptIdx);
      }
    }
  }

}

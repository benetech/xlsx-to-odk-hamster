package org.benetech.xlsxodk.conversion;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.xlsxjson.Xlsx2JsonConverter;
import org.benetech.xlsxjson.Xlsx2JsonConverterBuilder;
import org.benetech.xlsxodk.PredefSheet;
import org.benetech.xlsxodk.util.ConversionUtils;
import org.junit.Test;

public class LocalesTest {

  Log logger = LogFactory.getLog(LocalesTest.class);

  @Test
  public void testSortLocales() throws Exception {

    List<Map<String, Object>> locales = new ArrayList<Map<String, Object>>();
    Map<String, Object> newLocale = new HashMap<String, Object>();
    newLocale.put("name", "French");
    newLocale.put("display", "doesn't matter");
    locales.add(newLocale);

    newLocale = new HashMap<String, Object>();
    newLocale.put("name", "Japanese");
    newLocale.put("display", "doesn't matter");

    locales.add(newLocale);

    newLocale = new HashMap<String, Object>();
    newLocale.put("name", "Spanish");
    newLocale.put("display", "doesn't matter");
    newLocale.put("_row_num", new Integer(2));
    locales.add(newLocale);

    newLocale = new HashMap<String, Object>();
    newLocale.put("name", "Gaelic");
    newLocale.put("display", "doesn't matter");
    newLocale.put("_row_num", new Integer(1));
    locales.add(newLocale);


    logger.info(locales);
    SettingsConverter.sortLocales(locales);

    logger.info(locales);
  }

  @Test
  public void testConstructLocales() throws Exception {
    URL url = this.getClass().getResource("/poverty_stoplight_madison.xlsx");
    File xlsxFile = new File(url.getFile());
    Xlsx2JsonConverterBuilder builder = new Xlsx2JsonConverterBuilder();
    builder.dotsToNested(true);
    Xlsx2JsonConverter converter = builder.build();

    Map<String, List<Map<String, Object>>> javaCollections =
        converter.convertToJavaCollections(xlsxFile);

    SettingsConverter settingsConverter = new SettingsConverter();
    settingsConverter.settingsRowList =
        ConversionUtils.omitRowsWithMissingField(javaCollections.get(PredefSheet.SETTINGS.getSheetName()), SettingsConverter.SETTING_NAME_FIELD);
    logger.info(settingsConverter.settingsRowList);

    settingsConverter.settingsMap =
        ConversionUtils.toMap(settingsConverter.settingsRowList,  SettingsConverter.SETTING_NAME_FIELD);
    logger.info(settingsConverter.settingsMap);
    
    settingsConverter.constructLocaleBlock();

    logger.info(settingsConverter.defaultLocale);
    logger.info(settingsConverter.locales);

    assertThat((String)settingsConverter.defaultLocale.get(SettingsConverter.SETTING_NAME_FIELD), is("_default_locale"));
    assertThat((String)settingsConverter.defaultLocale.get("value"), is("default"));
    
    assertThat((String) settingsConverter.locales.get(SettingsConverter.SETTING_NAME_FIELD), is("_locales"));
    assertTrue(settingsConverter.locales.get("value") instanceof List<?>);
    List<Map<String,Object>> valueList = (List<Map<String, Object>>) settingsConverter.locales.get("value");

    String[] names = {(String)valueList.get(0).get("name"), (String)valueList.get(1).get("name")};
    assertThat(Arrays.asList(names), containsInAnyOrder("default", "spanish"));
  }

}

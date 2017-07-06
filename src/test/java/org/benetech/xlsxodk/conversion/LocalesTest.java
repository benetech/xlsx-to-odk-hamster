package org.benetech.xlsxodk.conversion;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.benetech.xlsxjson.Converter;
import org.benetech.xlsxjson.ConverterBuilder;
import org.benetech.xlsxodk.Xlsx2OdkConverter;
import org.benetech.xlsxodk.Xlsx2OdkConverterBuilder;
import org.benetech.xlsxodk.exception.OdkXlsValidationException;
import org.benetech.xlsxodk.util.ConversionUtils;
import org.benetech.xlsxodk.validation.SettingsValidator;

import com.google.gson.Gson;

import org.junit.*;

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
    ConverterBuilder builder = new ConverterBuilder();
    builder.dotsToNested(true);
    Converter converter = builder.build();

    Map<String, List<Map<String, Object>>> javaCollections =
        converter.convertToJavaCollections(xlsxFile);

    SettingsConverter settingsConverter = new SettingsConverter();
    settingsConverter.settingsRowList =
        ConversionUtils.omitRowsWithMissingField(javaCollections.get("settings"), "setting_name");
    logger.info(settingsConverter.settingsRowList);

    
    settingsConverter.settingsMap =
        SettingsConverter.toSettingsMap(settingsConverter.settingsRowList);
    logger.info(settingsConverter.settingsMap);

    
    settingsConverter.constructLocaleBlock();

    logger.info(settingsConverter.defaultLocale);
    logger.info(settingsConverter.locales);

  }

}

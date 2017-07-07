package org.benetech.xlsxodk;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.xlsxjson.Xlsx2JsonConverter;
import org.junit.Test;

public class FileLoadTest {

  Log logger = LogFactory.getLog(FileLoadTest.class);

  @Test
  public void testLoadFile() throws Exception {
    URL url = this.getClass().getResource("/poverty_stoplight_madison.xlsx");
    File xlsxFile = new File(url.getFile());
    assertTrue(xlsxFile.exists());
  }

  @Test
  public void testReadWorkbook() throws Exception {
    URL url = this.getClass().getResource("/poverty_stoplight_madison.xlsx");
    File xlsxFile = new File(url.getFile());
    Xlsx2OdkConverterBuilder builder = new Xlsx2OdkConverterBuilder();
    builder.dotsToNested(true);
    builder.addRowNums(true);
    Xlsx2OdkConverter converter = builder.build();

    String json = converter.convertToJson(xlsxFile);
    logger.info(json);
  }

  @Test
  public void testLoadConstants() throws Exception {
    logger.info(Constants.DEFAULT_INITIAL);

    assertThat((Integer) (Constants.DEFAULT_INITIAL.get(0).get(Xlsx2JsonConverter.ROW_NUM_KEY)), is(new Integer(2)));

    logger.info(Constants.PROMPT_TYPES);

    Map<String, Object> geopointMap = (Map<String, Object>) Constants.PROMPT_TYPES.get("geopoint");
    assertThat((String)geopointMap.get("type"), is("object"));
  }

}

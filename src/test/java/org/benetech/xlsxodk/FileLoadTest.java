package org.benetech.xlsxodk;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileLoadTest {

  Log logger = LogFactory.getLog(FileLoadTest.class);



  public void testLoadFile() throws Exception {
    URL url = this.getClass().getResource("/poverty_stoplight_madison.xlsx");
    File xlsxFile = new File(url.getFile());
    assertTrue(xlsxFile.exists());
  }

  
  public void testReadWorkbook() throws Exception {
    URL url = this.getClass().getResource("/poverty_stoplight_madison.xlsx");
    File xlsxFile = new File(url.getFile());
    Xlsx2OdkConverterBuilder builder = new Xlsx2OdkConverterBuilder();
    builder.dotsToNested(false);
    Xlsx2OdkConverter converter = builder.build();
    
    String json = converter.convertToJson(xlsxFile);
    logger.info(json);
  }

}

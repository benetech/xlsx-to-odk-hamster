package org.benetech.xlsxodk.conversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.xlsxodk.util.ConversionUtils;
import org.junit.Test;


public class DataModelCreatorTest {

  Log logger = LogFactory.getLog(DataModelCreatorTest.class);

  @Test
  public void testDeepClone() {
    Map<String, Object> myObject = getObject();

    Object newObject = ConversionUtils.deepCopyObject(myObject);
    logger.info(myObject);
    logger.info(newObject);

    Map<String, Object> newMap = (Map<String, Object>) newObject;
    ((Map<String, Object>) newMap.get("child")).put("color", "green");
    ((Map<String, Object>) newMap.get("child")).put("size", "large");
    ((Map<String, Object>) newMap.get("child")).remove("number");
    logger.info(myObject);
    logger.info(newObject);
  }

  @Test
  public void testDeepExtend() {

    Map<String, Object> myObject = getObject();
    Map<String, Object> anotherObject = getAnotherObject();

    logger.info(myObject);
    logger.info(anotherObject);
    Object newObject = ConversionUtils.deepExtendObject(myObject, anotherObject);

    logger.info(newObject);


    myObject = getObject();
    anotherObject = getAnotherObject();

    logger.info(myObject);
    logger.info(anotherObject);
    newObject = ConversionUtils.deepExtendObject(anotherObject, myObject);

    logger.info(newObject);


  }

  private Map<String, Object> getObject() {
    Map<String, Object> myObject = new HashMap<String, Object>();
    Map<String, Object> myChild = new HashMap<String, Object>();
    List<String> myList = new ArrayList<String>();

    myList.add("a");
    myList.add("b");
    myList.add("c");
    myChild.put("list", myList);
    myChild.put("truthiness", Boolean.TRUE);
    myChild.put("number", new Integer(1));
    myChild.put("color", "blue");
    myObject.put("child", myChild);
    myObject.put("simple", "x");
    return myObject;
  }

  private Map<String, Object> getAnotherObject() {
    Map<String, Object> myObject = new HashMap<String, Object>();
    Map<String, Object> myChild = new HashMap<String, Object>();
    List<String> myList = new ArrayList<String>();

    myList.add("c");
    myList.add("d");
    myList.add("e");
    myChild.put("list", myList);
    myChild.put("truthiness", Boolean.FALSE);
    myChild.put("number", new Integer(1));
    myChild.put("anotherNumber", new Integer(2));
    myChild.put("color", "red");
    myObject.put("child", myChild);
    myObject.put("kumquat", "x");

    return myObject;
  }

}

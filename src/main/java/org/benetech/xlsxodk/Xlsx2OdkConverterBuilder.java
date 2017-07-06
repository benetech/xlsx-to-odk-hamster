package org.benetech.xlsxodk;

public class Xlsx2OdkConverterBuilder {
  public boolean dotsToNested = true;
  public boolean addRowNums = true;

  public Xlsx2OdkConverterBuilder() {

  }

  public Xlsx2OdkConverterBuilder dotsToNested(boolean dotsToNested) {
    this.dotsToNested = dotsToNested;
    return this;
  }

 
  public Xlsx2OdkConverterBuilder addRowNums(boolean addRowNums) {
    this.addRowNums = addRowNums;
    return this;
  }

  public Xlsx2OdkConverter build() {
    return new Xlsx2OdkConverter(dotsToNested, addRowNums);

  }

}

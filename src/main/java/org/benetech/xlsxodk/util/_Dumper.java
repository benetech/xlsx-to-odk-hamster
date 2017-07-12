package org.benetech.xlsxodk.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class _Dumper {
  public static void _dump_section(String name, Object object) {
    final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    String output = gson.toJson(object);

    PrintWriter writer;
    try {

      File dumpFile = new File("./target/dump_impl_fragments/" + name + ".json");
      new File(dumpFile.getParent()).mkdirs();
      writer = new PrintWriter(dumpFile, "UTF-8");
      writer.println(output);
      writer.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }


  }
}

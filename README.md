# xlsx-to-odk-hamster
Converts an Excel file to a JSON file to be imported in to ODK 2.0

This was written to act as a Java replacement to the JavaScript file [XLSXConverter2.js](https://github.com/opendatakit/app-designer/blob/master/xlsxconverter/XLSXConverter2.js) 
for the [OpenDataKit 2.0](https://opendatakit.org/)-based [Poverty Stoplight](http://www.fundacionparaguaya.org.py/?page_id=490) project.

This project is dependent on [xlsx-to-json-hamster](https://github.com/benetech/xlsx-to-json-hamster), which does a simple Excel-to-JSON conversion.  This project performs the modifications necessary for OpenDataKit so that the survey can be imported into the [ODK 2.0 "Hamster" Web Service](https://github.com/benetech/odk-hamster).

This project manipulates the structure of the incoming file as Java Collections objects, and exports to JSON using the Google Gson library.


## formDef.json

When it is complete, this project will produce a JSON file in the [formDef.json](https://groups.google.com/d/msg/opendatakit-developers/BsZrj9vm6og/JtpvguPSCQAJ) format for import into ODK Hamster.  This is the same format produced by ODK App Designer, at least as of the [Benetech fork](https://github.com/benetech/app-designer) made in fall 2016.  
    


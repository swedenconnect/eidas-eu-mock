![Logo](images/sweden-connect.png)
# EU eIDAS node configuration builder
building configuration data for eIDAS nodes in a test environment
---

The config builder is built using the command:

> mvn clean install

Building is done using Java 11 or later.

This application is a Spring Boot command line application. Commands are executed using the following syntax:

> java -jar confbuilder.jar [options]

Help is available by running the following command on the compiled .jar file:

> java -jar confbuilder.jar -help


This provides the following output:

```
usage: java -jar confbuilder.jar [options]
 -c <arg>   Configuration properties file with configuration data
 -help      Print this message
 -o <arg>   Target directory where configuration data will be written.
 -p <arg>   Profile identifier defining the function of the builder
            (default CEF26)
 -t <arg>   Directory where template files are located
```

The -p option selects the version of te CEF node for which the configuration data is to be used. Fo version 2.7 versions use the profile "CEF27".

The -c option provides the .properties file that contains the data that determines the content of the fial confguration data being built by this application.

The -t otpion provides the location of the template files used to construct configuration data.

The -o option is the location of the output configuration data being produced. This folder will be deleted and recreated. DO NOT specify a folder with useful or valuable data as the folder will be deleted with all data.

All file names or folders can be specified by absolute or relative path.

# Properties file

The src/main/resources/application-data folder contains the tempaltes for version 2.6.0 as well as 2 example confgituraiton properties files in the conf folder.

These files demonstrates how to specify configuration data and how to specify one ore more key stores  as source of the keys and certificates used by this eIDAS node.

Note that the IdP and SP configuration data can be ignored in most cases as they are only relevant when the SP and IdP from the CEF demo code is used. If not used it is advisable to just keep the parameters in the example.

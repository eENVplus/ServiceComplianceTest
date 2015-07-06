# ServiceComplianceTest
by GraphiTech

This tool is composed by a single Java class, which allows validating the XML response of services developed within the eENVplus project.

To compile this software use Apache Maven, inside project directory type: `mvn`

To run the compiled JAR go inside `target` folder and type: `java -jar ServiceComplianceTest-<version>.jar`

Optional parameter is: `-s "<schema.xsd>"` to force an XSD to be used.
Otherwise the tool retrieves the XSD from the schemaLocation attribute in the XML document.

Mandatory parameter is one of:

- `-f "<file_path>"` to validate an XML on file system.
- `-u "<url>"` to validate the XML response of a service.

This tool requires the [Apache HttpComponents™](https://hc.apache.org/) libraries to perform the HTTP GET requests.

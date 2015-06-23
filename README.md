# ServiceComplianceTest
by GraphiTech

This tool is composed by a single Java class, which allows validating the XML response of services developed within the eENVplus project.

To compile this software use the provided pom.xml with Maven.

To run the compiled JAR type in the terminal: `java -jar ComplianceTest.jar`

Optional parameter is: `-s "<schema.xsd>"` to force an XSD to be used.
Otherwise the tool retrieves the XSD from the schemaLocation attribute in the XML document.

Mandatory parameter is one of:

- `-f "<file_path>"` to validate an XML on file system.
- `-u "<url>"` to validate the XML response of a service.

This tool requires the Apache HttpComponents libraries to perform the HTTP GET requests.

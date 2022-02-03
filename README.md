# MDSL-Web

A Web application that wraps the standalone MDSL tools (CLI). See the [MDSL website](https://microservice-api-patterns.github.io/MDSL-Specification/index) for details on MDSL. The refactorings are described in the [Interface Refactoring Catalog](https://interface-refactoring.github.io/). 

MDSL-Web is Spring Boot application; the project uses Maven.

```
mvn spring-boot:run
```

See [Running your application](https://docs.spring.io/spring-boot/docs/1.5.16.RELEASE/reference/html/using-boot-running-your-application.html) in the Spring documentation for more run instructions.

When started successfully, the application should be available at <http://localhost:8080/>. You may want to verify that it functions correctly:

1. On the "Elicit Requirement" tab, click on "Transform Story to MDSL" (default values will be used if the input forms are not filled out). The story should be diplayed.
2. Click on "Move to API Endpoint Design". The second tab "Refine and Refactor Design" should now be active. 
   * On this tab, you can continue to work with the story   
3. Click on "Refactor/Transform" to create an endpoint type from the story from Steps 1 and 2.
   * Alternatively, you can upload own MDSL files (for instance, one of the MDSL examples from the MDSL GitHub repository) by clicking on the tab name.
   * You can now apply other refactorings (optional).
4. Click on "Move to IDL Generation and Download". The MDSL should still be displayed, and the third tab "Generate Platform IDLs" be highlighted. 
5. Select a target language (default: OpenAPI) and click on "Convert". Information about the various generation options is available [here](https://microservice-api-patterns.github.io/MDSL-Specification/tools).
   * If OpenAPI cannot be generated, you might need an explicit HTTP binding (see [here](https://microservice-api-patterns.github.io/MDSL-Specification/bindings#http-protocol-binding)). 


# License

This project is made available under Apache-2.0 License. See the [LICENSE](./LICENSE) file for the full license. 

# Acknowledgements

This project was supported by the [Hasler Foundation](https://haslerstiftung.ch/en/welcome-to-the-hasler-foundation/).

Portions of the project have been copied from the Baeldung [Java and Spring Tutorials](https://github.com/eugenp/tutorials) and are copyrighted by Eugen Paraschiv under the terms of the MIT license.


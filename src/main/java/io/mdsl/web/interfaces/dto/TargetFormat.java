package io.mdsl.web.interfaces.dto;

public enum TargetFormat {
    OAS("OpenAPI"),
    GRPCPB("Protocol Buffers (gRPC)"),
    GQLSL("GraphQL Schema Language"),
    JOLIE("Jolie"),
    JAVA("Java Modulith (.zip)"),
    MDSL_JSON("MDSL Generator Model (as JSON)"),
    MDSL_YAML("MDSL Generator Model (as YAML file)"),
    MDSL("Microservice Domain-Specific Language"), // TODO do not show in menu
    FMT("Freemarker Text Template"), 
    ANALYTICS("MDSL Analytics"); // could make ALPS, MD report, SampleData explicit options

    private final String displayName;

    TargetFormat(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
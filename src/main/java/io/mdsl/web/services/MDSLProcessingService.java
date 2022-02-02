package io.mdsl.web.services;

import io.mdsl.MDSLResource;
import io.mdsl.generator.*;
import io.mdsl.generator.refactorings.MDSLAnalyticsGenerator;
import io.mdsl.standalone.MDSLStandaloneAPI;
import io.mdsl.standalone.MDSLStandaloneSetup;
import io.mdsl.web.interfaces.dto.TargetFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.zip.ZipOutputStream;

@Service
public class MDSLProcessingService {

    final static Logger logger = LoggerFactory.getLogger(MDSLProcessingService.class);

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private MDSLRefactorer refactorMDSL;

    public String convertToAnalyticsResult(MDSLStandaloneAPI api, Path sourceFilePath, String targetEndpoint) throws IOException {
        File input = loadFileResourceFromPath(sourceFilePath).getFile();
        MDSLResource resource = api.loadMDSL(input);
        MDSLAnalyticsGenerator generator = new MDSLAnalyticsGenerator(targetEndpoint); // removed parameter 2
        return api.callGeneratorInMemory(resource, generator);
    }

    public String convertToJolie(MDSLStandaloneAPI api, Path sourceFilePath) throws IOException {
        File input = loadFileResourceFromPath(sourceFilePath).getFile();
        MDSLResource resource = api.loadMDSL(input);
        JolieGenerator generator = new JolieGenerator();
        return api.callGeneratorInMemory(resource, generator);
    }

    private Resource loadFileResourceFromPath(Path path) {
        return new FileSystemResource(path.toString());
    }

    public String convertToOAS(MDSLStandaloneAPI api, Path sourceFilePath) throws IOException {
        File input = loadFileResourceFromPath(sourceFilePath).getFile();
        MDSLResource resource = api.loadMDSL(input);
        OpenAPIGenerator generator = new OpenAPIGenerator();
        return api.callGeneratorInMemory(resource, generator);
    }

    public String convertToProtocolBuffers(MDSLStandaloneAPI api, Path sourceFilePath) throws IOException {
        File input = loadFileResourceFromPath(sourceFilePath).getFile();
        MDSLResource resource = api.loadMDSL(input);
        ProtocolBuffersGenerator gRPCGenerator = new ProtocolBuffersGenerator();
        return api.callGeneratorInMemory(resource, gRPCGenerator);
    }

    public String convertToTextWithTemplate(MDSLStandaloneAPI api, Path sourceFilePath, Path templateFilePath) throws IOException {
        File input = loadFileResourceFromPath(sourceFilePath).getFile();
        File template = loadFileResourceFromPath(templateFilePath).getFile();

        MDSLResource resource = api.loadMDSL(input);
        TextFileGenerator generator = new TextFileGenerator();

        // TODO could ship ALPS and markdown report .ftl in project directory
        // and provide two more conversion options not requiring template upload

        generator.setFreemarkerTemplateFile(template);
        generator.setTargetFileName(sourceFilePath.getFileName() + ".willnotbeused"); // target file never stored
        return api.callGeneratorInMemory(resource, generator);
    }

    public void convertToJava(MDSLStandaloneAPI api, Path sourceFilePath, String outputDirName) throws IOException {
        File input = loadFileResourceFromPath(sourceFilePath).getFile();
        MDSLResource resource = api.loadMDSL(input);
        JavaGenerator generator = new JavaGenerator();
        api.callGenerator(resource, generator, outputDirName);
    }

    public String convertToGraphSQL(MDSLStandaloneAPI api, Path ipath) throws IOException {
        File input = loadFileResourceFromPath(ipath).getFile();
        MDSLResource resource = api.loadMDSL(input);
        GraphQLGenerator generator = new GraphQLGenerator();
        return api.callGeneratorInMemory(resource, generator);
    }

    private String convertToJSON(MDSLStandaloneAPI api, Path ipath) throws IOException {
        File input = loadFileResourceFromPath(ipath).getFile();
        MDSLResource resource = api.loadMDSL(input);
        GenModelJSONExporter generator = new GenModelJSONExporter();
        return api.callGeneratorInMemory(resource, generator);
    }

    private String convertToYAML(MDSLStandaloneAPI api, Path ipath) throws IOException {
        File input = loadFileResourceFromPath(ipath).getFile();
        MDSLResource resource = api.loadMDSL(input);
        GenModelYAMLExporter generator = new GenModelYAMLExporter();
        return api.callGeneratorInMemory(resource, generator);
    }

    public MDSLConversionResult convert(TargetFormat targetFormat, Path templateFilePath, File tmpFolder, Path inputFilePath) throws IOException {
        var tmpdirName = tmpFolder.getAbsolutePath();
        var api = MDSLStandaloneSetup.getStandaloneAPI();
        var result = new MDSLConversionResult();

        switch (targetFormat) {
            case OAS:
                result.setText(convertToOAS(api, inputFilePath));
                break;
            case GRPCPB:
                result.setText(convertToProtocolBuffers(api, inputFilePath));
                break;
            case JOLIE:
                result.setText(convertToJolie(api, inputFilePath));
                break;
            case GQLSL:
                result.setText(convertToGraphSQL(api, inputFilePath));
                break;
            case JAVA:
                convertToJava(api, inputFilePath, tmpdirName);
                var output = new ByteArrayOutputStream();
                try (var zipOutput = new ZipOutputStream(output)) {
                    Optional<File> optionalFile = Arrays.stream(tmpFolder.listFiles()).filter(File::isDirectory).findFirst();
                    if (optionalFile.isPresent()) {
                        archiveService.zipFile(optionalFile.get(), optionalFile.get().getName(), zipOutput);
                    }
                }
                result.setBinary(output.toByteArray());
                result.setText("Can't display Java source code folder content here, please use the download button below.");
                break;
            case MDSL_JSON:
                result.setText(convertToJSON(api, inputFilePath));
                break;
            case MDSL_YAML:
                result.setText(convertToYAML(api, inputFilePath));
                break;
            case FMT:
                if (templateFilePath != null) {
                    result.setText(convertToTextWithTemplate(api, inputFilePath, templateFilePath));
                } else {
                    throw new IllegalArgumentException("Freemarker template generator needs a template.");
                }
                break;
            // TODO not the right/best place and not fully implemented yet:
            case ANALYTICS:
                result.setText(convertToAnalyticsResult(api, inputFilePath, null));
                break;
        }
        return result;
    }

    public MDSLConversionResult refactor(File tmpFolder, Path inputFilePath, String refactoring, String targetEndpoint, String targetOperation) throws IOException {
        var result = new MDSLConversionResult();
        var api = MDSLStandaloneSetup.getStandaloneAPI();

        if (refactoring.equals("IntroducePagination")) {
            result.setText(refactorMDSL.addPagination(api, inputFilePath, targetEndpoint, targetOperation));
        } else if (refactoring.equals("IntroduceRequestBundle")) {
            result.setText(refactorMDSL.addRequestBundle(api, inputFilePath, targetEndpoint, targetOperation));
        } else if (refactoring.equals("IntroduceWishList")) {
            result.setText(refactorMDSL.addWishList(api, inputFilePath, targetEndpoint, targetOperation));
        } else if (refactoring.equals("AddWishTemplate")) {
            result.setText(refactorMDSL.addWishTemplate(api, inputFilePath, targetEndpoint, targetOperation));
        } else if (refactoring.equals("ExtractInformationHolder")) {
            result.setText(refactorMDSL.extractInformationHolder(api, inputFilePath, targetEndpoint, targetOperation));
        } else if (refactoring.equals("InlineInformationHolder")) {
            result.setText(refactorMDSL.inlineInformationHolder(api, inputFilePath, targetEndpoint, targetOperation));
        } else if (refactoring.equals("AddContextRepresentation")) {
            result.setText(refactorMDSL.addContextRepresentation(api, inputFilePath, targetEndpoint, targetOperation));
        } else if (refactoring.equals("MakeRequestConditional")) {
            result.setText(refactorMDSL.makeRequestConditional(api, inputFilePath, targetEndpoint, targetOperation));
        } else if (refactoring.equals("WrapInParameterTree")) {
            result.setText(refactorMDSL.wrapInParameterTree(api, inputFilePath, targetEndpoint, targetOperation));
        } else if (refactoring.equals("WrapInKeyValueMap")) {
            result.setText(refactorMDSL.wrapInKeyValueMap(api, inputFilePath, targetEndpoint, targetOperation));
        } else if (refactoring.equals("CompleteDataTypes")) {
            result.setText(refactorMDSL.completeDataTypes(api, inputFilePath, targetEndpoint, targetOperation));
        } else if (refactoring.equals("ConvertInlineTypeToTypeReference")) {
            result.setText(refactorMDSL.convertInlineTypeToTypeReference(api, inputFilePath, targetEndpoint, targetOperation));
        } else if (refactoring.equals("AddEventManagementOperationsForReceivedEvents")) {
            result.setText(refactorMDSL.addEventManagementOperations(api, inputFilePath, targetEndpoint));
        } else if (refactoring.equals("MoveOperation")) {
            result.setText(refactorMDSL.moveOperation(api, inputFilePath, targetEndpoint, targetOperation));
        } else if (refactoring.equals("SplitOperation")) {
            result.setText(refactorMDSL.splitOperation(api, inputFilePath, targetEndpoint, targetOperation));
        } else if (refactoring.equals("AddHTTPBinding")) {
            result.setText(refactorMDSL.addHttpBinding(api, inputFilePath, targetEndpoint));
        } else if (refactoring.equals("AddHttpResourceDuringBindingSplit")) {
            result.setText(refactorMDSL.addHttpResourceDuringBindingSplit(api, inputFilePath, targetEndpoint, targetOperation));
        } else if (refactoring.equals("AddURITemplateToExistingHttpResource")) {
            result.setText(refactorMDSL.addURITemplateToExistingHttpResource(api, inputFilePath, targetEndpoint, targetOperation));
        } else if (refactoring.equals("AddHttpResourceForURITemplate")) {
            result.setText(refactorMDSL.addHttpResourceForURITemplate(api, inputFilePath, targetEndpoint, targetOperation));
        } else if (refactoring.equals("SegregateCommandsfromQueries")) {
            result.setText(refactorMDSL.applyCQRS(api, inputFilePath, targetEndpoint));
        } else if (refactoring.equals("ExtractEndpoint")) { 
            result.setText(refactorMDSL.extractEndpoint(api, inputFilePath, targetEndpoint, targetOperation));
        } else if (refactoring.equals("AddMAPDecoratorPR")) {
            result.setText(refactorMDSL.addMAPProcessingResource(api, inputFilePath, targetEndpoint));
        } else if (refactoring.equals("AddMAPDecoratorIHR")) {
            result.setText(refactorMDSL.addMAPInformationHolderResource(api, inputFilePath, targetEndpoint));
        } else if (refactoring.equals("AddMAPDecoratorCR")) {
            result.setText(refactorMDSL.addMAPCollectionResource(api, inputFilePath, targetEndpoint));
        } else if (refactoring.equals("AddOperationsForDecorator")) {
            result.setText(refactorMDSL.addOperationsForDecorator(api, inputFilePath, targetEndpoint));
        } else if (refactoring.equals("AddEndpointForScenario")) {
            result.setText(refactorMDSL.addEndpointForScenario(api, inputFilePath, null, null)); // could pass scenario and story name
        } else if (refactoring.equals("ApplyAll")) {
            result.setText(refactorMDSL.applyAllTransformationsToAllScenarios(api, inputFilePath)); 
        } else {
            logger.error("Unknown refactoring: " + refactoring);
        }
        return result;
    }
}

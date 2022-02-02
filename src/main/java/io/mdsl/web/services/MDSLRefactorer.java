package io.mdsl.web.services;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.generator.model.MDSLGeneratorModel;
import io.mdsl.generator.model.converter.MDSL2GeneratorModelConverter;
import io.mdsl.generator.refactorings.AddEndpointForScenarioRefactoring;
import io.mdsl.generator.refactorings.AddEventManagementRefactoring;
import io.mdsl.generator.refactorings.AddMAPRoleRefactoring;
import io.mdsl.generator.refactorings.AddOperationsForRoleRefactoring;
import io.mdsl.generator.refactorings.AddPaginationRefactoring;
import io.mdsl.generator.refactorings.AddParameterTreeWrapperRefactoring;
import io.mdsl.generator.refactorings.AddKeyValueMapWrapperRefactoring;
import io.mdsl.generator.refactorings.CompleteDataTypesRefactoring;
import io.mdsl.generator.refactorings.ConvertInlinedTypeToTypeReferenceRefactorer;
import io.mdsl.generator.refactorings.ExternalizeContextRepresentationRefactoring;
import io.mdsl.generator.refactorings.MoveOperationRefactoring;
import io.mdsl.generator.refactorings.SplitOperationRefactoring;
import io.mdsl.generator.refactorings.TransformationChainAllInOneRefactoring;
import io.mdsl.generator.refactorings.SeparateCommandsFromQueriesRefactoring;
import io.mdsl.generator.refactorings.AddWishListRefactoring;
import io.mdsl.generator.refactorings.AddWishTemplateRefactoring;
import io.mdsl.generator.refactorings.ExtractInformationHolderRefactoring;
import io.mdsl.generator.refactorings.InlineInformationHolderRefactoring;
import io.mdsl.generator.refactorings.MakeRequestConditionalRefactoring;
import io.mdsl.generator.refactorings.AddRequestBundleRefactoring;
import io.mdsl.generator.refactorings.AddHttpBindingRefactoring;
import io.mdsl.generator.refactorings.AddURITemplateToExistingHttpResourceRefactoring;
import io.mdsl.generator.refactorings.AddHttpResourceDuringBindingSplitRefactoring;
import io.mdsl.generator.refactorings.AddHttpResourceForURITemplateRefactoring;
// difficult: Extract data type definition (target?)
// TODO more MAP decorators, as available in MDSL Core: DTR, LLR, VR, TfR, STATELESS_PR 
// TODO add event management operations (MDSL Core API?)

import io.mdsl.standalone.MDSLStandaloneAPI;

import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;

@Service
public class MDSLRefactorer {

	private static final String RESOURCE_ID = "{resourceId}";
    private static final String STRING_TYPE = "string";
	private static final String SAMPLE_EVENT = "SampleEvent";
	private static final String PERFORMANCE_QUALITY = "performance";

	private File loadFileResourceFromPath(Path path) {
        return new FileSystemResource(path.toString()).getFile();
    }

    // note: could find a simpler way to manipulate MDSL in memory, generated file not really in use yet (in refactorings)

    public String addEndpointForScenario(MDSLStandaloneAPI api, Path inputFilePath, String scenarioName, String storyName) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        ServiceSpecification serviceSpec = resource.getServiceSpecification();
        AddEndpointForScenarioRefactoring generator = new AddEndpointForScenarioRefactoring(getNameOfFirstScenario(serviceSpec), getNameOfFirstStory(serviceSpec));
        return api.callGeneratorInMemory(resource, generator);
    }

    public String addMAPProcessingResource(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        AddMAPRoleRefactoring generator = new AddMAPRoleRefactoring(targetEndpoint, "PROCESSING_RESOURCE");  
        return api.callGeneratorInMemory(resource, generator);
    }

    public String addMAPInformationHolderResource(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        AddMAPRoleRefactoring generator = new AddMAPRoleRefactoring(targetEndpoint, "INFORMATION_HOLDER_RESOURCE"); 
        return api.callGeneratorInMemory(resource, generator);
    }

    public String addMAPCollectionResource(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        AddMAPRoleRefactoring generator = new AddMAPRoleRefactoring(targetEndpoint, "COLLECTION_RESOURCE"); 
        return api.callGeneratorInMemory(resource, generator);
    }
    
 // TODO support all endpoint decorators: DTR, LLR; MDH, ODH, RDH (same as IHR)

    public String addOperationsForDecorator(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        AddOperationsForRoleRefactoring generator = new AddOperationsForRoleRefactoring(targetEndpoint);
        return api.callGeneratorInMemory(resource, generator);
    }

    public String moveOperation(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint, String targetOperation) {
        // temporary solution until issue #15 is resolved: 
    	return extractEndpoint(api, inputFilePath, targetEndpoint, targetOperation);
    }
    
    public String splitOperation(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint, String targetOperation) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        // note: get destination endpoint from UI NYI (existing one), see issue #15
        SplitOperationRefactoring generator = new SplitOperationRefactoring(targetEndpoint, targetOperation);
        return api.callGeneratorInMemory(resource, generator);
    }

    public String extractEndpoint(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint, String targetOperation) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        // note: get destination endpoint from UI NYI (existing one), see issue #15
        MoveOperationRefactoring generator = new MoveOperationRefactoring(targetEndpoint, targetOperation, "ExtractedTargetEndpoint");
        return api.callGeneratorInMemory(resource, generator);
    }

    public String applyCQRS(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        // TODO get destination endpoint from UI too
        SeparateCommandsFromQueriesRefactoring generator = new SeparateCommandsFromQueriesRefactoring(targetEndpoint);
        return api.callGeneratorInMemory(resource, generator);
    }

    public String wrapInParameterTree(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint, String targetOperation) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        AddParameterTreeWrapperRefactoring generator = new AddParameterTreeWrapperRefactoring(targetEndpoint, targetOperation);
        return api.callGeneratorInMemory(resource, generator);
    }

    public String wrapInKeyValueMap(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint, String targetOperation) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        AddKeyValueMapWrapperRefactoring generator = new AddKeyValueMapWrapperRefactoring(targetEndpoint, targetOperation);
        return api.callGeneratorInMemory(resource, generator);
    }

    public String addPagination(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint, String targetOperation) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        AddPaginationRefactoring generator = new AddPaginationRefactoring(targetEndpoint, targetOperation, "pageFromOperation");
        return api.callGeneratorInMemory(resource, generator);
    }

    public String addRequestBundle(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint, String targetOperation) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        AddRequestBundleRefactoring generator = new AddRequestBundleRefactoring(targetEndpoint, targetOperation, true, true);
        return api.callGeneratorInMemory(resource, generator);
    }

    public String addWishList(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint, String targetOperation) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        AddWishListRefactoring generator = new AddWishListRefactoring(targetEndpoint, targetOperation);
        
        // just a demo call:
        // System.out.println("Sample data: " + getSampleData(api, inputFilePath, targetEndpoint, targetOperation));
        
        return api.callGeneratorInMemory(resource, generator);
    }
    
	public String addWishTemplate(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint, String targetOperation) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        AddWishTemplateRefactoring generator = new AddWishTemplateRefactoring(targetEndpoint, targetOperation); 
        return api.callGeneratorInMemory(resource, generator);
	}
    
    public String extractInformationHolder(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint, String targetOperation) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        ExtractInformationHolderRefactoring generator = new ExtractInformationHolderRefactoring(targetEndpoint, targetOperation);
        return api.callGeneratorInMemory(resource, generator);
    }
    
    public String inlineInformationHolder(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint, String targetOperation) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        InlineInformationHolderRefactoring generator = new InlineInformationHolderRefactoring(targetEndpoint, targetOperation);
        return api.callGeneratorInMemory(resource, generator);
    }
    
	public String addContextRepresentation(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint, String targetOperation) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        ExternalizeContextRepresentationRefactoring generator = new ExternalizeContextRepresentationRefactoring(targetEndpoint, targetOperation);
        return api.callGeneratorInMemory(resource, generator);
	}
	
	public String makeRequestConditional(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint, String targetOperation) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
                MakeRequestConditionalRefactoring generator = new MakeRequestConditionalRefactoring(targetEndpoint, targetOperation);
        return api.callGeneratorInMemory(resource, generator);
	}
	
    public String completeDataTypes(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint, String targetOperation) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        // type not coming from UI, see issue #15
        CompleteDataTypesRefactoring generator = new CompleteDataTypesRefactoring(targetEndpoint, targetOperation, STRING_TYPE, true, true); 
        return api.callGeneratorInMemory(resource, generator);
    }
    
	public String convertInlineTypeToTypeReference(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint,
			String targetOperation) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        // type not coming from UI, see issue #15
        ConvertInlinedTypeToTypeReferenceRefactorer generator = new ConvertInlinedTypeToTypeReferenceRefactorer(targetEndpoint, targetOperation, null); 
        return api.callGeneratorInMemory(resource, generator);
	}

	private String getNameOfFirstScenario(ServiceSpecification serviceSpec) {
		if(serviceSpec.getScenarios()==null&&serviceSpec.getScenarios().get(0)==null)
			throw new IllegalArgumentException("No scenario found in MDSL input");
		return serviceSpec.getScenarios().get(0).getName();
	}
	
	private String getNameOfFirstStory(ServiceSpecification serviceSpec) {
		if(serviceSpec.getScenarios()==null&&serviceSpec.getScenarios().get(0)==null)
			throw new IllegalArgumentException("No scenario found in MDSL input");
		if(serviceSpec.getScenarios().get(0).getStories()==null || serviceSpec.getScenarios().get(0).getStories().size()<1)
			throw new IllegalArgumentException("Scenario does not have a story");
		return serviceSpec.getScenarios().get(0).getStories().get(0).getName();
	}
	
	public String addHttpBinding(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        AddHttpBindingRefactoring generator = new AddHttpBindingRefactoring(targetEndpoint);
        return api.callGeneratorInMemory(resource, generator);
    }
	
	public String addHttpResourceForURITemplate(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint, String targetOperation) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        AddHttpResourceForURITemplateRefactoring generator = new AddHttpResourceForURITemplateRefactoring(targetEndpoint, targetOperation, RESOURCE_ID);
        return api.callGeneratorInMemory(resource, generator);
    }

	public String addHttpResourceDuringBindingSplit(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint,
			String targetOperation) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        AddHttpResourceDuringBindingSplitRefactoring generator = new AddHttpResourceDuringBindingSplitRefactoring(targetEndpoint, targetOperation);
        return api.callGeneratorInMemory(resource, generator);
	}

	public String addURITemplateToExistingHttpResource(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint,
			String targetOperation) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        AddURITemplateToExistingHttpResourceRefactoring generator = new AddURITemplateToExistingHttpResourceRefactoring(targetEndpoint, targetOperation, RESOURCE_ID);
        return api.callGeneratorInMemory(resource, generator);
	}

	public String addEventManagementOperations(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        AddEventManagementRefactoring generator = new AddEventManagementRefactoring(targetEndpoint, SAMPLE_EVENT);
        return api.callGeneratorInMemory(resource, generator);
	}

	public String applyAllTransformationsToAllScenarios(MDSLStandaloneAPI api, Path inputFilePath) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        TransformationChainAllInOneRefactoring generator = new TransformationChainAllInOneRefactoring(PERFORMANCE_QUALITY);
        return api.callGeneratorInMemory(resource, generator);
	}
	
	// TODO tbd where to use (tab 2 (popup)? tab 3?)
	public String getSampleData(MDSLStandaloneAPI api, Path inputFilePath, String targetEndpoint, String targetOperation) {
        File input = loadFileResourceFromPath(inputFilePath);
        MDSLResource resource = api.loadMDSL(input);
        // get ServiceSpecification from resource
        ServiceSpecification mdsl = resource.getServiceSpecification();
		MDSL2GeneratorModelConverter converter = new MDSL2GeneratorModelConverter(mdsl);
		MDSLGeneratorModel mdslGenModel = converter.convert();
        return mdslGenModel.getEndpoints().get(0).getOperations().get(0).getRequest().sampleJSON(1); // TODO does this get me sample data?
	}

}

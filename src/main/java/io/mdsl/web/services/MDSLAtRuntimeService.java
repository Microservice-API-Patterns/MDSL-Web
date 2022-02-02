package io.mdsl.web.services; // TODO share with correct package name via Maven Central

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.generator.*;
import io.mdsl.standalone.MDSLStandaloneAPI;
import io.mdsl.standalone.MDSLStandaloneSetup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Service
// This class is not yet in use will be will in future work (on tab 4)
public class MDSLAtRuntimeService {

    final static Logger logger = LoggerFactory.getLogger(MDSLAtRuntimeService.class);

    private Resource loadFileResourceFromPath(Path path) {
        return new FileSystemResource(path.toString());
    }
    
    // TODO catch and handle IOExceptions
    
    public ServiceSpecification getAPIDescription(Path sourceFilePath) throws IOException {
        File input = loadFileResourceFromPath(sourceFilePath).getFile();
        MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
        MDSLResource resource = api.loadMDSL(input); 
        return resource.getServiceSpecification();
    }
    
    public String dumpEndpointContractName(Path sourceFilePath) throws IOException {
        File input = loadFileResourceFromPath(sourceFilePath).getFile();
        MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
        MDSLResource resource = api.loadMDSL(input);
        // do EMF or XText have a pretty print, resource-as-string feature? 
        return resource.getServiceSpecification().getName();
    }

    public String convertToOAS(Path sourceFilePath) throws IOException {
        File input = loadFileResourceFromPath(sourceFilePath).getFile();
        MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
        MDSLResource resource = api.loadMDSL(input);
        OpenAPIGenerator generator = new OpenAPIGenerator();
        return api.callGeneratorInMemory(resource, generator);
    }

	public void generateEndpointFromStory(Path inputFilePath) throws IOException {
        File input = loadFileResourceFromPath(inputFilePath).getFile();
        MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
        MDSLResource resource = api.loadMDSL(input); 
        logger.info("Sucessfully loaded " + resource.getServiceSpecification().getName());
	}
}

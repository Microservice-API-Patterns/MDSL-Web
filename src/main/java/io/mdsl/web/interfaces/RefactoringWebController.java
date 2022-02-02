package io.mdsl.web.interfaces;

import io.mdsl.web.interfaces.dto.TargetFormat;
import io.mdsl.web.services.MDSLConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;


@Controller
public class RefactoringWebController {

	public final static String DEFAULT_MDSL_FILE_NAME = "MDSLWeb-APIDescription.mdsl"; 
    final static Logger logger = LoggerFactory.getLogger(RefactoringWebController.class);

    @Autowired
    private MDSLConversionService mdslConversionService;

    @GetMapping("/refactor")
    public String getRefactor() {
        return "refactor_index";
    }

    @PostMapping("/refactor-from-file")
    public String getRefactoringOptionsFromFile(
            Model model,
            @RequestParam("Source") MultipartFile mdslInputFile) {
        if (mdslInputFile.isEmpty()) {
            throw new IllegalArgumentException("MDSL input file not provided.");
        }
        try {
            logger.info("Preparing refactoring options");
            mdslConversionService.convertUploadedFile(mdslInputFile.getInputStream(), mdslInputFile.getOriginalFilename(), null, TargetFormat.MDSL_JSON, model);
        } catch (Exception e) {
            logger.error("An error occurred reading MDSL: ", e);
            model.addAttribute("report", e.getMessage());
            return "error.html";
        }
        return "refactor_options";
    }

    @PostMapping("/refactor-from-string")
    public String getRefactoringOptionsFromString(
            Model model,
            @RequestParam("Source") String mdslInput,
            @RequestParam(value = "SourceFileName", required = false) String sourceFileName) {
        try {
            logger.info("Preparing refactoring options");
            ByteArrayInputStream mdslInputStream = new ByteArrayInputStream(mdslInput.getBytes(StandardCharsets.UTF_8));
            mdslConversionService.convertUploadedFile(mdslInputStream, getSourceFileName(mdslInput, sourceFileName), null, TargetFormat.MDSL_JSON, model);
        } catch (Exception e) {
            logger.error("An error occurred reading MDSL: ", e);
            model.addAttribute("report", e.getMessage());
            return "error.html";
        }
        return "refactor_options";
    }

    private String getSourceFileName(String mdslInput, String sourceFileName) {
        if (sourceFileName != null) {
            return sourceFileName;
        }
        var matcher = Pattern.compile("API description (.*)").matcher(mdslInput);
        if (matcher.find()) {
            return matcher.group(1) + ".mdsl";
        }
        return DEFAULT_MDSL_FILE_NAME;
    }

    @PostMapping("/refactor/perform")
    public String performRefactoring(
            Model model,
            @RequestParam("TargetEndpoint") String targetEndpoint,
            @RequestParam("TargetOperation") String targetOperation,
            @RequestParam("Refactoring") String refactoring,
            @RequestParam("SourceFileName") String sourceFileName,
            @RequestParam("Source") String mdslInput) {
        try {
            logger.info("Performing refactoring {} on endpoint {} and operation {}", refactoring, targetEndpoint, targetOperation);
            ByteArrayInputStream mdslInputStream = new ByteArrayInputStream(mdslInput.getBytes(StandardCharsets.UTF_8));
            mdslConversionService.refactorUploadedFile(mdslInputStream, sourceFileName, refactoring, targetEndpoint, targetOperation, model);
        } catch (Exception e) {
            logger.error("An error occurred reading MDSL: ", e);
            model.addAttribute("report", e.getMessage());
            return "error.html";
        }
        return "refactor_options";
    }
}
package io.mdsl.web.interfaces;

import io.mdsl.web.config.ConfigProperties;
import io.mdsl.web.interfaces.dto.TargetFormat;
import io.mdsl.web.services.MDSLConversionResult;
import io.mdsl.web.services.MDSLConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Controller
public class TransformationWebController {

    final static Logger logger = LoggerFactory.getLogger(TransformationWebController.class);

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private MDSLConversionService mdslConversionService;

    @GetMapping("/transform")
    public String getTransform() {
        return "transform_index";
    }

    // TOOD other exceptions should be wrapped too

    @PostMapping("/load-and-show-mdsl-from-string")
    public String loadAndShowMdslFromString(
            Model model,
            @RequestParam("Source") String inputFileContent,
            @RequestParam("SourceFileName") String mdslInputFilename) {

        model.addAttribute("mdsl", inputFileContent);
        model.addAttribute("mdslInputFileName", mdslInputFilename);

        return "transform_result.html";
    }

    @PostMapping("/transform-from-string")
    public String convertMDSLFromString(
            Model model,
            @RequestParam("Source") String mdslInput,
            @RequestParam("SourceFileName") String mdslInputFileOriginalFilename,
            @RequestParam(value = "Template", required = false) MultipartFile fmTemplateFile,
            @RequestParam("Target") TargetFormat targetFormat) {

        try {
            logger.info("Converting to target:" + targetFormat);
            ByteArrayInputStream mdslInputStream = new ByteArrayInputStream(mdslInput.getBytes(StandardCharsets.UTF_8));
            var fmTemplate = fmTemplateFile == null || fmTemplateFile.isEmpty() ? null : fmTemplateFile.getInputStream();

            mdslConversionService.convertUploadedFile(mdslInputStream, mdslInputFileOriginalFilename, fmTemplate, targetFormat, model);
        } catch (Exception e) {
            logger.error("An error occurred converting MDSL: ", e);
            model.addAttribute("report", e.getMessage());
            return "error.html";
        }

        return "transform_result.html";
    }

    @PostMapping("/transform-from-file")
    public String convertMDSLFromFile(
            Model model,
            @RequestParam(value = "Source") MultipartFile mdslInputFile,
            @RequestParam(value = "Template", required = false) MultipartFile fmTemplateFile,
            @RequestParam("Target") TargetFormat targetFormat
            ) {
        if (mdslInputFile.isEmpty()) {
            throw new IllegalArgumentException("MDSL input file not provided.");
        }
        try {
            logger.info("Converting to target:" + targetFormat);
            var fmTemplate = fmTemplateFile.isEmpty() ? null : fmTemplateFile.getInputStream();
            mdslConversionService.convertUploadedFile(mdslInputFile.getInputStream(), mdslInputFile.getOriginalFilename(), fmTemplate, targetFormat, model);
        } catch (Exception e) {
            logger.error("An error occurred converting MDSL: ", e);
            model.addAttribute("report", e.getMessage());
            return "error.html";
        }

        return "transform_result.html";
    }

    @PostMapping("/download")
    public ResponseEntity<Object> downloadConvertedMDSL(
            Model model,
            @RequestParam("Source") String mdslInput,
            @RequestParam(value = "FreemarkerTemplate", defaultValue = "") String templateInput,
            @RequestParam("SourceFileName") String mdslInputFileOriginalFilename,
            @RequestParam("Target") TargetFormat targetFormat
            ) {

        MDSLConversionResult conversionResult;

        try {
            logger.info("Converting to target and downloading as: " + targetFormat);
            // We also allow MDSL downloads, in that case, we don't need to do a conversion:
            if (targetFormat == TargetFormat.MDSL) {
                conversionResult = new MDSLConversionResult();
                conversionResult.setText(mdslInput);
            } else {
                ByteArrayInputStream mdslInputStream = new ByteArrayInputStream(mdslInput.getBytes(StandardCharsets.UTF_8));
                ByteArrayInputStream templateInputStream = new ByteArrayInputStream(templateInput.getBytes(StandardCharsets.UTF_8));
                conversionResult = mdslConversionService.convertUploadedFile(mdslInputStream, mdslInputFileOriginalFilename, templateInputStream, targetFormat, model);
            }
        } catch (Exception e) {
            logger.error("An error occurred converting MDSL: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        var responseMimeType = configProperties.getMimeTypeFor(targetFormat);
        var responseSuffix = configProperties.getSuffixFor(targetFormat);

        String responseFileName = getResponseFileName(mdslInputFileOriginalFilename, responseSuffix);
        Object responseBody = targetFormat == TargetFormat.JAVA ? conversionResult.getBinary() : conversionResult.getText();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + responseFileName + "\"")
                .contentType(MediaType.parseMediaType(responseMimeType))
                .body(responseBody);
    }

    private String getResponseFileName(String fileName, String suffix) {
        var suffixPosition = fileName.lastIndexOf(".");
        if (suffixPosition > 0) {
            return fileName.substring(0, suffixPosition) + suffix;
        } else {
            return fileName + "." + suffix;
        }
    }
}
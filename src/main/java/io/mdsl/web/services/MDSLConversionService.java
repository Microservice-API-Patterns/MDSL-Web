package io.mdsl.web.services;

import io.mdsl.exception.MDSLException;
import io.mdsl.web.config.ConfigProperties;
import io.mdsl.web.interfaces.dto.TargetFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class MDSLConversionService {

    final static Logger logger = LoggerFactory.getLogger(MDSLConversionService.class);

    @Autowired
    private MDSLProcessingService converter;

    @Autowired
    private ConfigProperties configProperties;

    public MDSLConversionResult convertUploadedFile(InputStream mdslInput, String mdslInputFilename, InputStream templateInput, TargetFormat targetFormat, Model model) throws IllegalArgumentException, IOException {

        File tmpFolder = null;
        try {
            tmpFolder = Files.createTempDirectory("tmpDirPrefix").toFile();
            String tmpdirName = tmpFolder.getAbsolutePath();
            logger.info("Created temporary directory: " + tmpdirName);
            Path inputFilePath = createTemporaryFile(mdslInput, mdslInputFilename, tmpFolder);

            Path templateFilePath = null;
            String templateFileContent = null;
            if (templateInput != null) {
                templateFilePath = createTemporaryFile(templateInput, "template.ftl", tmpFolder);
                templateFileContent = Files.readString(templateFilePath, StandardCharsets.UTF_8);
            }

            var conversionResult = converter.convert(targetFormat, templateFilePath, tmpFolder, inputFilePath);

            String inputFileContent = Files.readString(inputFilePath, StandardCharsets.UTF_8);
            model.addAttribute("mdsl", inputFileContent);
            model.addAttribute("mdslInputFileName", mdslInputFilename);
            model.addAttribute("target", conversionResult.getText());
            model.addAttribute("targetFormat", targetFormat);
            model.addAttribute("highlight", configProperties.getHighlightFor(targetFormat));
            model.addAttribute("freemarkerTemplate", templateFileContent);

            return conversionResult;
        } catch (MDSLException me) {
            throw new IllegalArgumentException(me.getLocalizedMessage());
        } finally {
            deleteTmpFolder(tmpFolder);
        }
    }

    public MDSLConversionResult refactorUploadedFile(InputStream mdslInput,
                                                     String mdslInputFilename,
                                                     String refactoring, String targetEndpoint, String targetOperation,
                                                     Model model) throws IllegalArgumentException, IOException {
        File tmpFolder = null;
        try {
            tmpFolder = Files.createTempDirectory("tmpDirPrefix").toFile();
            String tmpdirName = tmpFolder.getAbsolutePath();
            logger.info("Created temporary directory: " + tmpdirName);
            Path inputFilePath = createTemporaryFile(mdslInput, mdslInputFilename, tmpFolder);

            MDSLConversionResult refactoringResult = converter.refactor(tmpFolder, inputFilePath, refactoring, targetEndpoint, targetOperation);

            model.addAttribute("mdsl", refactoringResult.getText());
            model.addAttribute("mdslInputFileName", mdslInputFilename);

            // the MDSL-as-json needs to be inserted here, so that we can continue refactoring
            Path refactoredMdslFilePath = createTemporaryFile(new ByteArrayInputStream(refactoringResult.getText().getBytes(StandardCharsets.UTF_8)), mdslInputFilename, tmpFolder);
            var conversionResult = converter.convert(TargetFormat.MDSL_JSON, null, tmpFolder, refactoredMdslFilePath);
            model.addAttribute("target", conversionResult.getText());

            return refactoringResult;
        } catch (MDSLException me) {
            throw new IllegalArgumentException(me.getLocalizedMessage());
        } finally {
            deleteTmpFolder(tmpFolder);
        }
    }

    private void deleteTmpFolder(File tmpFolder) {
        if (tmpFolder != null) {
            logger.info("Deleting temporary directory: " + tmpFolder.getPath());
            var result = FileSystemUtils.deleteRecursively(tmpFolder);
            if (!result) {
                logger.error("Could not delete temporary directory!");
            }
        }
    }

    private Path createTemporaryFile(InputStream inputStream, String fileName, File tmpFolder) throws IOException {
        // save the file on the local file system temporarily (due to EMF/XText limitation)
        String cleanedFileName = StringUtils.cleanPath(fileName);
        Path inputFilePath = Paths.get(tmpFolder.getPath(), cleanedFileName);
        Files.copy(inputStream, inputFilePath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Created temporary file: " + inputFilePath.getFileName());
        return inputFilePath;
    }
}

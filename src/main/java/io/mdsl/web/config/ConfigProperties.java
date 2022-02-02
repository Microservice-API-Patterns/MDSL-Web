package io.mdsl.web.config;

import io.mdsl.web.interfaces.dto.TargetFormat;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@EnableConfigurationProperties(ConfigProperties.class)
@ConfigurationProperties(prefix = "io.mdsl.web")
public class ConfigProperties {

    private String defaultMimeType;
    private Map<String, String> mimeTypes;
    private Map<String, String> fileNameSuffixes;
    private Map<String, String> highlightjs;

    public String getDefaultMimeType() {
        return defaultMimeType;
    }

    public void setDefaultMimeType(String defaultMimeType) {
        this.defaultMimeType = defaultMimeType;
    }

    public Map<String, String> getMimeTypes() {
        return mimeTypes;
    }

    public void setMimeTypes(Map<String, String> mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public String getMimeTypeFor(TargetFormat format) {
        return mimeTypes.getOrDefault(format.name().toLowerCase(), defaultMimeType);
    }

    public String getSuffixFor(TargetFormat format) {
        return fileNameSuffixes.get(format.name().toLowerCase());
    }

    public Map<String, String> getFileNameSuffixes() {
        return fileNameSuffixes;
    }

    public void setFileNameSuffixes(Map<String, String> fileNameSuffixes) {
        this.fileNameSuffixes = fileNameSuffixes;
    }

    public String getHighlightFor(TargetFormat format) {
        return highlightjs.get(format.name().toLowerCase());
    }

    public Map<String, String> getHighlightjs() {
        return highlightjs;
    }

    public void setHighlightjs(Map<String, String> highlightjs) {
        this.highlightjs = highlightjs;
    }
}
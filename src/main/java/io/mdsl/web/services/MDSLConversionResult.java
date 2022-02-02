package io.mdsl.web.services;

public class MDSLConversionResult {
    private String text;
    private byte[] binary;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public byte[] getBinary() {
        return binary;
    }

    public void setBinary(byte[] binary) {
        this.binary = binary;
    }
}

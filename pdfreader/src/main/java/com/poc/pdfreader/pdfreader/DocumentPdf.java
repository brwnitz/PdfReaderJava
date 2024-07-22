package com.poc.pdfreader.pdfreader;

public class DocumentPdf {
    String base64;
    String type;

    public DocumentPdf(String base64, String type) {
        this.base64 = base64;
        this.type = type;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

package com.poc.pdfreader.pdfreader;

public class ResponsePdf {
    String cnpj;
    String qrcode;
    String barcode;


    public ResponsePdf(String cnpj, String qrcode, String barcode) {
        this.cnpj = cnpj;
        this.qrcode = qrcode;
        this.barcode = barcode;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }
}

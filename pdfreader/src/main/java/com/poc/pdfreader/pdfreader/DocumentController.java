package com.poc.pdfreader.pdfreader;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@RestController
public class DocumentController {
    @PostMapping("/document/")
    public ResponseEntity<ResponsePdf> createDocument(@RequestBody DocumentPdf documentPdf) {
        String outputFilePath = "pdfreader/src/main/java/com/poc/pdfreader/pdfreader/PDFBases/decoded.pdf";
        String decodedQrCode = "";
        String barcode = "";
        String textFromImage = "";
        decodeBase64ToPdf(documentPdf.getBase64(), "pdfreader/src/main/java/com/poc/pdfreader/pdfreader/PDFBases/decoded.pdf");
        try {
            if (documentPdf.getType().equals("SESES")){
                extractImage(outputFilePath, "extractedImage",65, 440, 550, 132);
                extractImage(outputFilePath, "qrImage", 1070, 2870, 350, 350);
                decodedQrCode = decodeQrCode(new File("pdfreader/src/main/java/com/poc/pdfreader/pdfreader/PDFBases/qrImage.png"));
                textFromImage = extractTextFromImage("pdfreader/src/main/java/com/poc/pdfreader/pdfreader/PDFBases/extractedImage.png");
                return ResponseEntity.ok(new ResponsePdf(textFromImage, decodedQrCode,""));
            } else if (documentPdf.getType().equals("SANT")) {
                extractImage(outputFilePath, "barCode", 1215, 1990, 1120, 65);
                return ResponseEntity.ok(new ResponsePdf("", "", extractTextFromImage("pdfreader/src/main/java/com/poc/pdfreader/pdfreader/PDFBases/barCode.png")));
            }
        }catch (IOException e){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.badRequest().build();
    }
    public static void decodeBase64ToPdf(String base64, String outputFilePath) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64);
        File outputFile = new File(outputFilePath);
        outputFile.getParentFile().mkdirs();
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            fileOutputStream.write(decodedBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String encodePdfToBas64(String filePath){
        try{
            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @GetMapping("/checkStatus/")
    @ResponseBody
    public String checkServiceStatus() {
        return "Service is up and running!";
    }

    public static String decodeQrCode(File qrImage) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(qrImage);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage)));
        QRCodeReader qrCodeReader = new QRCodeReader();
        try{
            Result result = qrCodeReader.decode(binaryBitmap);
            return result.getText();
        } catch (ChecksumException e) {
            System.out.println("QR Code could not be read because of a ChecksumException");
            throw new RuntimeException(e);
        } catch (NotFoundException e) {
            System.out.println("QR Code could not be read because of a NotFoundException");
            throw new RuntimeException(e);
        } catch (FormatException e) {
            System.out.println("QR Code could not be read because of a FormatException");
            throw new RuntimeException(e);
        }
    }

    public static String extractTextFromPdf(String filePath, int x, int y, int width, int height) {
        String extractedText = "";
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);
            Rectangle rect = new Rectangle(x, y, width, height);
            stripper.addRegion("class1", rect);
            stripper.extractRegions(document.getPage(0));
            extractedText = stripper.getTextForRegion("class1");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return extractedText;
    }

    public static void extractImage(String filePath, String prefix,int x, int y, int width, int height) throws IOException{
        BufferedImage bufferedImage = null;
        try(PDDocument document = PDDocument.load(new File(filePath))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            bufferedImage = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
            bufferedImage = bufferedImage.getSubimage(x, y, width, height);
            File outputfile = new File("pdfreader/src/main/java/com/poc/pdfreader/pdfreader/PDFBases/"+ prefix +".png");
            ImageIO.write(bufferedImage, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String extractTextFromImage(String imagePath){
        ITesseract instance = new Tesseract();
        try {
            // Check if running from a JAR file
            String protocol = DocumentController.class.getResource("").getProtocol();
            if ("jar".equals(protocol)) {
                // Access tessdata as a resource stream and copy to a temporary directory
                String tessDataFolder = "/Tesseract-OCR/tessdata"; // Adjust if necessary
                Path tempDir = Files.createTempDirectory("tessdata");
                // Assuming 'por.traineddata' is the file you need, adjust for any other files
                try (InputStream tessData = DocumentController.class.getResourceAsStream(tessDataFolder + "/por.traineddata")) {
                    if (tessData == null) {
                        throw new FileNotFoundException("Tesseract data file not found in resources.");
                    }
                    Files.copy(tessData, tempDir.resolve("por.traineddata"));
                }
                instance.setDatapath(tempDir.toAbsolutePath().toString());
            } else {
                // For non-JAR execution, adjust the path as needed
                instance.setDatapath("src/main/resources/Tesseract-OCR/tessdata");
            }
        } catch (IOException e) {
            System.err.println("Error setting up Tesseract data: " + e.getMessage());
            return "Error setting up Tesseract data";
        }
        instance.setLanguage("por");

        try {
            return instance.doOCR(new File(imagePath));
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
            return "Error while reading image" + e.getMessage();
        }
    }
}

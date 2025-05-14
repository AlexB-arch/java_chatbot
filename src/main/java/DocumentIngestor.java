import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;

public class DocumentIngestor {
    private final RAGPipeline ragPipeline;

    public DocumentIngestor(RAGPipeline ragPipeline) {
        this.ragPipeline = ragPipeline;
    }

    // Ingests a text file using RAGPipeline
    public void ingestTextFile(String filePath) throws Exception {
        String content = Files.readString(Paths.get(filePath));
        ragPipeline.ingestDocument(content);
    }

    // Ingests a PDF file using RAGPipeline
    public void ingestPdfFile(String pdfPath) throws Exception {
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            ragPipeline.ingestDocument(text);
        }
    }
}


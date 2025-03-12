import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NLPService {
    private SentenceDetectorME sentenceDetector;
    private TokenizerME tokenizer;
    private DocumentCategorizerME categorizer;

    public NLPService() {
        try {
            // initialze basic models
            initializeModels();
        } catch (IOException e) {
            System.err.println("Error initializing NLP models: " + e.getMessage());
        }
    }

    private void initializeModels() throws IOException {
        // Load sentence detector model
        try (InputStream sentenceModelIn = getClass().getResourceAsStream("/models/en-sent.bin")) {
            if (sentenceModelIn != null) {
                SentenceModel sentenceModel = new SentenceModel(sentenceModelIn);
                sentenceDetector = new SentenceDetectorME(sentenceModel);
                
            }
        }

        // Load tokenizer model
    }
}

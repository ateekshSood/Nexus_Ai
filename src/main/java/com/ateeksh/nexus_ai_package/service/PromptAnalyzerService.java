package com.ateeksh.nexus_ai_package.service;

import org.springframework.stereotype.Service;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.CosineSimilarity;

@Service
public class PromptAnalyzerService {
    // model declaration langchain
    private final EmbeddingModel embeddingmodel = new AllMiniLmL6V2EmbeddingModel();
    
    private final String CODE_ANCHOR_TEXT = "Programming , software development,"+
    "coding , debugging , technical computer science concepts , computer science,"+ 
    "code";

    private final String GENERAL_ANCHOR_TEXT = "General conversations , casual,"+
    "chatting , history , daily life , storytelling , and non technical topics";

    private final Embedding codeAnchorEmbedding = embeddingmodel.embed(CODE_ANCHOR_TEXT).content();

    private final Embedding generalAnchorEmbedding = embeddingmodel.embed(GENERAL_ANCHOR_TEXT).content();

    public String analyzePrompt(String prompt){
        
        if (prompt == null || prompt.isEmpty()){
            return "GENERAL";      
        }

        Embedding promptEmbedding = embeddingmodel.embed(prompt).content();

        double similarityToCode = CosineSimilarity.between(promptEmbedding, codeAnchorEmbedding);

        double similarityToGeneral = CosineSimilarity.between(promptEmbedding, generalAnchorEmbedding);

        if(similarityToCode > similarityToGeneral){
            return "CODE";
        }
        else{
            return "GENERAL";
        }
        
    }
}

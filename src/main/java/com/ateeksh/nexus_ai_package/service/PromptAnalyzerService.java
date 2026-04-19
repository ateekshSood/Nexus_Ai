package com.ateeksh.nexus_ai_package.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.CosineSimilarity;
import jakarta.annotation.PostConstruct;

@Slf4j
@Service
public class PromptAnalyzerService {
    // model declaration langchain
    private final EmbeddingModel embeddingmodel = new AllMiniLmL6V2EmbeddingModel();
    
    private static final String CODE_ANCHOR_TEXT = "Programming , software development,"+
    "coding , debugging , technical computer science concepts , computer science,"+ 
    "code";

    private static final String GENERAL_ANCHOR_TEXT = "General conversations , casual,"+
    "chatting , history , daily life , storytelling , and non technical topics";

    private Embedding codeAnchorEmbedding = embeddingmodel.embed(CODE_ANCHOR_TEXT).content();
    private Embedding generalAnchorEmbedding = embeddingmodel.embed(GENERAL_ANCHOR_TEXT).content();

    @PostConstruct
    public void init(){
        try{
            log.info("Loading prompt classifier anchor embeddings");
            codeAnchorEmbedding = embeddingmodel.embed(CODE_ANCHOR_TEXT).content();
            generalAnchorEmbedding = embeddingmodel.embed(GENERAL_ANCHOR_TEXT).content();
            log.info("Anchor embeddings are loaded successfully");
        }
        catch(Exception e){
            log.error("Failed to load anchor embeddings : {}" , e.getMessage() , e);
            throw new IllegalStateException("Prompt analyzer service failed to init" , e);
        }
    }


    public String analyzePrompt(String prompt){
        
        if (prompt == null || prompt.isBlank()){
            return "GENERAL";      
        }

        Embedding promptEmbedding = embeddingmodel.embed(prompt).content();

        double similarityToCode = CosineSimilarity.between(promptEmbedding, codeAnchorEmbedding);

        double similarityToGeneral = CosineSimilarity.between(promptEmbedding, generalAnchorEmbedding);

        log.debug("Prompt similarity — CODE: {:.3f}, GENERAL: {:.3f}", similarityToCode, similarityToGeneral);

        if(similarityToCode > similarityToGeneral){
            return "CODE";
        }
        else{
            return "GENERAL";
        }
        
    }
}

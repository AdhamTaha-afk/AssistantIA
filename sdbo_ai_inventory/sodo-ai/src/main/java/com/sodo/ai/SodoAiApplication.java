package com.sodo.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties
public class SodoAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SodoAiApplication.class, args);
    }
    @Bean
    public com.fasterxml.jackson.databind.ObjectMapper objectMapper(org.springframework.http.converter.json.Jackson2ObjectMapperBuilder jacksonBuilder) {
        com.fasterxml.jackson.databind.ObjectMapper mapper = jacksonBuilder.build();
        mapper.configure(com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
        return mapper;
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public org.springframework.ai.vectorstore.VectorStore vectorStore(org.springframework.ai.embedding.EmbeddingModel embeddingModel) {
        return new org.springframework.ai.vectorstore.SimpleVectorStore(embeddingModel);
    }
}

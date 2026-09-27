package com.sodo.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableConfigurationProperties
public class SodoAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SodoAiApplication.class, args);
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

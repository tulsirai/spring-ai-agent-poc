/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.trai.sai.config;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Tulsi Rai
 */
@Configuration
public class MemoryConfig {

  @Bean
  ChatMemory chatMemory(ChatMemoryRepository repo) {
    // Keep the last 20 messages per conversation
    return MessageWindowChatMemory.builder()
        .chatMemoryRepository(repo)
        .maxMessages(20)
        .build();
  }

  @Bean
  MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
    return MessageChatMemoryAdvisor.builder(chatMemory).build();
  }
}

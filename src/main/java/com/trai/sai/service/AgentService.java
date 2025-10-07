/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.trai.sai.service;

import com.trai.sai.config.ToolConfig;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.stereotype.Service;

/**
 *
 * @author Tulsi Rai
 */
@Service
public class AgentService {

  private final ChatClient chat;

  public AgentService(ChatClient.Builder builder,
                      MessageChatMemoryAdvisor memoryAdvisor,
                      ToolConfig toolbox) { 

    this.chat = builder
        .defaultSystem("""
          You are TRai's AI Agent.
          - Understand the user's intent.
          - If a tool will give a more accurate answer, call it with precise args.
          - When a tool returns data, include returned IDs/URLs in your final answer.
                       
          DELETION POLICY:
          - For delete operations you MUST require explicit confirmation (confirm:true) and a short reason.
          - Include an `actor` field; if not available, use "system".
          - Never attempt to delete SHIPPED or DELIVERED orders; suggest cancel/return instead.
          """)
        .defaultAdvisors(memoryAdvisor)
        .defaultTools(toolbox)   // <-- registers all @Tool methods found on this object
        .build();
  }

  public String step(String sessionId, String userMessage) {
    return chat.prompt()
        .advisors(a -> a.param(
            org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID, sessionId))
        .user(userMessage)
        .call()
        .content();
  }
}

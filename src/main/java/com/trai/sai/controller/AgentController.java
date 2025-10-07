/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.trai.sai.controller;

import com.trai.sai.service.AgentService;
import com.trai.sai.util.GreetingDetector;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Tulsi Rai
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

  private final AgentService agent;
  private final GreetingDetector greetings;

  public AgentController(AgentService agent, GreetingDetector greetings) {
    this.agent = agent;
    this.greetings = greetings;
  }

  public record ChatRequest(String sessionId, String message) {}
  public record ChatResponse(String reply) {}

  @PostMapping("/chat")
  public ChatResponse chat(@RequestBody ChatRequest req) {
    final String msg = req.message() == null ? "" : req.message().trim();
    if (greetings.isGreeting(msg)) {
      // Deterministic greeting — tweak copy here as you like.
      String reply = "Hello! Who do I have the pleasure of speaking with today? "
                   + "How can I assist you today?";
      return new ChatResponse(reply);
    }
    String reply = agent.step(req.sessionId(), msg);
    return new ChatResponse(reply);
  }
}

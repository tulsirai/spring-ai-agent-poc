/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.trai.sai.util;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 *
 * @author Tulsi Rai
 */
@Component
public class GreetingDetector {
  private static final Pattern GREETING = Pattern.compile(
      "^(hi|hello|hey|howdy|yo|good\\s*(morning|afternoon|evening))\\b.*",
      Pattern.CASE_INSENSITIVE
  );

  public boolean isGreeting(String text) {
    if (text == null || text.isBlank()) return false;
    return GREETING.matcher(text.trim()).matches();
  }
}
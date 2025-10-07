/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.trai.sai.config;

import com.trai.sai.domain.Order;
import com.trai.sai.domain.OrderStatus;
import com.trai.sai.repository.OrderJpaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Tulsi Rai
 */
@Configuration
public class SeedOrdersConfig {
  @Bean(name = "orderDataSeeder")
  CommandLineRunner seedOrders(OrderJpaRepository jpa) {
    return args -> {
      if (!jpa.existsById("12345")) jpa.save(new Order("12345", "tulsi", OrderStatus.SHIPPED));
      if (!jpa.existsById("A-001"))  jpa.save(new Order("A-001", "acme",  OrderStatus.PROCESSING));
    };
  }
}

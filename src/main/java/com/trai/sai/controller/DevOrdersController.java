/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.trai.sai.controller;

import com.trai.sai.domain.Order;
import com.trai.sai.repository.OrderJpaRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Tulsi Rai
 */
@RestController
public class DevOrdersController {
  private final OrderJpaRepository jpa;
  public DevOrdersController(OrderJpaRepository jpa) { this.jpa = jpa; }

  @GetMapping("/dev/orders")
  public List<Order> all() { return jpa.findAll(); }
}

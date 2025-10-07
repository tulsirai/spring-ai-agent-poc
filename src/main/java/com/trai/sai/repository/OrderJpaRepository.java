/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.trai.sai.repository;

import com.trai.sai.domain.Order;
import com.trai.sai.domain.OrderStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Tulsi Rai
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<Order, String> {

  @Override
  long count();

  long countByCustomerId(String customerId);

  List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);

  List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
}

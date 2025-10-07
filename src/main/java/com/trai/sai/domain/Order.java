/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.trai.sai.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

/**
 *
 * @author Tulsi Rai
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "orders",
        indexes = {
          @Index(name = "idx_orders_customer", columnList = "customerId"),
          @Index(name = "idx_orders_status", columnList = "status")
        })
public class Order {

  @Id @EqualsAndHashCode.Include
  private String id;

  @Column(nullable = false)
  private String customerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  // --- soft delete metadata (nullable) ---
  private Instant deletedAt;
  private String  deletedBy;
  @Column(length = 500)
  private String  deleteReason;

  // --- optimistic locking ---
  @Version
  private Long version;

  public Order() {
  }

  public Order(String id, String customerId, OrderStatus status) {
    this.id = id;
    this.customerId = customerId;
    this.status = status;
  }
}

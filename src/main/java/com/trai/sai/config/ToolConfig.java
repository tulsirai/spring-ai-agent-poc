/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.trai.sai.config;

import com.trai.sai.domain.Order;
import com.trai.sai.domain.OrderStatus;
import com.trai.sai.repository.OrderJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 *
 * @author Tulsi Rai
 */
@Component
public class ToolConfig {
  private final OrderJpaRepository orderJPARepo;

  public ToolConfig(OrderJpaRepository jpa) {
    this.orderJPARepo = jpa;
  }

  // ===== DTOs returned to the model =====
  public record CreateOrderRequest(String customerId, String status, String orderId) {}
  public record CreateOrderResponse(String orderId, String customerId, String status, boolean created) {}

  public record OrderRequest(String orderId) {}
  public record OrderStatusDTO(String orderId, String customerId, String status) {}

  public record CustomerOrdersRequest(String customerId) {}
  public record OrderSummary(String orderId, String customerId, String status) {}
  public record CustomerOrdersResponse(String customerId, int count, List<OrderSummary> orders) {}

  public record CountResponse(long total) {}

  public record StatusQuery(String status) {}
  public record OrdersByStatusResponse(String status, int count, List<OrderSummary> orders) {}
  
  public record DeleteOrderRequest(String orderId, String reason, String actor, boolean confirm) {}
  public record DeleteOrderResult(String orderId, boolean deleted, String message, String status) {}

  // ===== Helpers =====
  private static OrderStatus parseStatus(String s) {
    if (s == null) throw new IllegalArgumentException("status is required");
    return OrderStatus.valueOf(s.trim().toUpperCase());
  }
  private static OrderSummary toSummary(Order o) {
    return new OrderSummary(o.getId(), o.getCustomerId(), o.getStatus().name());
  }
  
  private static boolean deletable(OrderStatus s) {
    return s == OrderStatus.NEW || s == OrderStatus.PROCESSING || s == OrderStatus.CANCELLED || s == OrderStatus.BACKORDERED;
  }

  // ===== Tools =====

  @Tool(name = "create_order", description = """
      Create (or upsert) an order. Required: customerId, status (NEW|PROCESSING|SHIPPED|DELIVERED|CANCELLED|BACKORDERED).
      orderId is optional: if missing, generate one.
      """)
  public CreateOrderResponse createOrder(CreateOrderRequest req) {
    String orderId = (req.orderId() == null || req.orderId().isBlank())
        ? "O-" + UUID.randomUUID()
        : req.orderId().trim();

    boolean created = !orderJPARepo.existsById(orderId);
    var status = parseStatus(req.status());
    Order o = new Order(orderId, req.customerId().trim(), status);
    orderJPARepo.save(o);
    return new CreateOrderResponse(orderId, o.getCustomerId(), o.getStatus().name(), created);
    // Note: if order already exists, this is an update (created=false).
  }

  @Tool(name = "get_order_status", description = "Return order status and owner by orderId")
  public OrderStatusDTO getOrderStatus(OrderRequest req) {
    return orderJPARepo.findById(req.orderId().trim())
        .map(o -> new OrderStatusDTO(o.getId(), o.getCustomerId(), o.getStatus().name()))
        .orElse(new OrderStatusDTO(req.orderId().trim(), null, "UNKNOWN"));
  }

  @Tool(name = "orders_for_customer", description = "List orders for a given customerId")
  public CustomerOrdersResponse ordersForCustomer(CustomerOrdersRequest req) {
    var list = orderJPARepo.findByCustomerIdOrderByCreatedAtDesc(req.customerId().trim()).stream()
        .map(ToolConfig::toSummary).toList();
    return new CustomerOrdersResponse(req.customerId(), list.size(), list);
  }

  @Tool(name = "count_orders", description = "Return total number of orders in the system")
  public CountResponse countOrders() {
    return new CountResponse(orderJPARepo.count());
  }

  @Tool(name = "orders_by_status", description = "List orders by status (NEW|PROCESSING|SHIPPED|DELIVERED|CANCELLED|BACKORDERED)")
  public OrdersByStatusResponse ordersByStatus(StatusQuery query) {
    var status = parseStatus(query.status());
    var list = orderJPARepo.findByStatusOrderByCreatedAtDesc(status).stream()
        .map(ToolConfig::toSummary).toList();
    return new OrdersByStatusResponse(status.name(), list.size(), list);
  }
  
  @Tool(name = "delete_order", description = """
      Soft-delete an order. Requires explicit confirmation and a short reason.
      Allowed current statuses: NEW, PROCESSING, CANCELLED, BACKORDERED.
      Blocks SHIPPED and DELIVERED. Idempotent if already DELETED.
      """)
  public DeleteOrderResult deleteOrder(DeleteOrderRequest req) {
    String id = req.orderId() == null ? "" : req.orderId().trim();
    if (id.isBlank()) {
      return new DeleteOrderResult(null, false, "orderId is required", "UNKNOWN");
    }
    if (!req.confirm()) {
      return new DeleteOrderResult(id, false,
          "Confirmation required. Re-issue with confirm:true and a reason, e.g., " +
          "delete order " + id + " confirm:true reason:'duplicate entry'", "PENDING_CONFIRMATION");
    }
    if (req.reason() == null || req.reason().isBlank()) {
      return new DeleteOrderResult(id, false, "Deletion reason is required.", "REJECTED");
    }

    return orderJPARepo.findById(id).map(o -> {
      if (o.getStatus() == OrderStatus.DELETED) {
        return new DeleteOrderResult(o.getId(), true, "Order already deleted (idempotent).", o.getStatus().name());
      }
      if (o.getStatus() == OrderStatus.SHIPPED || o.getStatus() == OrderStatus.DELIVERED) {
        return new DeleteOrderResult(o.getId(), false,
            "Deletion blocked for " + o.getStatus() + ". Use cancel/return workflow.", o.getStatus().name());
      }
      if (!deletable(o.getStatus())) {
        return new DeleteOrderResult(o.getId(), false,
            "Deletion not allowed from status " + o.getStatus(), o.getStatus().name());
      }

      o.setStatus(OrderStatus.DELETED);
      o.setDeletedAt(Instant.now());
      o.setDeletedBy(req.actor() == null ? "unknown" : req.actor().trim());
      o.setDeleteReason(req.reason().trim());
      orderJPARepo.save(o);

      // (optional) emit an audit event here
      return new DeleteOrderResult(o.getId(), true, "Order deleted successfully.", o.getStatus().name());
    }).orElseGet(() ->
        new DeleteOrderResult(id, false, "Order not found.", "UNKNOWN")
    );
  }
}

package com.wfsample.delivery;

import com.google.common.collect.ImmutableMap;
import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.service.DeliveryApi;
import com.wfsample.service.NotificationApi;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.log.Fields;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.core.Response;

/**
 * Controller for delivery service which is responsible for dispatching shirts returning tracking
 * number for a given order.
 *
 * @author Hao Song (songhao@vmware.com).
 */
public class DeliveryController implements DeliveryApi {
  @Autowired
  private NotificationApi notificationApi;
  private final AtomicInteger tracking = new AtomicInteger(0);
  private final AtomicInteger dispatch = new AtomicInteger(0);
  private final AtomicInteger cancel = new AtomicInteger(0);
  private final Tracer tracer;

  public DeliveryController(Tracer tracer) {
    this.tracer = tracer;
  }

  @Override
  public Response dispatch(String orderNum, PackedShirtsDTO packedShirts) {
    if (dispatch.incrementAndGet() % 20 == 0) {
      Span span = tracer.activeSpan();
      if (span != null) {
        span.log(ImmutableMap.of(Fields.ERROR_KIND, "no shirts to deliver", "orderNum", orderNum));
      }
      return Response.status(Response.Status.BAD_REQUEST).entity(
          new DeliveryStatusDTO(null, "no shirts to deliver")).build();
    }
    try {
      Thread.sleep(70);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    String trackingNum = UUID.randomUUID().toString();
    System.out.println("Tracking number of Order:" + orderNum + " is " + trackingNum);
    notificationApi.notify(trackingNum);
    return Response.ok(new DeliveryStatusDTO(trackingNum, "shirts delivery dispatched")).build();
  }

  @Override
  public Response trackOrder(String orderNum) {
    if (tracking.incrementAndGet() % 8 == 0) {
      Span span = tracer.activeSpan();
      if (span != null) {
        span.log(ImmutableMap.of(Fields.ERROR_KIND, "order number not found", "orderNum",
            orderNum));
      }
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    try {
      Thread.sleep(30);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return Response.ok().build();
  }

  @Override
  public Response cancelOrder(String orderNum) {
    try {
      Thread.sleep(45);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    if (cancel.incrementAndGet() % 7 == 0) {
      Span span = tracer.activeSpan();
      if (span != null) {
        span.log(ImmutableMap.of(Fields.ERROR_KIND, "order has already been cancelled", "orderNum",
            orderNum));
      }
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    return Response.ok().build();
  }
}
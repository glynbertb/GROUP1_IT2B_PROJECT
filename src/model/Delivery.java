package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Delivery {
    private final int id;
    private final int subscriptionId;
    private final int customerId;
    private Integer driverId;
    private LocalDate scheduledDate;
    private DeliveryStatus status;
    private final List<Integer> containerIds = new ArrayList<>();

    public Delivery(int id, int subscriptionId, int customerId, LocalDate scheduledDate) {
        this.id = id;
        this.subscriptionId = subscriptionId;
        this.customerId = customerId;
        this.scheduledDate = scheduledDate;
        this.status = DeliveryStatus.SCHEDULED;
    }

    public int getId() { return id; }
    public int getSubscriptionId() { return subscriptionId; }
    public int getCustomerId() { return customerId; }
    public Integer getDriverId() { return driverId; }
    public void setDriverId(Integer driverId) { this.driverId = driverId; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }
    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
    public List<Integer> getContainerIds() { return containerIds; }
}

package model;

import java.time.LocalDate;

public class Subscription {
    private final int id;
    private final int customerId;
    private int planId;
    private int quantity;
    private final LocalDate startDate;
    private SubscriptionStatus status;
    private double totalAmount;
    private double amountPaid;

    public Subscription(int id, int customerId, int planId, int quantity, double totalAmount) {
        this.id = id;
        this.customerId = customerId;
        this.planId = planId;
        this.quantity = quantity;
        this.startDate = LocalDate.now();
        this.status = SubscriptionStatus.ACTIVE;
        this.totalAmount = totalAmount;
        this.amountPaid = 0.0;
    }

    public int getId() { return id; }
    public int getCustomerId() { return customerId; }
    public int getPlanId() { return planId; }
    public void setPlanId(int planId) { this.planId = planId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public LocalDate getStartDate() { return startDate; }
    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public double getAmountPaid() { return amountPaid; }
    public void addPayment(double amount) { this.amountPaid += amount; }

    public double getRemainingBalance() {
        return Math.max(0.0, totalAmount - amountPaid);
    }
}

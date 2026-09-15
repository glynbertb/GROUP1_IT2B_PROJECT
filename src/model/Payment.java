package model;

import java.time.LocalDate;

public class Payment {
    private final int id;
    private final int subscriptionId;
    private final double amount;
    private final LocalDate date;
    private final String method;

    public Payment(int id, int subscriptionId, double amount, String method) {
        this.id = id;
        this.subscriptionId = subscriptionId;
        this.amount = amount;
        this.date = LocalDate.now();
        this.method = method;
    }

    public int getId() { return id; }
    public int getSubscriptionId() { return subscriptionId; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getMethod() { return method; }
}
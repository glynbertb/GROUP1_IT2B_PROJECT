package model;

public class SubscriptionPlan {
    private final int id;
    private String name;
    private String description;
    private double unitPrice;
    private String billingCycle;

    public SubscriptionPlan(int id, String name, String description, double unitPrice, String billingCycle) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.unitPrice = unitPrice;
        this.billingCycle = billingCycle;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }

    @Override
    public String toString() {
        return String.format("[P%03d] %-15s | P%-8.2f/%s | %s",
                id, name, unitPrice, billingCycle, description);
    }
}

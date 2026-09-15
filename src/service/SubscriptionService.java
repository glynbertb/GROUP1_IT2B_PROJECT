package service;

import model.Subscription;
import model.SubscriptionPlan;
import model.SubscriptionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubscriptionService {
    private final AppContext ctx;

    public SubscriptionService(AppContext ctx) {
        this.ctx = ctx;
    }

    // ---- Plan CRUD ----
    public SubscriptionPlan createPlan(String name, String desc, double price, String cycle) throws Exception {
        validatePlan(name, desc, price, cycle, -1);
        SubscriptionPlan p = new SubscriptionPlan(ctx.planSeq++, name.trim(), desc.trim(), price, cycle.trim().toUpperCase());
        ctx.plans.add(p);
        return p;
    }

    public SubscriptionPlan updatePlan(int id, String name, String desc, double price, String cycle) throws Exception {
        SubscriptionPlan p = findPlan(id).orElseThrow(() -> new Exception("Plan not found (ID " + id + ")."));
        validatePlan(name, desc, price, cycle, id);
        p.setName(name.trim());
        p.setDescription(desc.trim());
        p.setUnitPrice(price);
        p.setBillingCycle(cycle.trim().toUpperCase());
        return p;
    }

    public void deletePlan(int id) throws Exception {
        SubscriptionPlan p = findPlan(id).orElseThrow(() -> new Exception("Plan not found (ID " + id + ")."));
        boolean inUse = ctx.subscriptions.stream().anyMatch(s -> s.getPlanId() == id);
        if (inUse) throw new Exception("Cannot delete: plan is used by subscription(s).");
        ctx.plans.remove(p);
    }

    private void validatePlan(String name, String desc, double price, String cycle, int excludeId) throws Exception {
        if (name == null || name.trim().length() < 2 || name.trim().length() > 60)
            throw new Exception("Plan name must be 2-60 characters.");
        if (desc == null || desc.trim().length() < 3 || desc.trim().length() > 150)
            throw new Exception("Description must be 3-150 characters.");
        if (price <= 0 || price > 1_000_000)
            throw new Exception("Unit price must be between 0.01 and 1000000.");
        String c = cycle == null ? "" : cycle.trim().toUpperCase();
        if (!c.equals("WEEKLY") && !c.equals("MONTHLY"))
            throw new Exception("Billing cycle must be WEEKLY or MONTHLY.");
        for (SubscriptionPlan p : ctx.plans) {
            if (p.getId() == excludeId) continue;
            if (p.getName().equalsIgnoreCase(name.trim()))
                throw new Exception("Plan name already exists (P" + String.format("%03d", p.getId()) + ").");
        }
    }

    public List<SubscriptionPlan> allPlans() {
        return ctx.plans;
    }

    public Optional<SubscriptionPlan> findPlan(int id) {
        return ctx.plans.stream().filter(p -> p.getId() == id).findFirst();
    }

    // ---- Subscription CRUD ----
    public Subscription createSubscription(int customerId, int planId, int quantity) throws Exception {
        ctx.customers.stream().filter(c -> c.getId() == customerId).findFirst()
                .orElseThrow(() -> new Exception("Customer not found (ID " + customerId + ")."));
        SubscriptionPlan plan = findPlan(planId).orElseThrow(() -> new Exception("Plan not found (ID " + planId + ")."));
        if (quantity < 1 || quantity > 1000) throw new Exception("Quantity must be 1-1000.");

        double total = plan.getUnitPrice() * quantity;
        Subscription s = new Subscription(ctx.subSeq++, customerId, planId, quantity, total);
        ctx.subscriptions.add(s);
        return s;
    }

    public void updateQuantity(int subscriptionId, int newQuantity) throws Exception {
        Subscription s = findById(subscriptionId).orElseThrow(() -> new Exception("Subscription not found (ID " + subscriptionId + ")."));
        SubscriptionPlan plan = findPlan(s.getPlanId()).orElseThrow(() -> new Exception("Plan not found."));
        if (newQuantity < 1 || newQuantity > 1000) throw new Exception("Quantity must be 1-1000.");
        if (newQuantity * plan.getUnitPrice() < s.getAmountPaid())
            throw new Exception("New total would be below amount already paid (" + s.getAmountPaid() + ").");

        s.setQuantity(newQuantity);
        s.setTotalAmount(plan.getUnitPrice() * newQuantity);
    }

    public void changePlan(int subscriptionId, int newPlanId) throws Exception {
        Subscription s = findById(subscriptionId).orElseThrow(() -> new Exception("Subscription not found (ID " + subscriptionId + ")."));
        SubscriptionPlan plan = findPlan(newPlanId).orElseThrow(() -> new Exception("Plan not found (ID " + newPlanId + ")."));
        if (plan.getUnitPrice() * s.getQuantity() < s.getAmountPaid())
            throw new Exception("New plan total would be below amount already paid (" + s.getAmountPaid() + ").");
        s.setPlanId(newPlanId);
        s.setTotalAmount(plan.getUnitPrice() * s.getQuantity());
    }

    public void updateStatus(int subscriptionId, SubscriptionStatus status) throws Exception {
        Subscription s = findById(subscriptionId).orElseThrow(() -> new Exception("Subscription not found (ID " + subscriptionId + ")."));
        if (status == null) throw new Exception("Status cannot be empty.");
        s.setStatus(status);
    }

    public void deleteSubscription(int subscriptionId) throws Exception {
        Subscription s = findById(subscriptionId).orElseThrow(() -> new Exception("Subscription not found (ID " + subscriptionId + ")."));
        boolean hasDelivery = ctx.deliveries.stream().anyMatch(d -> d.getSubscriptionId() == subscriptionId);
        if (hasDelivery) throw new Exception("Cannot delete: subscription has delivery record(s). Cancel it instead.");
        boolean hasPayment = ctx.payments.stream().anyMatch(p -> p.getSubscriptionId() == subscriptionId);
        if (hasPayment) throw new Exception("Cannot delete: subscription has payment record(s). Cancel it instead.");
        ctx.subscriptions.remove(s);
    }

    public Optional<Subscription> findById(int id) {
        return ctx.subscriptions.stream().filter(s -> s.getId() == id).findFirst();
    }

    public List<Subscription> byCustomer(int customerId) {
        List<Subscription> out = new ArrayList<>();
        for (Subscription s : ctx.subscriptions) {
            if (s.getCustomerId() == customerId) out.add(s);
        }
        return out;
    }

    public List<Subscription> all() {
        return ctx.subscriptions;
    }
}

package util;

import model.*;
import service.AppContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Prints an order-style receipt summarizing one delivery and everything assigned to it. */
public class DeliveryReceipt {
    private static final int WIDTH = 48;

    public static void print(AppContext ctx, int deliveryId) throws Exception {
        Delivery d = ctx.deliveries.stream().filter(x -> x.getId() == deliveryId).findFirst()
                .orElseThrow(() -> new Exception("Delivery not found (ID " + deliveryId + ")."));

        Customer cust = ctx.customers.stream().filter(x -> x.getId() == d.getCustomerId()).findFirst().orElse(null);
        Subscription sub = ctx.subscriptions.stream().filter(x -> x.getId() == d.getSubscriptionId()).findFirst().orElse(null);
        SubscriptionPlan plan = null;
        if (sub != null) {
            int planId = sub.getPlanId();
            plan = ctx.plans.stream().filter(x -> x.getId() == planId).findFirst().orElse(null);
        }
        Driver driver = null;
        if (d.getDriverId() != null) {
            int driverId = d.getDriverId();
            driver = ctx.drivers.stream().filter(x -> x.getId() == driverId).findFirst().orElse(null);
        }
        List<Container> containers = new ArrayList<>();
        for (int cid : d.getContainerIds()) {
            ctx.containers.stream().filter(x -> x.getId() == cid).findFirst().ifPresent(containers::add);
        }
        List<Payment> payments = new ArrayList<>();
        for (Payment p : ctx.payments) {
            if (sub != null && p.getSubscriptionId() == sub.getId()) payments.add(p);
        }
        // Containers assigned to this customer/subscription (e.g. via Container
        // Management) that are not linked to this specific delivery.
        List<Container> accountContainers = new ArrayList<>();
        for (Container c : ctx.containers) {
            if (containers.contains(c)) continue;
            if (c.getAssignedCustomerId() != null && c.getAssignedCustomerId() == d.getCustomerId()
                    && c.getAssignedSubscriptionId() != null
                    && c.getAssignedSubscriptionId() == d.getSubscriptionId()) {
                accountContainers.add(c);
            }
        }

        bar("=");
        center("DELIVERY RECEIPT");
        center("Subscription Delivery & Container");
        center("Asset Tracking System");
        bar("-");
        row("Receipt No", String.format("DL%03d", d.getId()));
        row("Date Issued", String.valueOf(LocalDate.now()));
        bar("-");
        System.out.println(" DELIVERY DETAILS");
        row("Scheduled Date", String.valueOf(d.getScheduledDate()));
        row("Status", String.valueOf(d.getStatus()));
        bar("-");
        System.out.println(" CUSTOMER");
        if (cust == null) {
            row("ID", String.format("C%03d (record removed)", d.getCustomerId()));
        } else {
            row("ID", String.format("C%03d - %s", cust.getId(), cust.getName()));
            row("Address", cust.getAddress());
            row("Contact", cust.getContactNumber());
            row("Email", cust.getEmail());
        }
        bar("-");
        if (sub == null) {
            System.out.println(" SUBSCRIPTION");
            row("ID", String.format("S%03d (record removed)", d.getSubscriptionId()));
        } else {
            System.out.println(" SUBSCRIPTION " + String.format("S%03d", sub.getId()) + " [" + sub.getStatus() + "]");
            row("Plan", plan == null ? "-" : plan.getName() + " (" + plan.getBillingCycle() + ")");
            row("Quantity", plan == null
                    ? String.valueOf(sub.getQuantity())
                    : sub.getQuantity() + " x " + String.format("%.2f", plan.getUnitPrice()));
            row("Total", String.format("%.2f", sub.getTotalAmount()));
            row("Paid", String.format("%.2f", sub.getAmountPaid()));
            row("Balance", String.format("%.2f", sub.getRemainingBalance()));
        }
        bar("-");
        System.out.println(" DRIVER");
        if (driver == null) {
            row("Assigned", d.getDriverId() == null ? "Not yet assigned" : "D"
                    + String.format("%03d (record removed)", d.getDriverId()));
        } else {
            row("ID", String.format("D%03d - %s", driver.getId(), driver.getName()));
            row("Contact", driver.getContactNumber());
        }
        bar("-");
        if (!accountContainers.isEmpty()) {
            bar("-");
            System.out.println(" CONTAINERS (" + accountContainers.size() + ")");
            for (Container c : accountContainers) {
                System.out.println("  " + String.format("CT%03d", c.getId()) + " " + c.getContainerCode()
                        + " | " + c.getStatus() + " | " + c.getCondition());
            }
        }
        bar("-");
        System.out.println(" PAYMENTS (" + payments.size() + ")");
        if (payments.isEmpty()) {
            System.out.println("  (no payments recorded)");
        } else {
            for (Payment p : payments) {
                System.out.println("  " + String.format("PMT%03d", p.getId()) + " | " + p.getDate()
                        + " | " + p.getMethod() + " | " + String.format("%.2f", p.getAmount()));
            }
        }
        bar("-");
        center("*** Thank you for your business! ***");
        bar("=");
    }

    private static void bar(String ch) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < WIDTH; i++) sb.append(ch);
        System.out.println(sb);
    }

    private static void center(String text) {
        if (text.length() >= WIDTH) {
            System.out.println(text);
            return;
        }
        int pad = (WIDTH - text.length()) / 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pad; i++) sb.append(' ');
        sb.append(text);
        System.out.println(sb);
    }

    private static void row(String label, String value) {
        String v = value == null ? "-" : value;
        int labelWidth = 16;
        StringBuilder sb = new StringBuilder(" ");
        sb.append(label);
        for (int i = label.length(); i < labelWidth; i++) sb.append(' ');
        sb.append(": ").append(v);
        String line = sb.toString();
        System.out.println(line.length() > WIDTH ? line.substring(0, WIDTH) : line);
    }
}

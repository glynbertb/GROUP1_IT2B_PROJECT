package ui;

import model.Subscription;
import model.SubscriptionPlan;
import model.SubscriptionStatus;
import service.AppContext;
import service.SubscriptionService;
import util.InputHelper;
import util.TablePrinter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class SubscriptionMenu {
    private final SubscriptionService subscriptionService;
    private final Scanner sc;

    public SubscriptionMenu(AppContext ctx, Scanner sc) {
        this.subscriptionService = new SubscriptionService(ctx);
        this.sc = sc;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-- Subscription Management --");
            System.out.println("1. Manage Subscription Plans");
            System.out.println("2. Manage Customer Subscriptions");
            System.out.println("0. Back to Main Menu");

            String c = InputHelper.readText(sc, "Choose: ");
            switch (c) {
                case "1":
                    showPlanMenu();
                    break;
                case "2":
                    showSubscriptionMenu();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("  ! Invalid option. Choose 0-2.");
                    break;
            }
        }
    }

    // --- SUB-MENU 1: PLAN MANAGEMENT ---
    private void showPlanMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-- Manage Subscription Plans --");
            System.out.println("1. Create subscription plan");
            System.out.println("2. List plans");
            System.out.println("3. Update plan");
            System.out.println("4. Delete plan");
            System.out.println("5. Back");

            String c = InputHelper.readText(sc, "Choose: ");
            try {
                switch (c) {
                    case "1": createPlan(); break;
                    case "2": listPlans(); break;
                    case "3": updatePlan(); break;
                    case "4": deletePlan(); break;
                    case "5": back = true; break;
                    default: System.out.println("  ! Invalid option. Choose 0-4.");
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    // --- SUB-MENU 2: SUBSCRIPTION MANAGEMENT ---
    private void showSubscriptionMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-- Manage Customer Subscriptions --");
            System.out.println("1. Create subscription for customer");
            System.out.println("2. List all subscriptions");
            System.out.println("3. Update subscription quantity");
            System.out.println("4. Update subscription status");
            System.out.println("5. Delete subscription");
            System.out.println("6. Back");

            String c = InputHelper.readText(sc, "Choose: ");
            try {
                switch (c) {
                    case "1": createSub(); break;
                    case "2": listSubs(); break;
                    case "3": updateQty(); break;
                    case "4": updateStatus(); break;
                    case "5": deleteSub(); break;
                    case "6": back = true; break;
                    default: System.out.println("  ! Invalid option. Choose 0-6.");
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    // --- CRUD HELPER METHODS ---

    private void createPlan() throws Exception {
        String name = InputHelper.readText(sc, "Plan name (2-60 chars): ", 0, 60, false);
        String desc = InputHelper.readText(sc, "Description (3-150 chars): ", 0, 150, false);
        double price = InputHelper.readDouble(sc, "Unit price (0.01-1000000): ", 0.01, 1000000);
        String cycle = InputHelper.readOption(sc, "Billing cycle (WEEKLY/MONTHLY): ", Set.of("WEEKLY", "MONTHLY"));
        SubscriptionPlan created = subscriptionService.createPlan(name, desc, price, cycle);
        System.out.println("Created successfully.");
        printPlans(java.util.List.of(created));
    }

    private void listPlans() {
        printPlans(subscriptionService.allPlans());
    }

    private void updatePlan() throws Exception {
        int id = InputHelper.readId(sc, "Plan ID to update: ");
        SubscriptionPlan existing = subscriptionService.findPlan(id)
                .orElseThrow(() -> new Exception("Plan not found (ID " + id + ")."));
        printPlans(java.util.List.of(existing));
        String name = InputHelper.readText(sc, "New name [" + existing.getName() + "]: ", 2, 60, false);
        String desc = InputHelper.readText(sc, "New description: ", 3, 150, false);
        double price = InputHelper.readDouble(sc, "New unit price: ", 0.01, 1000000);
        String cycle = InputHelper.readOption(sc, "New cycle (WEEKLY/MONTHLY): ", Set.of("WEEKLY", "MONTHLY"));
        SubscriptionPlan updated = subscriptionService.updatePlan(id, name, desc, price, cycle);
        System.out.println("Updated successfully.");
        printPlans(java.util.List.of(updated));
    }

    private void deletePlan() throws Exception {
        int id = InputHelper.readId(sc, "Plan ID to delete: ");
        if (!InputHelper.confirm(sc, "Delete plan P" + String.format("%03d", id) + "?")) {
            System.out.println("Cancelled.");
            return;
        }
        subscriptionService.deletePlan(id);
        System.out.println("Plan deleted.");
    }

    private void createSub() throws Exception {
        int custId = InputHelper.readId(sc, "Customer ID: ");
        listPlans();
        int planId = InputHelper.readId(sc, "Plan ID: ");
        int qty = InputHelper.readInt(sc, "Quantity 1-1000 (# of containers/units): ", 1, 1000);
        Subscription s = subscriptionService.createSubscription(custId, planId, qty);
        System.out.printf("Subscription created [S%03d] Total amount: %.2f%n", s.getId(), s.getTotalAmount());
    }

    private void listSubs() {
        printSubs(subscriptionService.all());
    }


    private void updateQty() throws Exception {
        int subId = InputHelper.readId(sc, "Subscription ID: ");
        int qty = InputHelper.readInt(sc, "New quantity (1-1000): ", 1, 1000);
        subscriptionService.updateQuantity(subId, qty);
        System.out.println("Quantity updated.");
    }

    private void updateStatus() throws Exception {
        int subId = InputHelper.readId(sc, "Subscription ID: ");
        System.out.println("Statuses: ACTIVE, PAUSED, CANCELLED, COMPLETED");
        SubscriptionStatus st = InputHelper.readEnum(sc, "New status: ", SubscriptionStatus.class);
        subscriptionService.updateStatus(subId, st);
        System.out.println("Status updated.");
    }

    private void deleteSub() throws Exception {
        int subId = InputHelper.readId(sc, "Subscription ID to delete: ");
        if (!InputHelper.confirm(sc, "Delete subscription S" + String.format("%03d", subId) + "?")) {
            System.out.println("Cancelled.");
            return;
        }
        subscriptionService.deleteSubscription(subId);
        System.out.println("Subscription deleted (or cancel via status update if it has records).");
    }

    private void printPlans(List<SubscriptionPlan> list) {
        String[] headers = {"ID", "Plan Name", "Unit Price", "Cycle", "Description"};
        List<String[]> rows = new ArrayList<>();
        for (SubscriptionPlan p : list) {
            rows.add(new String[]{String.format("P%03d", p.getId()), p.getName(),
                    String.format("%.2f", p.getUnitPrice()), p.getBillingCycle(), p.getDescription()});
        }
        TablePrinter.print("Subscription Plan List", headers, rows);
    }

    private void printSubs(List<Subscription> list) {
        String[] headers = {"ID", "Cust ID", "Plan ID", "Qty", "Total", "Paid", "Balance", "Status"};
        List<String[]> rows = new ArrayList<>();
        for (Subscription s : list) {
            rows.add(new String[]{String.format("S%03d", s.getId()),
                    String.format("C%03d", s.getCustomerId()), String.format("P%03d", s.getPlanId()),
                    String.valueOf(s.getQuantity()), String.format("%.2f", s.getTotalAmount()),
                    String.format("%.2f", s.getAmountPaid()),
                    String.format("%.2f", s.getRemainingBalance()), String.valueOf(s.getStatus())});
        }
        TablePrinter.print("Subscription List", headers, rows);
    }
}
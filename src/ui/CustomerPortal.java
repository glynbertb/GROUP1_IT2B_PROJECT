package ui;

import model.Container;
import model.Customer;
import model.Delivery;
import model.Payment;
import model.Subscription;
import model.SubscriptionPlan;
import model.SubscriptionStatus;
import service.AppContext;
import service.AuthService;
import service.ContainerService;
import service.CustomerService;
import service.DeliveryService;
import service.DriverService;
import service.PaymentService;
import service.SubscriptionService;
import util.InputHelper;
import util.TablePrinter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CustomerPortal {
    private final AppContext ctx;
    private final Scanner sc;
    private final AuthService authService;
    private final CustomerService customerService;
    private final SubscriptionService subscriptionService;
    private final ContainerService containerService;
    private final DeliveryService deliveryService;
    private final DriverService driverService;
    private final PaymentService paymentService;

    public CustomerPortal(AppContext ctx, Scanner sc) {
        this.ctx = ctx;
        this.sc = sc;
        this.authService = new AuthService(ctx);
        this.customerService = new CustomerService(ctx);
        this.subscriptionService = new SubscriptionService(ctx);
        this.containerService = new ContainerService(ctx);
        this.driverService = new DriverService(ctx);
        this.deliveryService = new DeliveryService(ctx, driverService, containerService);
        this.paymentService = new PaymentService(ctx, subscriptionService);
    }

    public void show() {
        Integer cid = ctx.currentUser.getCustomerId();
        if (cid == null) {
            System.out.println("  ! Your login is not linked to a customer record. Please contact an admin.");
            return;
        }
        Customer me = customerService.findById(cid).orElse(null);
        if (me == null) {
            System.out.println("  ! Your linked customer record no longer exists. Please contact an admin.");
            return;
        }
        System.out.println("\nWelcome, " + ctx.currentUser.getFullName() + "!");
        printCustomer(me);

        boolean running = true;
        while (running) {
            System.out.println("--------------------------------------------------");
            System.out.println("\n-- Customer Self-Service Portal --");
            System.out.println("1. View My Active Subscriptions");
            System.out.println("2. Request New Subscription / Change Plan");
            System.out.println("3. View Containers Currently in My Possession");
            System.out.println("4. Track Current Delivery Status");
            System.out.println("5. View Billing History & Remaining Balance");
            System.out.println("6. Update Contact & Address Details");
            System.out.println("0. Log Out");
            String c = InputHelper.readText(sc, "Choose: ");
            try {
                switch (c) {
                    case "1": viewActiveSubscriptions(cid); break;
                    case "2": requestOrChangePlan(cid); break;
                    case "3": viewMyContainers(cid); break;
                    case "4": trackDeliveries(cid); break;
                    case "5": billingHistory(cid); break;
                    case "6": updateDetails(cid); break;
                    case "0":
                        System.out.println("Logging out. Thank you, " + ctx.currentUser.getFullName() + "!");
                        authService.logout();
                        running = false;
                        break;
                    default: System.out.println("  ! Invalid option. Choose 0-6.");
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void viewActiveSubscriptions(int cid) {
        List<Subscription> active = new ArrayList<>();
        for (Subscription s : subscriptionService.byCustomer(cid)) {
            if (s.getStatus() == SubscriptionStatus.ACTIVE) active.add(s);
        }
        printSubscriptions("My Active Subscriptions", active);
    }

    private void requestOrChangePlan(int cid) throws Exception {
        System.out.println("1. Request new subscription");
        System.out.println("2. Change plan of an existing subscription");
        String c = InputHelper.readText(sc, "Choose: ");
        if (c.equals("1")) {
            printPlans(subscriptionService.allPlans());
            if (subscriptionService.allPlans().isEmpty()) return;
            int planId = InputHelper.readId(sc, "Plan ID: ");
            int qty = InputHelper.readInt(sc, "Quantity 1-1000: ", 1, 1000);
            Subscription s = subscriptionService.createSubscription(cid, planId, qty);
            System.out.printf("Request recorded [S%03d]. Total amount: %.2f%n", s.getId(), s.getTotalAmount());
        } else if (c.equals("2")) {
            List<Subscription> mine = subscriptionService.byCustomer(cid);
            if (mine.isEmpty()) {
                System.out.println("(You have no subscriptions yet.)");
                return;
            }
            printSubscriptions("My Subscriptions", mine);
            int subId = InputHelper.readId(sc, "Subscription ID: ");
            Subscription s = mySubscription(cid, subId);
            printPlans(subscriptionService.allPlans());
            int planId = InputHelper.readId(sc, "New plan ID: ");
            subscriptionService.changePlan(s.getId(), planId);
            System.out.println("Plan changed successfully.");
            printSubscriptions("Updated Subscription",
                    java.util.List.of(subscriptionService.findById(s.getId()).orElse(s)));
        } else {
            System.out.println("  ! Invalid option. Choose 1 or 2.");
        }
    }

    private void viewMyContainers(int cid) {
        List<String[]> rows = new ArrayList<>();
        for (Container ct : containerService.all()) {
            if (ct.getAssignedCustomerId() != null && ct.getAssignedCustomerId() == cid) {
                rows.add(new String[]{String.format("CT%03d", ct.getId()), ct.getContainerCode(),
                        String.valueOf(ct.getStatus()), String.valueOf(ct.getCondition()),
                        ct.getAssignedSubscriptionId() == null ? "-"
                                : String.format("S%03d", ct.getAssignedSubscriptionId())});
            }
        }
        TablePrinter.print("Containers In My Possession",
                new String[]{"ID", "Code", "Status", "Condition", "Sub ID"}, rows);
    }

    private void trackDeliveries(int cid) {
        List<Delivery> mine = deliveryService.byCustomer(cid);
        List<String[]> rows = new ArrayList<>();
        for (Delivery d : mine) {
            String driver = d.getDriverId() == null ? "Not assigned yet" :
                    driverService.findById(d.getDriverId())
                            .map(drv -> drv.getName()).orElse("Unknown");
            rows.add(new String[]{String.format("DL%03d", d.getId()),
                    String.format("S%03d", d.getSubscriptionId()),
                    String.valueOf(d.getScheduledDate()), driver,
                    String.valueOf(d.getStatus()),
                    d.getContainerIds().isEmpty() ? "-" : d.getContainerIds().toString()});
        }
        TablePrinter.print("My Delivery Status",
                new String[]{"ID", "Sub ID", "Date", "Driver", "Status", "Containers"}, rows);
    }

    private void billingHistory(int cid) {
        List<Subscription> mine = subscriptionService.byCustomer(cid);
        if (mine.isEmpty()) {
            System.out.println("(You have no subscriptions yet.)");
            return;
        }
        double grandBalance = 0;
        for (Subscription s : mine) {
            List<Payment> payments = paymentService.byline(s.getId());
            List<String[]> rows = new ArrayList<>();
            for (Payment p : payments) {
                rows.add(new String[]{String.format("PMT%03d", p.getId()),
                        String.format("%.2f", p.getAmount()), p.getMethod(), String.valueOf(p.getDate())});
            }
            TablePrinter.print("Billing History for S" + String.format("%03d", s.getId()),
                    new String[]{"Payment", "Amount", "Method", "Date"}, rows);
            System.out.printf("  Balance for S%03d: %.2f%n", s.getId(), s.getRemainingBalance());
            grandBalance += s.getRemainingBalance();
        }
        System.out.printf("  TOTAL REMAINING BALANCE: %.2f%n", grandBalance);
    }

    private void updateDetails(int cid) throws Exception {
        Customer me = customerService.findById(cid)
                .orElseThrow(() -> new Exception("Customer record not found."));
        printCustomer(me);
        String contact = InputHelper.readContact(sc, "New contact [" + me.getContactNumber() + "]: ", false);
        String address = InputHelper.readText(sc, "New address [" + me.getAddress() + "]: ", 5, 120, false);
        Customer updated = customerService.update(cid, me.getName(), address, contact, me.getEmail());
        System.out.println("Details updated successfully.");
        printCustomer(updated);
    }

    private Subscription mySubscription(int cid, int subId) throws Exception {
        Subscription s = subscriptionService.findById(subId)
                .orElseThrow(() -> new Exception("Subscription not found (ID " + subId + ")."));
        if (s.getCustomerId() != cid)
            throw new Exception("Subscription S" + String.format("%03d", subId) + " is not yours.");
        return s;
    }

    private void printSubscriptions(String title, List<Subscription> list) {
        List<String[]> rows = new ArrayList<>();
        for (Subscription s : list) {
            String plan = subscriptionService.findPlan(s.getPlanId())
                    .map(SubscriptionPlan::getName).orElse("Unknown");
            rows.add(new String[]{String.format("S%03d", s.getId()), plan,
                    String.valueOf(s.getQuantity()), String.format("%.2f", s.getTotalAmount()),
                    String.format("%.2f", s.getAmountPaid()),
                    String.format("%.2f", s.getRemainingBalance()), String.valueOf(s.getStatus())});
        }
        TablePrinter.print(title,
                new String[]{"ID", "Plan", "Qty", "Total", "Paid", "Balance", "Status"}, rows);
    }

    private void printPlans(List<SubscriptionPlan> list) {
        List<String[]> rows = new ArrayList<>();
        for (SubscriptionPlan p : list) {
            rows.add(new String[]{String.format("P%03d", p.getId()), p.getName(),
                    String.format("%.2f", p.getUnitPrice()), p.getBillingCycle(), p.getDescription()});
        }
        TablePrinter.print("Available Plans",
                new String[]{"ID", "Plan", "Unit Price", "Cycle", "Description"}, rows);
    }

    private void printCustomer(Customer c) {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{String.format("C%03d", c.getId()), c.getName(),
                c.getContactNumber(), c.getEmail(), c.getAddress()});
        TablePrinter.print("My Details",
                new String[]{"ID", "Name", "Contact", "Email", "Address"}, rows);
    }
}

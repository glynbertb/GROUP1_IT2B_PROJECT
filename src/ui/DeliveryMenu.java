package ui;

import model.*;
import service.*;
import util.DeliveryReceipt;
import util.InputHelper;
import util.TablePrinter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DeliveryMenu {
    private final AppContext ctx;
    private final DeliveryService deliveryService;
    private final SubscriptionService subscriptionService;
    private final DriverService driverService;
    private final ContainerService containerService;
    private final Scanner sc;

    public DeliveryMenu(AppContext ctx, Scanner sc) {
        this.ctx = ctx;
        this.subscriptionService = new SubscriptionService(ctx);
        this.driverService = new DriverService(ctx);
        this.containerService = new ContainerService(ctx);
        this.deliveryService = new DeliveryService(ctx, driverService, containerService);
        this.sc = sc;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-- Delivery Management --");
            System.out.println("1. Schedule & Operations Management");
            System.out.println("2. Driver Assignments");
            System.out.println("3. Container Assignments");
            System.out.println("0. Back to Main Menu");

            String c = InputHelper.readText(sc, "Choose: ");
            switch (c) {
                case "1":
                    showScheduleMenu();
                    break;
                case "2":
                    showDriverAssignmentMenu();
                    break;
                case "3":
                    showContainerAssignmentMenu();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("  ! Invalid option. Choose 0-3.");
                    break;
            }
        }
    }

    // --- SUB-MENU 1: SCHEDULE & OPERATIONS MANAGEMENT ---
    private void showScheduleMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-- Schedule & Operations Management --");
            System.out.println("1. Schedule delivery");
            System.out.println("2. List all deliveries");
            System.out.println("3. Reschedule delivery");
            System.out.println("4. Update delivery status");
            System.out.println("5. Cancel delivery");
            System.out.println("6. Delete delivery");
            System.out.println("7. Print delivery receipt (order summary)");
            System.out.println("8. Back");

            String c = InputHelper.readText(sc, "Choose: ");
            try {
                switch (c) {
                    case "1":
                        schedule();
                        break;
                    case "2":
                        list();
                        break;
                    case "3":
                        reschedule();
                        break;
                    case "4":
                        updateStatus();
                        break;
                    case "5":
                        cancel();
                        break;
                    case "6":
                        delete();
                        break;
                    case "7":
                        printReceipt();
                        break;
                    case "8":
                        back = true;
                        break;
                    default:
                        System.out.println("  ! Invalid option. Choose 1-8.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    // --- SUB-MENU 2: DELIVERY PERSONNEL ASSIGNMENTS ---
    private void showDriverAssignmentMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-- Driver Assignments --");
            System.out.println("1. Assign driver to delivery");
            System.out.println("2. Unassign driver");
            System.out.println("0. Back");

            String c = InputHelper.readText(sc, "Choose: ");
            try {
                switch (c) {
                    case "1":
                        assignDriver();
                        break;
                    case "2":
                        unassignDriver();
                        break;
                    case "3":
                        back = true;
                        break;
                    default:
                        System.out.println("  ! Invalid option. Choose 0-2.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    // --- SUB-MENU 3: CONTAINER ASSIGNMENTS ---
    private void showContainerAssignmentMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-- Container Assignments --");
            System.out.println("1. Assign / prepare container for delivery");
            System.out.println("2. Remove container from delivery");
            System.out.println("3. Back");

            String c = InputHelper.readText(sc, "Choose: ");
            try {
                switch (c) {
                    case "1":
                        assignContainer();
                        break;
                    case "2":
                        removeContainer();
                        break;
                    case "3":
                        back = true;
                        break;
                    default:
                        System.out.println("  ! Invalid option. Choose 0-2.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    // --- HELPER METHODS ---

    private void schedule() throws Exception {
        int subId = InputHelper.readId(sc, "Subscription ID: ");
        Subscription s = subscriptionService.findById(subId)
                .orElseThrow(() -> new Exception("Subscription not found (ID " + subId + ")."));
        LocalDate date = InputHelper.readDate(sc, "Scheduled date (yyyy-MM-dd, today or later): ", true);
        Delivery d = deliveryService.schedule(subId, s.getCustomerId(), date);
        System.out.println("Delivery scheduled [DL" + d.getId() + "] for customer " + s.getCustomerId() + " on " + date);
    }

    private void list() {
        printDeliveries(deliveryService.all());
    }

    private void printReceipt() throws Exception {
        int delId = InputHelper.readId(sc, "Delivery ID: ");
        DeliveryReceipt.print(ctx, delId);
    }

    private void assignDriver() throws Exception {
        int delId = InputHelper.readId(sc, "Delivery ID: ");
        if (driverService.available().isEmpty()) {
            System.out.println("  ! No available drivers right now.");
            return;
        }
        System.out.println("Available drivers:");
        printDriverRows(driverService.available());
        int drvId = InputHelper.readId(sc, "Driver ID: ");
        deliveryService.assignDriver(delId, drvId);
        System.out.println("Driver assigned.");
    }

    private void unassignDriver() throws Exception {
        int delId = InputHelper.readId(sc, "Delivery ID: ");
        deliveryService.unassignDriver(delId);
        System.out.println("Driver unassigned; delivery back to SCHEDULED.");
    }

    private void assignContainer() throws Exception {
        int delId = InputHelper.readId(sc, "Delivery ID: ");
        if (containerService.available().isEmpty()) {
            System.out.println("  ! No available containers right now.");
            return;
        }
        System.out.println("Available containers:");
        printContainerRows(containerService.available());
        int ctId = InputHelper.readId(sc, "Container ID: ");
        deliveryService.assignContainer(delId, ctId);
        System.out.println("Container assigned to delivery and customer.");
    }

    private void removeContainer() throws Exception {
        int delId = InputHelper.readId(sc, "Delivery ID: ");
        int ctId = InputHelper.readId(sc, "Container ID to remove: ");
        deliveryService.removeContainer(delId, ctId);
        System.out.println("Container removed from delivery (now AVAILABLE).");
    }

    private void reschedule() throws Exception {
        int delId = InputHelper.readId(sc, "Delivery ID: ");
        LocalDate date = InputHelper.readDate(sc, "New date (yyyy-MM-dd, today or later): ", true);
        deliveryService.reschedule(delId, date);
        System.out.println("Delivery rescheduled to " + date + ".");
    }

    private void updateStatus() throws Exception {
        int delId = InputHelper.readId(sc, "Delivery ID: ");
        System.out.println("Statuses: SCHEDULED, ASSIGNED, IN_TRANSIT, DELIVERED, RETURNED, CANCELLED");
        DeliveryStatus st = InputHelper.readEnum(sc, "New status: ", DeliveryStatus.class);
        deliveryService.updateStatus(delId, st);
        System.out.println("Delivery status updated to " + st);
    }

    private void cancel() throws Exception {
        int delId = InputHelper.readId(sc, "Delivery ID to cancel: ");
        if (!InputHelper.confirm(sc, "Cancel delivery DL" + delId + "?")) {
            System.out.println("Cancelled.");
            return;
        }
        deliveryService.cancel(delId);
        System.out.println("Delivery cancelled (driver released).");
    }

    private void delete() throws Exception {
        int delId = InputHelper.readId(sc, "Delivery ID to delete: ");
        if (!InputHelper.confirm(sc, "Delete delivery DL" + delId + "?")) {
            System.out.println("Cancelled.");
            return;
        }
        deliveryService.delete(delId);
        System.out.println("Delivery deleted.");
    }

    private void printDeliveries(List<Delivery> list) {
        String[] headers = {"ID", "Sub ID", "Cust ID", "Driver", "Date", "Status", "Containers"};
        List<String[]> rows = new ArrayList<>();
        for (Delivery d : list) {
            rows.add(new String[]{String.format("DL%03d", d.getId()),
                    String.format("S%03d", d.getSubscriptionId()),
                    String.format("C%03d", d.getCustomerId()),
                    d.getDriverId() == null ? "-" : String.format("D%03d", d.getDriverId()),
                    String.valueOf(d.getScheduledDate()), String.valueOf(d.getStatus()),
                    d.getContainerIds().isEmpty() ? "-" : d.getContainerIds().toString()});
        }
        TablePrinter.print("Delivery List", headers, rows);
    }

    private void printDriverRows(List<Driver> list) {
        String[] headers = {"ID", "Name", "Contact", "License", "Status"};
        List<String[]> rows = new ArrayList<>();
        for (Driver dr : list) {
            rows.add(new String[]{String.format("D%03d", dr.getId()), dr.getName(),
                    dr.getContactNumber(), dr.getLicenseNumber(),
                    dr.isAvailable() ? "AVAILABLE" : "BUSY"});
        }
        TablePrinter.print("Available Drivers", headers, rows);
    }

    private void printContainerRows(List<Container> list) {
        String[] headers = {"ID", "Code", "Status", "Condition"};
        List<String[]> rows = new ArrayList<>();
        for (Container ct : list) {
            rows.add(new String[]{String.format("CT%03d", ct.getId()), ct.getContainerCode(),
                    String.valueOf(ct.getStatus()), String.valueOf(ct.getCondition())});
        }
        TablePrinter.print("Available Containers", headers, rows);
    }
}
package ui;

import model.*;
import service.*;
import util.InputHelper;
import util.TablePrinter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DeliveryMenu {
    private final DeliveryService deliveryService;
    private final SubscriptionService subscriptionService;
    private final DriverService driverService;
    private final ContainerService containerService;
    private final Scanner sc;

    public DeliveryMenu(AppContext ctx, Scanner sc) {
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
            System.out.println("1. Schedule delivery (Create)");
            System.out.println("2. List all deliveries (Read)");
            System.out.println("3. View delivery by ID (Read)");
            System.out.println("4. Assign driver to delivery (Update)");
            System.out.println("5. Unassign driver (Update)");
            System.out.println("6. Assign / prepare container for delivery (Update)");
            System.out.println("7. Remove container from delivery (Update)");
            System.out.println("8. Reschedule delivery (Update)");
            System.out.println("9. Update delivery status (Update)");
            System.out.println("10. Cancel delivery (Update)");
            System.out.println("11. Delete delivery (Delete)");
            System.out.println("0. Back");
            String c = InputHelper.readText(sc, "Choose: ");
            try {
                switch (c) {
                    case "1": schedule(); break;
                    case "2": list(); break;
                    case "3": view(); break;
                    case "4": assignDriver(); break;
                    case "5": unassignDriver(); break;
                    case "6": assignContainer(); break;
                    case "7": removeContainer(); break;
                    case "8": reschedule(); break;
                    case "9": updateStatus(); break;
                    case "10": cancel(); break;
                    case "11": delete(); break;
                    case "0": back = true; break;
                    default: System.out.println("  ! Invalid option. Choose 0-11.");
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

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

    private void view() {
        int id = InputHelper.readId(sc, "Delivery ID: ");
        deliveryService.findById(id).ifPresentOrElse(
                d -> printDeliveries(java.util.List.of(d)),
                () -> System.out.println("  ! Delivery not found (ID " + id + ")."));
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

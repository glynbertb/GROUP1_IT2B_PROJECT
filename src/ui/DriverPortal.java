package ui;

import model.Container;
import model.ContainerCondition;
import model.ContainerStatus;
import model.Customer;
import model.Delivery;
import model.DeliveryStatus;
import model.Driver;
import service.AppContext;
import service.AuthService;
import service.ContainerService;
import service.DeliveryService;
import service.DriverService;
import util.InputHelper;
import util.TablePrinter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DriverPortal {
    private final AppContext ctx;
    private final Scanner sc;
    private final AuthService authService;
    private final DriverService driverService;
    private final ContainerService containerService;
    private final DeliveryService deliveryService;

    public DriverPortal(AppContext ctx, Scanner sc) {
        this.ctx = ctx;
        this.sc = sc;
        this.authService = new AuthService(ctx);
        this.driverService = new DriverService(ctx);
        this.containerService = new ContainerService(ctx);
        this.deliveryService = new DeliveryService(ctx, driverService, containerService);
    }

    public void show() {
        Integer did = ctx.currentUser.getDriverId();
        if (did == null) {
            System.out.println("  ! Your login is not linked to a driver record. Please contact an admin.");
            return;
        }
        Driver me = driverService.findById(did).orElse(null);
        if (me == null) {
            System.out.println("  ! Your linked driver record no longer exists. Please contact an admin.");
            return;
        }
        System.out.println("\n              Welcome, " + ctx.currentUser.getFullName() + "!");
        printLinkedDriver(me);

        boolean running = true;
        while (running) {
            System.out.println("\n-- Driver Field Portal --");
            System.out.println("1. View Today's Assigned Route");
            System.out.println("2. Process Delivery & Container Exchange");
            System.out.println("3. Report Damaged / Missing Container");
            System.out.println("0. Log Out");
            String c = InputHelper.readText(sc, "Choose: ");
            try {
                switch (c) {
                    case "1": todaysRoute(did); break;
                    case "2": processDelivery(did); break;
                    case "3": reportContainer(did); break;
                    case "0":
                        System.out.println("Logging out. Drive safe, " + ctx.currentUser.getFullName() + "!");
                        authService.logout();
                        running = false;
                        break;
                    default: System.out.println("  ! Invalid option. Choose 0-3.");
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void printLinkedDriver(Driver d) {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{String.format("D%03d", d.getId()), d.getName(),
                d.getContactNumber(), d.getLicenseNumber(),
                d.isAvailable() ? "AVAILABLE" : "BUSY"});
        TablePrinter.print("My Linked Driver Record",
                new String[]{"ID", "Name", "Contact", "License", "Status"}, rows);
    }

    private void todaysRoute(int driverId) {
        List<Delivery> route = new ArrayList<>();
        for (Delivery d : deliveryService.all()) {
            if (d.getDriverId() != null && d.getDriverId() == driverId
                    && d.getScheduledDate().equals(LocalDate.now())
                    && (d.getStatus() == DeliveryStatus.SCHEDULED
                            || d.getStatus() == DeliveryStatus.ASSIGNED
                            || d.getStatus() == DeliveryStatus.IN_TRANSIT)) {
                route.add(d);
            }
        }
        printRoute(route);
    }

    private void printRoute(List<Delivery> route) {
        List<String[]> rows = new ArrayList<>();
        for (Delivery d : route) {
            Customer cust = ctx.customers.stream()
                    .filter(c -> c.getId() == d.getCustomerId()).findFirst().orElse(null);
            rows.add(new String[]{String.format("DL%03d", d.getId()),
                    String.format("S%03d", d.getSubscriptionId()),
                    cust == null ? "Unknown" : cust.getName(),
                    cust == null ? "-" : cust.getAddress(),
                    String.valueOf(d.getStatus()),
                    d.getContainerIds().isEmpty() ? "-" : d.getContainerIds().toString()});
        }
        TablePrinter.print("Today's Assigned Route (" + LocalDate.now() + ")",
                new String[]{"Delivery", "Sub ID", "Customer", "Address", "Status", "Containers"}, rows);
    }

    private List<Delivery> myActiveDeliveries(int driverId) {
        List<Delivery> out = new ArrayList<>();
        for (Delivery d : deliveryService.all()) {
            if (d.getDriverId() != null && d.getDriverId() == driverId
                    && (d.getStatus() == DeliveryStatus.ASSIGNED
                            || d.getStatus() == DeliveryStatus.IN_TRANSIT)) {
                out.add(d);
            }
        }
        return out;
    }

    private void processDelivery(int driverId) throws Exception {
        List<Delivery> mine = myActiveDeliveries(driverId);
        if (mine.isEmpty()) {
            System.out.println("(You have no active deliveries to process.)");
            return;
        }
        printRoute(mine);
        int delId = InputHelper.readId(sc, "Delivery ID: ");
        Delivery d = myDelivery(driverId, delId);
        System.out.println("Containers on this delivery: "
                + (d.getContainerIds().isEmpty() ? "-" : d.getContainerIds()));
        System.out.println("1. Mark as DELIVERED (containers handed over)");
        System.out.println("2. Mark as RETURNED (empty containers collected)");
        String c = InputHelper.readText(sc, "Choose: ");
        if (c.equals("1")) {
            deliveryService.updateStatus(d.getId(), DeliveryStatus.DELIVERED);
            System.out.println("Delivery DL" + String.format("%03d", d.getId()) + " marked DELIVERED.");
        } else if (c.equals("2")) {
            deliveryService.updateStatus(d.getId(), DeliveryStatus.RETURNED);
            System.out.println("Delivery DL" + String.format("%03d", d.getId()) + " marked RETURNED (empties collected).");
        } else {
            System.out.println("  ! Invalid option. Choose 1 or 2.");
        }
    }

    private void reportContainer(int driverId) throws Exception {
        List<Container> mine = new ArrayList<>();
        for (Delivery d : myActiveDeliveries(driverId)) {
            for (int cid : d.getContainerIds()) {
                containerService.findById(cid).ifPresent(mine::add);
            }
        }
        if (mine.isEmpty()) {
            System.out.println("(No containers on your active deliveries.)");
            return;
        }
        List<String[]> rows = new ArrayList<>();
        for (Container ct : mine) {
            rows.add(new String[]{String.format("CT%03d", ct.getId()), ct.getContainerCode(),
                    String.valueOf(ct.getStatus()), String.valueOf(ct.getCondition())});
        }
        TablePrinter.print("Containers On My Deliveries",
                new String[]{"ID", "Code", "Status", "Condition"}, rows);
        int ctId = InputHelper.readId(sc, "Container ID: ");
        Container ct = null;
        for (Container c : mine) {
            if (c.getId() == ctId) { ct = c; break; }
        }
        if (ct == null) throw new Exception("Container is not part of your active deliveries.");
        System.out.println("1. Report DAMAGED");
        System.out.println("2. Report MISSING");
        String c = InputHelper.readText(sc, "Choose: ");
        if (c.equals("1")) {
            System.out.println("Damage: MINOR_DAMAGE, MAJOR_DAMAGE, UNUSABLE");
            ContainerCondition cond = InputHelper.readEnum(sc, "Condition: ", ContainerCondition.class);
            containerService.checkCondition(ct.getId(), cond);
            System.out.println("Damage reported for CT" + String.format("%03d", ct.getId()) + ".");
        } else if (c.equals("2")) {
            if (!InputHelper.confirm(sc, "Report CT" + String.format("%03d", ct.getId()) + " as MISSING?")) {
                System.out.println("Cancelled.");
                return;
            }
            containerService.updateStatus(ct.getId(), ContainerStatus.RETIRED);
            System.out.println("Container reported MISSING and retired from inventory.");
        } else {
            System.out.println("  ! Invalid option. Choose 1 or 2.");
        }
    }

    private Delivery myDelivery(int driverId, int delId) throws Exception {
        Delivery d = deliveryService.findById(delId)
                .orElseThrow(() -> new Exception("Delivery not found (ID " + delId + ")."));
        if (d.getDriverId() == null || d.getDriverId() != driverId)
            throw new Exception("Delivery DL" + String.format("%03d", delId) + " is not assigned to you.");
        if (d.getStatus() != DeliveryStatus.ASSIGNED && d.getStatus() != DeliveryStatus.IN_TRANSIT)
            throw new Exception("Delivery is " + d.getStatus() + " and cannot be processed.");
        return d;
    }
}

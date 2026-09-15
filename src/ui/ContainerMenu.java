package ui;

import model.Container;
import model.ContainerCondition;
import model.ContainerStatus;
import service.AppContext;
import service.ContainerService;
import util.InputHelper;
import util.TablePrinter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ContainerMenu {
    private final ContainerService containerService;
    private final Scanner sc;

    public ContainerMenu(AppContext ctx, Scanner sc) {
        this.containerService = new ContainerService(ctx);
        this.sc = sc;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-- Container Management --");
            System.out.println("1. Container Inventory & CRUD");
            System.out.println("2. Customer Allocation & Tracking");
            System.out.println("3. Condition & Maintenance");
            System.out.println("4. Back to Main Menu");

            String c = InputHelper.readText(sc, "Choose: ");
            switch (c) {
                case "1":
                    showInventoryMenu();
                    break;
                case "2":
                    showTrackingMenu();
                    break;
                case "3":
                    showMaintenanceMenu();
                    break;
                case "4":
                    back = true;
                    break;
                default:
                    System.out.println("  ! Invalid option. Choose 0-3.");
                    break;
            }
        }
    }

    // --- SUB-MENU 1: INVENTORY & CRUD ---
    private void showInventoryMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-- Container Inventory & CRUD --");
            System.out.println("1. Prepare new container");
            System.out.println("2. List all containers");
            System.out.println("3. Update container");
            System.out.println("4. Delete container");
            System.out.println("5. Back");

            String c = InputHelper.readText(sc, "Choose: ");
            try {
                switch (c) {
                    case "1":
                        create();
                        break;
                    case "2":
                        list();
                        break;
                    case "3":
                        update();
                        break;
                    case "4":
                        delete();
                        break;
                    case "5":
                        back = true;
                        break;
                    default:
                        System.out.println("  ! Invalid option. Choose 0-4.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    // --- SUB-MENU 2: ALLOCATION & TRACKING ---
    private void showTrackingMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-- Customer Allocation & Tracking --");
            System.out.println("1. Record container assignment to customer");
            System.out.println("2. Record delivered container");
            System.out.println("3. Record returned container");
            System.out.println("4. Back");

            String c = InputHelper.readText(sc, "Choose: ");
            try {
                switch (c) {
                    case "1":
                        assign();
                        break;
                    case "2":
                        delivered();
                        break;
                    case "3":
                        returned();
                        break;
                    case "4":
                        back = true;
                        break;
                    default:
                        System.out.println("  ! Invalid option. Choose 0-3.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    // --- SUB-MENU 3: CONDITION & MAINTENANCE ---
    private void showMaintenanceMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-- Condition & Maintenance --");
            System.out.println("1. Check / update container condition");
            System.out.println("2. Update container status");
            System.out.println("3. Back");

            String c = InputHelper.readText(sc, "Choose: ");
            try {
                switch (c) {
                    case "1":
                        condition();
                        break;
                    case "2":
                        status();
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

    private void create() throws Exception {
        String code = InputHelper.readText(sc, "Container code (format CNT-0000): ", 8, 8, false).toUpperCase();
        Container ct = containerService.prepareContainer(code);
        System.out.println("Prepared successfully.");
        printContainers(java.util.List.of(ct));
    }

    private void list() {
        printContainers(containerService.all());
    }

    private void update() throws Exception {
        int ctId = InputHelper.readId(sc, "Container ID to update: ");
        Container existing = containerService.findById(ctId)
                .orElseThrow(() -> new Exception("Container not found (ID " + ctId + ")."));
        printContainers(java.util.List.of(existing));
        String code = InputHelper.readText(sc, "New code (format CNT-0000) [" + existing.getContainerCode() + "]: ", 8, 8, false).toUpperCase();
        System.out.println("Conditions: GOOD, MINOR_DAMAGE, MAJOR_DAMAGE, UNUSABLE");
        ContainerCondition cond = InputHelper.readEnum(sc, "New condition [" + existing.getCondition() + "]: ", ContainerCondition.class);
        System.out.println("Statuses: AVAILABLE, ASSIGNED, IN_TRANSIT, DELIVERED, RETURNED, DAMAGED, RETIRED");
        ContainerStatus st = InputHelper.readEnum(sc, "New status [" + existing.getStatus() + "]: ", ContainerStatus.class);
        Container updated = containerService.updateContainer(ctId, code, cond, st);
        System.out.println("Updated successfully.");
        printContainers(java.util.List.of(updated));
    }

    private void printContainers(List<Container> list) {
        String[] headers = {"ID", "Code", "Status", "Condition", "Cust ID", "Sub ID"};
        List<String[]> rows = new ArrayList<>();
        for (Container ct : list) {
            rows.add(new String[]{String.format("CT%03d", ct.getId()), ct.getContainerCode(),
                    String.valueOf(ct.getStatus()), String.valueOf(ct.getCondition()),
                    ct.getAssignedCustomerId() == null ? "-" : String.format("C%03d", ct.getAssignedCustomerId()),
                    ct.getAssignedSubscriptionId() == null ? "-" : String.format("S%03d", ct.getAssignedSubscriptionId())});
        }
        TablePrinter.print("Container List", headers, rows);
    }

    private void assign() throws Exception {
        int ctId = InputHelper.readId(sc, "Container ID: ");
        int custId = InputHelper.readId(sc, "Customer ID: ");
        String subStr = InputHelper.readOptionalText(sc, "Subscription ID (Enter to skip): ", 10);
        Integer subId = subStr.isBlank() ? null : parsePositiveIntOrThrow(subStr, "Subscription ID");
        containerService.assignToCustomer(ctId, custId, subId);
        System.out.println("Container assigned to customer " + custId);
    }

    private void delivered() throws Exception {
        int ctId = InputHelper.readId(sc, "Container ID: ");
        containerService.recordDelivered(ctId);
        System.out.println("Container recorded as DELIVERED.");
    }

    private void returned() throws Exception {
        int ctId = InputHelper.readId(sc, "Container ID: ");
        containerService.recordReturned(ctId);
        System.out.println("Container recorded as RETURNED.");
    }

    private void condition() throws Exception {
        int ctId = InputHelper.readId(sc, "Container ID: ");
        System.out.println("Conditions: GOOD, MINOR_DAMAGE, MAJOR_DAMAGE, UNUSABLE");
        ContainerCondition cond = InputHelper.readEnum(sc, "Condition: ", ContainerCondition.class);
        containerService.checkCondition(ctId, cond);
        System.out.println("Condition updated.");
    }

    private void status() throws Exception {
        int ctId = InputHelper.readId(sc, "Container ID: ");
        System.out.println("Statuses: AVAILABLE, ASSIGNED, IN_TRANSIT, DELIVERED, RETURNED, DAMAGED, RETIRED");
        ContainerStatus st = InputHelper.readEnum(sc, "New status: ", ContainerStatus.class);
        containerService.updateStatus(ctId, st);
        System.out.println("Status updated.");
    }

    private void delete() throws Exception {
        int ctId = InputHelper.readId(sc, "Container ID to delete: ");
        if (!InputHelper.confirm(sc, "Delete container CT" + String.format("%03d", ctId) + "?")) {
            System.out.println("Cancelled.");
            return;
        }
        containerService.deleteContainer(ctId);
        System.out.println("Container deleted.");
    }

    private int parsePositiveIntOrThrow(String raw, String field) throws Exception {
        try {
            int v = Integer.parseInt(raw.trim());
            if (v < 1) throw new Exception(field + " must be a positive number.");
            return v;
        } catch (NumberFormatException e) {
            throw new Exception(field + " must be digits only.");
        }
    }
}
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
            System.out.println("1. Prepare new container (Create)");
            System.out.println("2. List all containers (Read)");
            System.out.println("3. Find container by code (Read)");
            System.out.println("4. Record container assignment to customer (Update)");
            System.out.println("5. Record delivered container (Update)");
            System.out.println("6. Record returned container (Update)");
            System.out.println("7. Check / update container condition (Update)");
            System.out.println("8. Update container status (Update)");
            System.out.println("9. Delete container (Delete)");
            System.out.println("0. Back");
            String c = InputHelper.readText(sc, "Choose: ");
            try {
                switch (c) {
                    case "1": create(); break;
                    case "2": list(); break;
                    case "3": find(); break;
                    case "4": assign(); break;
                    case "5": delivered(); break;
                    case "6": returned(); break;
                    case "7": condition(); break;
                    case "8": status(); break;
                    case "9": delete(); break;
                    case "0": back = true; break;
                    default: System.out.println("  ! Invalid option. Choose 0-9.");
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void create() throws Exception {
        String code = InputHelper.readText(sc, "Container code (format CNT-0000): ", 8, 8, false).toUpperCase();
        Container ct = containerService.prepareContainer(code);
        System.out.println("Prepared successfully.");
        printContainers(java.util.List.of(ct));
    }

    private void list() {
        printContainers(containerService.all());
    }

    private void find() {
        String code = InputHelper.readText(sc, "Container code (e.g. CNT-0001): ");
        containerService.findByCode(code).ifPresentOrElse(
                ct -> printContainers(java.util.List.of(ct)),
                () -> System.out.println("  ! Container not found (" + code.toUpperCase() + ")."));
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

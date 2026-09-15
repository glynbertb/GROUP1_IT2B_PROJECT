package ui;

import model.Driver;
import service.AppContext;
import service.DriverService;
import util.InputHelper;
import util.TablePrinter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DriverMenu {
    private final DriverService driverService;
    private final Scanner sc;

    public DriverMenu(AppContext ctx, Scanner sc) {
        this.driverService = new DriverService(ctx);
        this.sc = sc;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-- Driver Management --");
            System.out.println("1. Add driver (Create)");
            System.out.println("2. List all drivers (Read)");
            System.out.println("3. Search driver by name (Read)");
            System.out.println("4. View driver by ID (Read)");
            System.out.println("5. Update driver (Update)");
            System.out.println("6. Remove driver (Delete)");
            System.out.println("0. Back");
            String c = InputHelper.readText(sc, "Choose: ");
            try {
                switch (c) {
                    case "1": create(); break;
                    case "2": list(); break;
                    case "3": search(); break;
                    case "4": view(); break;
                    case "5": update(); break;
                    case "6": delete(); break;
                    case "0": back = true; break;
                    default: System.out.println("  ! Invalid option. Choose 0-6.");
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void create() throws Exception {
        String name = InputHelper.readText(sc, "Name (2-60 chars): ", 2, 60, false);
        String contact = InputHelper.readContact(sc, "Contact: ", false);
        String license = InputHelper.readText(sc, "License number (3-30 chars): ", 3, 30, false);
        Driver added = driverService.add(name, contact, license);
        System.out.println("Added successfully.");
        printDrivers(java.util.List.of(added));
    }

    private void list() {
        printDrivers(driverService.all());
    }

    private void search() {
        String kw = InputHelper.readText(sc, "Search name keyword: ");
        printDrivers(driverService.searchByName(kw));
    }

    private void view() {
        int id = InputHelper.readId(sc, "Driver ID: ");
        driverService.findById(id).ifPresentOrElse(
                d -> printDrivers(java.util.List.of(d)),
                () -> System.out.println("  ! Driver not found (ID " + id + ")."));
    }

    private void printDrivers(List<Driver> list) {
        String[] headers = {"ID", "Name", "Contact", "License", "Status"};
        List<String[]> rows = new ArrayList<>();
        for (Driver d : list) {
            rows.add(new String[]{String.format("D%03d", d.getId()), d.getName(),
                    d.getContactNumber(), d.getLicenseNumber(),
                    d.isAvailable() ? "AVAILABLE" : "BUSY"});
        }
        TablePrinter.print("Driver List", headers, rows);
    }

    private void update() throws Exception {
        int id = InputHelper.readId(sc, "Driver ID to update: ");
        Driver existing = driverService.findById(id)
                .orElseThrow(() -> new Exception("Driver not found (ID " + id + ")."));
        printDrivers(java.util.List.of(existing));
        String name = InputHelper.readText(sc, "New name [" + existing.getName() + "]: ", 2, 60, false);
        String contact = InputHelper.readContact(sc, "New contact [" + existing.getContactNumber() + "]: ", false);
        String license = InputHelper.readText(sc, "New license [" + existing.getLicenseNumber() + "]: ", 3, 30, false);
        Driver updated = driverService.update(id, name, contact, license);
        System.out.println("Updated successfully.");
        printDrivers(java.util.List.of(updated));
    }

    private void delete() throws Exception {
        int id = InputHelper.readId(sc, "Driver ID to remove: ");
        if (!InputHelper.confirm(sc, "Remove driver D" + String.format("%03d", id) + "?")) {
            System.out.println("Cancelled.");
            return;
        }
        driverService.remove(id);
        System.out.println("Driver removed.");
    }
}

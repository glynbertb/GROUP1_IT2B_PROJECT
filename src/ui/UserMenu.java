package ui;

import model.Customer;
import model.Driver;
import model.Role;
import model.User;
import service.AppContext;
import service.CustomerService;
import service.DriverService;
import service.UserService;
import util.InputHelper;
import util.TablePrinter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UserMenu {
    private final UserService userService;
    private final CustomerService customerService;
    private final DriverService driverService;
    private final Scanner sc;

    public UserMenu(AppContext ctx, Scanner sc) {
        this.userService = new UserService(ctx);
        this.customerService = new CustomerService(ctx);
        this.driverService = new DriverService(ctx);
        this.sc = sc;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-- User Account Management (Admin) --");
            System.out.println("1. List all accounts");
            System.out.println("2. Create account (e.g. delivery personnel)");
            System.out.println("3. Update account role");
            System.out.println("0. Back");
            String c = InputHelper.readText(sc, "Choose: ");
            try {
                switch (c) {
                    case "1": list(); break;
                    case "2": create(); break;
                    case "3": updateRole(); break;
                    case "0": back = true; break;
                    default: System.out.println("  ! Invalid option. Choose 0-3.");
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void list() {
        String[] headers = {"ID", "Username", "Full Name", "Role", "Linked", "Source"};
        List<String[]> rows = new ArrayList<>();
        for (User u : userService.all()) {
            rows.add(new String[]{String.valueOf(u.getId()), u.getUsername(),
                    u.getFullName(), String.valueOf(u.getRole()), linkedLabel(u), sourceLabel(u)});
        }
        TablePrinter.print("System Accounts", headers, rows);
    }

    private String sourceLabel(User u) {
        if (u.getUsername().equalsIgnoreCase("admin")
                || u.getUsername().equalsIgnoreCase("delivery")) return "BUILT-IN";
        return "NEW";
    }

    private String linkedLabel(User u) {
        if (u.getCustomerId() != null) return String.format("C%03d", u.getCustomerId());
        if (u.getDriverId() != null) return String.format("D%03d", u.getDriverId());
        return "-";
    }

    private void create() throws Exception {
        String username = InputHelper.readText(sc, "Username (3-20 chars, letters/digits/./_): ", 3, 20, false);
        System.out.print("Password (4-30 chars): ");
        String password = sc.nextLine().trim();
        String fullName = InputHelper.readText(sc, "Full name (2-60 chars): ", 2, 60, false);
        System.out.println("Roles: ADMIN, DELIVERY_PERSONNEL, CUSTOMER");
        Role role = InputHelper.readEnum(sc, "Role: ", Role.class);
        User u = userService.createAccount(username, password, fullName, role);
        linkAccount(u);
        System.out.println("Account created for '" + u.getUsername() + "' [" + u.getRole() + "].");
    }

    private void updateRole() throws Exception {
        list();
        int id = InputHelper.readId(sc, "Account ID to update: ");
        System.out.println("Roles: ADMIN, DELIVERY_PERSONNEL, CUSTOMER");
        Role role = InputHelper.readEnum(sc, "New role: ", Role.class);
        if (!InputHelper.confirm(sc, "Set account " + id + " to " + role + "?")) {
            System.out.println("Cancelled.");
            return;
        }
        User updated = userService.updateRole(id, role);
        updated.setCustomerId(null);
        updated.setDriverId(null);
        linkAccount(updated);
        System.out.println("Updated: " + updated.getUsername() + " is now " + updated.getRole() + ".");
    }

    /** Auto-creates the matching customer/driver record so every account gets its own unique linked ID. */
    private void linkAccount(User u) throws Exception {
        if (u.getRole() == Role.CUSTOMER) {
            System.out.println("Creating a customer record for '" + u.getUsername() + "'.");
            String address = InputHelper.readText(sc, "Address (5-120 chars): ", 5, 120, false);
            String contact = InputHelper.readContact(sc, "Contact number: ", false);
            String email = InputHelper.readEmail(sc, "Email: ", false);
            Customer c = customerService.register(u.getFullName(), address, contact, email);
            u.setCustomerId(c.getId());
            System.out.println("Created customer C" + String.format("%03d", c.getId())
                    + " and linked it to '" + u.getUsername() + "'.");
        } else if (u.getRole() == Role.DELIVERY_PERSONNEL) {
            System.out.println("Creating a driver record for '" + u.getUsername() + "'.");
            String contact = InputHelper.readContact(sc, "Contact number: ", false);
            String license = InputHelper.readText(sc, "License no. (3-30 chars): ", 3, 30, false);
            Driver d = driverService.add(u.getFullName(), contact, license);
            u.setDriverId(d.getId());
            System.out.println("Created driver D" + String.format("%03d", d.getId())
                    + " and linked it to '" + u.getUsername() + "'.");
        }
    }
}

package ui;

import model.Customer;
import service.AppContext;
import service.CustomerService;
import util.InputHelper;
import util.TablePrinter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CustomerMenu {
    private final CustomerService customerService;
    private final Scanner sc;

    public CustomerMenu(AppContext ctx, Scanner sc) {
        this.customerService = new CustomerService(ctx);
        this.sc = sc;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-- Customer Management --");
            System.out.println("1. Register new customer");
            System.out.println("2. List all customers");
            System.out.println("3. Update customer");
            System.out.println("4. Delete customer");
            System.out.println("5. Back");
            String c = InputHelper.readText(sc, "Choose: ");
            try {
                switch (c) {
                    case "1": create(); break;
                    case "2": list(); break;
                    case "3": update(); break;
                    case "4": delete(); break;
                    case "5": back = true; break;
                    default: System.out.println("  ! Invalid option. Choose 0-6.");
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void create() throws Exception {
        String name = InputHelper.readText(sc, "Name : ", 0, 60, false);
        String address = InputHelper.readText(sc, "Address : ", 0, 120, false);
        String contact = InputHelper.readContact(sc, "Contact number: ", false);
        String email = InputHelper.readEmail(sc, "Email: ", false);
        Customer cust = customerService.register(name, address, contact, email);
        System.out.println("Registered successfully.");
        printCustomers(java.util.List.of(cust));
    }

    private void list() {
        printCustomers(customerService.all());
    }


    private void printCustomers(List<Customer> list) {
        String[] headers = {"ID", "Name", "Contact", "Email", "Address", "Registered"};
        List<String[]> rows = new ArrayList<>();
        for (Customer cu : list) {
            rows.add(new String[]{
                    String.format("C%03d", cu.getId()), cu.getName(), cu.getContactNumber(),
                    cu.getEmail(), cu.getAddress(), String.valueOf(cu.getRegisteredDate())});
        }
        TablePrinter.print("Customer List", headers, rows);
    }

    private void update() throws Exception {
        int id = InputHelper.readId(sc, "Customer ID to update: ");
        Customer existing = customerService.findById(id)
                .orElseThrow(() -> new Exception("Customer not found (ID " + id + ")."));
        printCustomers(java.util.List.of(existing));
        System.out.println("(Press Enter with no text is NOT allowed here - retype the value.)");
        String name = InputHelper.readText(sc, "New name [" + existing.getName() + "]: ", 0, 60, false);
        String address = InputHelper.readText(sc, "New address [" + existing.getAddress() + "]: ", 0, 120, false);
        String contact = InputHelper.readContact(sc, "New contact [" + existing.getContactNumber() + "]: ", false);
        String email = InputHelper.readEmail(sc, "New email [" + existing.getEmail() + "]: ", false);
        Customer updated = customerService.update(id, name, address, contact, email);
        System.out.println("Updated successfully.");
        printCustomers(java.util.List.of(updated));
    }

    private void delete() throws Exception {
        int id = InputHelper.readId(sc, "Customer ID to delete: ");
        if (!InputHelper.confirm(sc, "Delete customer C" + String.format("%03d", id) + "?")) {
            System.out.println("Cancelled.");
            return;
        }
        customerService.delete(id);
        System.out.println("Customer deleted.");
    }
}

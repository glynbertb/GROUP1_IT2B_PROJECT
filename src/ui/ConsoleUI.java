package ui;

import model.User;
import service.AppContext;
import service.AuthService;
import java.util.Scanner;

public class ConsoleUI {
    private final AppContext ctx;
    private final Scanner sc = new Scanner(System.in);
    private final AuthService authService;

    private final CustomerMenu customerMenu;
    private final SubscriptionMenu subscriptionMenu;
    private final DeliveryMenu deliveryMenu;
    private final ContainerMenu containerMenu;
    private final PaymentMenu paymentMenu;
    private final DriverMenu driverMenu;
    private final ReportMenu reportMenu;

    public ConsoleUI(AppContext ctx) {
        this.ctx = ctx;
        this.authService = new AuthService(ctx);

        this.customerMenu = new CustomerMenu(ctx, sc);
        this.subscriptionMenu = new SubscriptionMenu(ctx, sc);
        this.deliveryMenu = new DeliveryMenu(ctx, sc);
        this.containerMenu = new ContainerMenu(ctx, sc);
        this.paymentMenu = new PaymentMenu(ctx, sc);
        this.driverMenu = new DriverMenu(ctx, sc);
        this.reportMenu = new ReportMenu(ctx, sc);
    }

    public void run() {
        System.out.println("============================================================");
        System.out.println(" SUBSCRIPTION DELIVERY & CONTAINER ASSET TRACKING SYSTEM");
        System.out.println("============================================================");

        if (!login()) {
            System.out.println("Too many failed attempts. Exiting.");
            return;
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = prompt("Choose an option: ");
            try {
                switch (choice) {
                    case "1": customerMenu.show(); break;
                    case "2": subscriptionMenu.show(); break;
                    case "3": deliveryMenu.show(); break;
                    case "4": containerMenu.show(); break;
                    case "5": paymentMenu.show(); break;
                    case "6": reportMenu.show(); break;
                    case "7": driverMenu.show(); break;
                    case "0":
                        System.out.println("Logging out. Goodbye, " + ctx.currentUser.getFullName() + "!");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private boolean login() {
        int attempts = 0;
        while (attempts < 3) {
            String u = prompt("Username: ");
            String p = prompt("Password: ");
            User user = authService.login(u, p);
            if (user != null) {
                System.out.println("Login successful. Welcome, " + user.getFullName() + " [" + user.getRole() + "]\n");
                return true;
            }
            attempts++;
            System.out.println("Invalid credentials. Attempts left: " + (3 - attempts));
        }
        return false;
    }

    private void printMainMenu() {
        System.out.println("--------------------------------------------------");
        System.out.println("MAIN MENU  (logged in as " + ctx.currentUser.getUsername() + ")");
        System.out.println("1. Customer Management");
        System.out.println("2. Subscription Management");
        System.out.println("3. Delivery Management");
        System.out.println("4. Container Management");
        System.out.println("5. Payment Management");
        System.out.println("6. Reports");
        System.out.println("7. Driver Management");
        System.out.println("0. Logout / Exit");
        System.out.println("--------------------------------------------------");
    }

    private String prompt(String label) {
        System.out.print(label);
        return sc.nextLine().trim();
    }
}
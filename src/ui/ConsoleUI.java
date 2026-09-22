package ui;

import model.Customer;
import model.Role;
import model.User;
import service.AppContext;
import service.AuthService;
import service.CustomerService;
import service.UserService;
import util.DeliveryReceipt;
import util.InputHelper;
import java.util.Scanner;

public class ConsoleUI {
    private final AppContext ctx;
    private final Scanner sc = new Scanner(System.in);
    private final AuthService authService;
    private final UserService userService;
    private final CustomerService customerService;

    private final CustomerMenu customerMenu;
    private final SubscriptionMenu subscriptionMenu;
    private final DeliveryMenu deliveryMenu;
    private final ContainerMenu containerMenu;
    private final PaymentMenu paymentMenu;
    private final DriverMenu driverMenu;
    private final ReportMenu reportMenu;
    private final UserMenu userMenu;

    public ConsoleUI(AppContext ctx) {
        this.ctx = ctx;
        this.authService = new AuthService(ctx);
        this.userService = new UserService(ctx);
        this.customerService = new CustomerService(ctx);

        this.customerMenu = new CustomerMenu(ctx, sc);
        this.subscriptionMenu = new SubscriptionMenu(ctx, sc);
        this.deliveryMenu = new DeliveryMenu(ctx, sc);
        this.containerMenu = new ContainerMenu(ctx, sc);
        this.paymentMenu = new PaymentMenu(ctx, sc);
        this.driverMenu = new DriverMenu(ctx, sc);
        this.reportMenu = new ReportMenu(ctx, sc);
        this.userMenu = new UserMenu(ctx, sc);
    }

    public void run() {
        System.out.println("============================================================");
        System.out.println(" SUBSCRIPTION DELIVERY & CONTAINER ASSET TRACKING SYSTEM");
        System.out.println("============================================================");

        boolean appRunning = true;
        while (appRunning) {
            printWelcomeMenu();
            String choice = InputHelper.readText(sc, "Choose an option: ");
            switch (choice) {
                case "1":
                    if (login()) routeByRole();
                    break;
                case "2":
                    signUp();
                    break;
                case "0":
                    System.out.println("Thank you for using the system. Goodbye!");
                    appRunning = false;
                    break;
                default:
                    System.out.println("  ! Invalid option. Choose 1, 2, or 0.");
            }
        }
    }

    /** Sends each login to its own interface. The main menu below serves as the admin UI. */
    private void routeByRole() {
        Role role = ctx.currentUser.getRole();
        if (role == Role.ADMIN) {
            mainLoop();
        } else if (role == Role.CUSTOMER) {
            new CustomerPortal(ctx, sc).show();
        } else {
            new DriverPortal(ctx, sc).show();
        }
    }

    private void mainLoop() {
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = InputHelper.readText(sc, "Choose an option: ");
            try {
                switch (choice) {
                    case "1": customerMenu.show(); break;
                    case "2": subscriptionMenu.show(); break;
                    case "3": deliveryMenu.show(); break;
                    case "4": containerMenu.show(); break;
                    case "5": paymentMenu.show(); break;
                    case "6": driverMenu.show(); break;
                    case "7": reportMenu.show(); break;
                    case "8":
                        if (ctx.currentUser.getRole() == Role.ADMIN) userMenu.show();
                        else System.out.println("  ! User Management is restricted to administrators.");
                        break;
                    case "9":
                        int delId = InputHelper.readId(sc, "Delivery ID: ");
                        DeliveryReceipt.print(ctx, delId);
                        break;
                    case "0":
                        System.out.println("Logging out. Goodbye, " + ctx.currentUser.getFullName() + "!");
                        authService.logout();
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
            String u = InputHelper.readText(sc, "Username (or 0 to cancel): ");
            if (u.equals("0")) return false;
            System.out.print("Password: ");
            String p = sc.nextLine().trim();
            if (p.isEmpty()) {
                System.out.println("  ! Password cannot be blank.");
                continue;
            }
            User user = authService.login(u, p);
            if (user != null) {
                System.out.println("Login successful. Welcome, " + user.getFullName() + " [" + user.getRole() + "]\n");
                return true;
            }
            attempts++;
            System.out.println("Invalid credentials. Attempts left: " + (3 - attempts));
        }
        System.out.println("Too many failed attempts. Returning to welcome menu.");
        return false;
    }

    private void signUp() {
        System.out.println("\n-- Create New Account --");
        System.out.println("(New accounts are CUSTOMER accounts.)");
        try {
            String username = InputHelper.readText(sc, "Choose a username : ", 0, 20, false);
            System.out.print("Choose a password : ");
            String password = sc.nextLine().trim();
            String fullName = InputHelper.readText(sc, "Full name : ", 0, 60, false);
            String address = InputHelper.readText(sc, "Address : ", 0, 120, false);
            String contact = InputHelper.readContact(sc, "Contact number : ", false);
            String email = InputHelper.readEmail(sc, "Email : ", false);
            User u = userService.signUp(username, password, fullName);
            Customer c = customerService.register(fullName, address, contact, email);
            u.setCustomerId(c.getId());
            System.out.println("Account created for '" + u.getUsername() + "' [" + u.getRole() + "]. You may now log in.");
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void printWelcomeMenu() {
        System.out.println("--------------------------------------------------");
        System.out.println("              WELCOME TO OUR SYSTEM               ");
        System.out.println("1. Log in");
        System.out.println("2. Sign up ");
        System.out.println("0. Exit");
        System.out.println("--------------------------------------------------");
    }

    private void printMainMenu() {
        System.out.println("--------------------------------------------------");
        System.out.println("MAIN MENU  (logged in as " + ctx.currentUser.getUsername()
                + " [" + ctx.currentUser.getRole() + "])");
        System.out.println("1. Customer Management");
        System.out.println("2. Subscription Management");
        System.out.println("3. Delivery Management");
        System.out.println("4. Container Management");
        System.out.println("5. Payment Management");
        System.out.println("6. Driver Management");
        System.out.println("7. Reports");
        if (ctx.currentUser.getRole() == Role.ADMIN) {
            System.out.println("8. User Management (Admin)");
        }
        System.out.println("9. Print delivery receipt");
        System.out.println("0. Logout");
        System.out.println("--------------------------------------------------");
    }

    private String prompt(String label) {
        System.out.print(label);
        return sc.nextLine().trim();
    }
}

package ui;

import model.Payment;
import service.AppContext;
import service.PaymentService;
import service.SubscriptionService;
import util.InputHelper;
import util.TablePrinter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class PaymentMenu {
    private final PaymentService paymentService;
    private final Scanner sc;

    public PaymentMenu(AppContext ctx, Scanner sc) {
        SubscriptionService subService = new SubscriptionService(ctx);
        this.paymentService = new PaymentService(ctx, subService);
        this.sc = sc;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-- Payment Management --");
            System.out.println("1. Record subscription payment (Create)");
            System.out.println("2. List all payments (Read)");
            System.out.println("3. List payments for a subscription (Read)");
            System.out.println("4. Calculate remaining balance (Read)");
            System.out.println("5. Void a payment (Delete)");
            System.out.println("0. Back");
            String c = InputHelper.readText(sc, "Choose: ");
            try {
                switch (c) {
                    case "1": record(); break;
                    case "2": listAll(); break;
                    case "3": listBySub(); break;
                    case "4": balance(); break;
                    case "5": voidPayment(); break;
                    case "0": back = true; break;
                    default: System.out.println("  ! Invalid option. Choose 0-5.");
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void record() throws Exception {
        int subId = InputHelper.readId(sc, "Subscription ID: ");
        double balance = paymentService.remainingBalance(subId);
        System.out.printf("Current balance: %.2f%n", balance);
        if (balance <= 0) {
            System.out.println("  ! Already fully paid. No payment needed.");
            return;
        }
        double amount = InputHelper.readDouble(sc, "Amount (0.01-" + String.format("%.2f", balance) + "): ", 0.01, balance);
        String method = InputHelper.readOption(sc, "Method (CASH/GCASH/CARD/BANK): ",
                Set.of("CASH", "GCASH", "CARD", "BANK"));
        Payment p = paymentService.recordPayment(subId, amount, method);
        System.out.printf("Payment recorded [PMT%03d]. Remaining balance: %.2f%n",
                p.getId(), paymentService.remainingBalance(subId));
    }

    private void listAll() {
        printPayments("Payment List", paymentService.all());
    }

    private void listBySub() throws Exception {
        int subId = InputHelper.readId(sc, "Subscription ID: ");
        paymentService.remainingBalance(subId); // validates subscription exists
        printPayments("Payments for S" + String.format("%03d", subId), paymentService.byline(subId));
    }

    private void balance() throws Exception {
        int subId = InputHelper.readId(sc, "Subscription ID: ");
        double bal = paymentService.remainingBalance(subId);
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{String.format("S%03d", subId), String.format("%.2f", bal)});
        TablePrinter.print("Remaining Balance",
                new String[]{"Subscription", "Balance"}, rows);
    }

    private void voidPayment() throws Exception {
        int id = InputHelper.readId(sc, "Payment ID to void (e.g. 1 for PMT001): ");
        if (!InputHelper.confirm(sc, "Void payment PMT" + String.format("%03d", id) + "? Balance will be restored.")) {
            System.out.println("Cancelled.");
            return;
        }
        paymentService.voidPayment(id);
        System.out.println("Payment voided and balance restored.");
    }

    private void printPayments(String title, List<Payment> list) {
        String[] headers = {"ID", "Sub ID", "Amount", "Method", "Date"};
        List<String[]> rows = new ArrayList<>();
        for (Payment p : list) {
            rows.add(new String[]{String.format("PMT%03d", p.getId()),
                    String.format("S%03d", p.getSubscriptionId()),
                    String.format("%.2f", p.getAmount()), p.getMethod(), String.valueOf(p.getDate())});
        }
        TablePrinter.print(title, headers, rows);
    }
}

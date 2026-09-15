package ui;

import service.AppContext;
import service.ReportService;
import util.InputHelper;
import java.util.Scanner;

public class ReportMenu {
    private final ReportService reportService;
    private final Scanner sc;

    public ReportMenu(AppContext ctx, Scanner sc) {
        this.reportService = new ReportService(ctx);
        this.sc = sc;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-- Reports (Read-only) --");
            System.out.println("1. Delivery report");
            System.out.println("2. Subscription report");
            System.out.println("3. Container report");
            System.out.println("4. All reports");
            System.out.println("0. Back");
            String c = InputHelper.readText(sc, "Choose: ");
            switch (c) {
                case "1": reportService.deliveryReport(); break;
                case "2": reportService.subscriptionReport(); break;
                case "3": reportService.containerReport(); break;
                case "4":
                    reportService.deliveryReport();
                    reportService.subscriptionReport();
                    reportService.containerReport();
                    break;
                case "0": back = true; break;
                default: System.out.println("  ! Invalid option. Choose 0-4.");
            }
        }
    }
}

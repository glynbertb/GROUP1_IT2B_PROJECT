package service;

import model.*;
import util.TablePrinter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ReportService {
    private final AppContext ctx;

    public ReportService(AppContext ctx) {
        this.ctx = ctx;
    }

    public void deliveryReport() {
        String[] headers = {"ID", "Sub ID", "Customer", "Driver", "Date", "Status", "Containers"};
        List<String[]> rows = new ArrayList<>();
        for (Delivery d : ctx.deliveries) {
            String custName = ctx.customers.stream().filter(c -> c.getId() == d.getCustomerId())
                    .findFirst().map(Customer::getName).orElse("Unknown");
            String drvName = d.getDriverId() == null ? "-" :
                    ctx.drivers.stream().filter(x -> x.getId() == d.getDriverId())
                            .findFirst().map(Driver::getName).orElse("Unknown");
            rows.add(new String[]{String.format("DL%03d", d.getId()),
                    String.format("S%03d", d.getSubscriptionId()), custName, drvName,
                    String.valueOf(d.getScheduledDate()), String.valueOf(d.getStatus()),
                    String.valueOf(d.getContainerIds().size())});
        }
        TablePrinter.print("Delivery Report", headers, rows);
    }

    public void subscriptionReport() {
        String[] headers = {"ID", "Customer", "Plan", "Qty", "Total", "Paid", "Balance", "Status"};
        List<String[]> rows = new ArrayList<>();
        double grandTotal = 0, grandPaid = 0;
        for (Subscription s : ctx.subscriptions) {
            String custName = ctx.customers.stream().filter(c -> c.getId() == s.getCustomerId())
                    .findFirst().map(Customer::getName).orElse("Unknown");
            rows.add(new String[]{String.format("S%03d", s.getId()), custName,
                    String.format("P%03d", s.getPlanId()), String.valueOf(s.getQuantity()),
                    String.format("%.2f", s.getTotalAmount()), String.format("%.2f", s.getAmountPaid()),
                    String.format("%.2f", s.getRemainingBalance()), String.valueOf(s.getStatus())});
            grandTotal += s.getTotalAmount();
            grandPaid += s.getAmountPaid();
        }
        TablePrinter.print("Subscription Report", headers, rows);
        String[] sumHeaders = {"Total Billed", "Total Collected", "Outstanding"};
        List<String[]> sumRows = new ArrayList<>();
        sumRows.add(new String[]{String.format("%.2f", grandTotal),
                String.format("%.2f", grandPaid), String.format("%.2f", grandTotal - grandPaid)});
        TablePrinter.print("Subscription Summary", sumHeaders, sumRows);
    }

    public void containerReport() {
        String[] headers = {"ID", "Code", "Status", "Condition", "Customer"};
        List<String[]> rows = new ArrayList<>();
        Map<ContainerStatus, Integer> counts = new EnumMap<>(ContainerStatus.class);
        for (Container c : ctx.containers) {
            rows.add(new String[]{String.format("CT%03d", c.getId()), c.getContainerCode(),
                    String.valueOf(c.getStatus()), String.valueOf(c.getCondition()),
                    c.getAssignedCustomerId() == null ? "-"
                            : String.format("C%03d", c.getAssignedCustomerId())});
            counts.merge(c.getStatus(), 1, Integer::sum);
        }
        TablePrinter.print("Container Report", headers, rows);
        String[] sumHeaders = {"Status", "Count"};
        List<String[]> sumRows = new ArrayList<>();
        for (Map.Entry<ContainerStatus, Integer> e : counts.entrySet()) {
            sumRows.add(new String[]{String.valueOf(e.getKey()), String.valueOf(e.getValue())});
        }
        TablePrinter.print("Containers by Status", sumHeaders, sumRows);
    }
}

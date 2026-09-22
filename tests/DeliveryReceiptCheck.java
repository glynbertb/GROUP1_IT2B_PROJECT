import model.Container;
import model.Customer;
import model.Delivery;
import model.Payment;
import model.Subscription;
import model.SubscriptionPlan;
import service.AppContext;
import service.ContainerService;
import service.CustomerService;
import service.DeliveryService;
import service.DriverService;
import service.PaymentService;
import service.SubscriptionService;
import util.DeliveryReceipt;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;

/**
 * Focused regression check for DeliveryReceipt: a container assigned at
 * customer/account level (Container Management path) and a container assigned
 * to the delivery itself (Delivery Management path) must both appear on the
 * printed receipt. No test framework required; run with:
 *   javac -d out -sourcepath src:test tests/DeliveryReceiptCheck.java
 *   java -cp out DeliveryReceiptCheck
 * Exits non-zero with a message when any expectation fails.
 */
public class DeliveryReceiptCheck {
    public static void main(String[] args) throws Exception {
        AppContext ctx = new AppContext();
        CustomerService customers = new CustomerService(ctx);
        SubscriptionService subs = new SubscriptionService(ctx);
        ContainerService containers = new ContainerService(ctx);
        DriverService drivers = new DriverService(ctx);
        DeliveryService deliveries = new DeliveryService(ctx, drivers, containers);
        PaymentService payments = new PaymentService(ctx, subs);

        Customer c = customers.register("Receipt Test", "123 Test Street",
                "0917-111-2222", "receipt@test.com");
        SubscriptionPlan plan = subs.allPlans().get(0);
        Subscription s = subs.createSubscription(c.getId(), plan.getId(), 2);
        Delivery d = deliveries.schedule(s.getId(), c.getId(), LocalDate.now().plusDays(1));

        // Path 1 (reported bug): assigned at account level only, not to the delivery.
        Container accountCt = containers.all().get(0);
        containers.assignToCustomer(accountCt.getId(), c.getId(), s.getId());
        String out = capture(ctx, d.getId());
        mustContain(out, accountCt.getContainerCode(),
                "account-level container missing from receipt");

        // Path 2: assigned directly to the delivery.
        Container deliveryCt = containers.all().get(1);
        deliveries.assignContainer(d.getId(), deliveryCt.getId());
        String out2 = capture(ctx, d.getId());
        mustContain(out2, deliveryCt.getContainerCode(),
                "delivery-level container missing from receipt");
        mustContain(out2, "CONTAINERS (1)",
                "delivery container count wrong on receipt");

        // Payment linked to the subscription must also be summarized.
        payments.recordPayment(s.getId(), plan.getUnitPrice(), "CASH");
        String out3 = capture(ctx, d.getId());
        mustContain(out3, "PMT001", "payment missing from receipt");

        System.out.println("All DeliveryReceipt checks passed.");
    }

    private static String capture(AppContext ctx, int deliveryId) throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buf));
        try {
            DeliveryReceipt.print(ctx, deliveryId);
        } finally {
            System.setOut(original);
        }
        return buf.toString("UTF-8");
    }

    private static void mustContain(String haystack, String needle, String problem) {
        if (!haystack.contains(needle)) {
            System.out.println("FAIL: " + problem + " (expected <" + needle + ">). Receipt was:");
            System.out.println(haystack);
            System.exit(1);
        }
    }
}

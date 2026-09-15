package service;

import model.Payment;
import model.Subscription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PaymentService {
    public static final Set<String> METHODS =
            new HashSet<>(Arrays.asList("CASH", "GCASH", "CARD", "BANK"));

    private final AppContext ctx;
    private final SubscriptionService subService;

    public PaymentService(AppContext ctx, SubscriptionService subService) {
        this.ctx = ctx;
        this.subService = subService;
    }

    public Payment recordPayment(int subscriptionId, double amount, String method) throws Exception {
        Subscription s = subService.findById(subscriptionId)
                .orElseThrow(() -> new Exception("Subscription not found (ID " + subscriptionId + ")."));
        if (amount <= 0) throw new Exception("Payment amount must be greater than zero.");
        if (amount > 1_000_000) throw new Exception("Payment amount looks too large (max 1000000).");
        String m = method == null ? "" : method.trim().toUpperCase();
        if (!METHODS.contains(m))
            throw new Exception("Invalid method. Allowed: CASH/GCASH/CARD/BANK.");
        if (s.getRemainingBalance() <= 0)
            throw new Exception("Subscription is already fully paid.");
        if (amount > s.getRemainingBalance())
            throw new Exception(String.format("Amount exceeds balance (balance: %.2f).", s.getRemainingBalance()));

        Payment p = new Payment(ctx.paymentSeq++, subscriptionId, amount, m);
        ctx.payments.add(p);
        s.addPayment(amount);
        return p;
    }

    public void voidPayment(int paymentId) throws Exception {
        Payment p = ctx.payments.stream().filter(x -> x.getId() == paymentId).findFirst()
                .orElseThrow(() -> new Exception("Payment not found (ID " + paymentId + ")."));
        subService.findById(p.getSubscriptionId()).ifPresent(s -> s.addPayment(-p.getAmount()));
        ctx.payments.remove(p);
    }

    public double remainingBalance(int subscriptionId) throws Exception {
        Subscription s = subService.findById(subscriptionId)
                .orElseThrow(() -> new Exception("Subscription not found (ID " + subscriptionId + ")."));
        return s.getRemainingBalance();
    }

    public List<Payment> byline(int subscriptionId) {
        List<Payment> out = new ArrayList<>();
        for (Payment p : ctx.payments) {
            if (p.getSubscriptionId() == subscriptionId) out.add(p);
        }
        return out;
    }

    public List<Payment> all() {
        return ctx.payments;
    }
}

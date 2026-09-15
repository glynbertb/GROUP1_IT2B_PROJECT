package service;

import model.*;
import java.util.ArrayList;
import java.util.List;

public class AppContext {
    public List<User> users = new ArrayList<>();
    public List<Customer> customers = new ArrayList<>();
    public List<SubscriptionPlan> plans = new ArrayList<>();
    public List<Subscription> subscriptions = new ArrayList<>();
    public List<Driver> drivers = new ArrayList<>();
    public List<Container> containers = new ArrayList<>();
    public List<Delivery> deliveries = new ArrayList<>();
    public List<Payment> payments = new ArrayList<>();

    public int userSeq = 1;
    public int custSeq = 1;
    public int planSeq = 1;
    public int subSeq = 1;
    public int driverSeq = 1;
    public int containerSeq = 1;
    public int deliverySeq = 1;
    public int paymentSeq = 1;

    public User currentUser;

    public AppContext() {
        seed();
    }

    private void seed() {
        users.add(new User(userSeq++, "admin", "admin123", Role.ADMIN, "System Administrator"));
        users.add(new User(userSeq++, "delivery", "delivery123", Role.DELIVERY_PERSONNEL, "Delivery Personnel"));

        plans.add(new SubscriptionPlan(planSeq++, "Basic 5-Gal", "Standard 5 gallon water container plan", 150.00, "MONTHLY"));
        plans.add(new SubscriptionPlan(planSeq++, "Premium 5-Gal", "Priority delivery + free sanitizing", 220.00, "MONTHLY"));

        drivers.add(new Driver(driverSeq++, "Juan Dela Cruz", "0917-000-0001", "N01-LIC-1111"));
        drivers.add(new Driver(driverSeq++, "Maria Santos", "0917-000-0002", "N01-LIC-2222"));

        // Link the built-in delivery login to the first driver record.
        users.stream().filter(u -> u.getUsername().equalsIgnoreCase("delivery")).findFirst()
                .ifPresent(u -> u.setDriverId(drivers.get(0).getId()));

        for (int i = 0; i < 5; i++) {
            containers.add(new Container(containerSeq++, String.format("CNT-%04d", containerSeq - 1)));
        }
    }
}

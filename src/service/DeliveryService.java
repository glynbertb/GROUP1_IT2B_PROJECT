package service;

import model.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeliveryService {
    private final AppContext ctx;
    private final DriverService driverService;
    private final ContainerService containerService;

    public DeliveryService(AppContext ctx, DriverService driverService, ContainerService containerService) {
        this.ctx = ctx;
        this.driverService = driverService;
        this.containerService = containerService;
    }

    public Delivery schedule(int subscriptionId, int customerId, LocalDate date) throws Exception {
        ctx.subscriptions.stream().filter(s -> s.getId() == subscriptionId).findFirst()
                .orElseThrow(() -> new Exception("Subscription not found (ID " + subscriptionId + ")."));
        ctx.customers.stream().filter(c -> c.getId() == customerId).findFirst()
                .orElseThrow(() -> new Exception("Customer not found (ID " + customerId + ")."));
        if (date == null) throw new Exception("Scheduled date is required (yyyy-MM-dd).");
        if (date.isBefore(LocalDate.now())) throw new Exception("Scheduled date cannot be in the past.");
        Delivery d = new Delivery(ctx.deliverySeq++, subscriptionId, customerId, date);
        ctx.deliveries.add(d);
        return d;
    }

    public void reschedule(int deliveryId, LocalDate newDate) throws Exception {
        Delivery del = findById(deliveryId).orElseThrow(() -> new Exception("Delivery not found (ID " + deliveryId + ")."));
        if (del.getStatus() == DeliveryStatus.DELIVERED || del.getStatus() == DeliveryStatus.CANCELLED)
            throw new Exception("Cannot reschedule a " + del.getStatus() + " delivery.");
        if (newDate == null) throw new Exception("New date is required (yyyy-MM-dd).");
        if (newDate.isBefore(LocalDate.now())) throw new Exception("New date cannot be in the past.");
        del.setScheduledDate(newDate);
    }

    public void cancel(int deliveryId) throws Exception {
        Delivery del = findById(deliveryId).orElseThrow(() -> new Exception("Delivery not found (ID " + deliveryId + ")."));
        if (del.getStatus() == DeliveryStatus.DELIVERED)
            throw new Exception("Cannot cancel an already DELIVERED delivery.");
        if (del.getStatus() == DeliveryStatus.CANCELLED)
            throw new Exception("Delivery is already CANCELLED.");
        updateStatus(deliveryId, DeliveryStatus.CANCELLED);
    }

    public void delete(int deliveryId) throws Exception {
        Delivery del = findById(deliveryId).orElseThrow(() -> new Exception("Delivery not found (ID " + deliveryId + ")."));
        if (del.getStatus() != DeliveryStatus.SCHEDULED && del.getStatus() != DeliveryStatus.CANCELLED)
            throw new Exception("Only SCHEDULED or CANCELLED deliveries can be deleted. Cancel it first.");
        if (del.getDriverId() != null)
            driverService.findById(del.getDriverId()).ifPresent(dr -> dr.setAvailable(true));
        ctx.deliveries.remove(del);
    }

    public Optional<Delivery> findById(int id) {
        return ctx.deliveries.stream().filter(d -> d.getId() == id).findFirst();
    }

    public void assignDriver(int deliveryId, int driverId) throws Exception {
        Delivery del = findById(deliveryId).orElseThrow(() -> new Exception("Delivery not found (ID " + deliveryId + ")."));
        Driver drv = driverService.findById(driverId).orElseThrow(() -> new Exception("Driver not found (ID " + driverId + ")."));
        if (!drv.isAvailable()) throw new Exception("Driver is currently busy.");
        if (del.getStatus() == DeliveryStatus.DELIVERED || del.getStatus() == DeliveryStatus.CANCELLED)
            throw new Exception("Cannot assign a driver to a " + del.getStatus() + " delivery.");

        if (del.getDriverId() != null && del.getDriverId() != driverId)
            driverService.findById(del.getDriverId()).ifPresent(old -> old.setAvailable(true));
        del.setDriverId(driverId);
        del.setStatus(DeliveryStatus.ASSIGNED);
        drv.setAvailable(false);
    }

    public void unassignDriver(int deliveryId) throws Exception {
        Delivery del = findById(deliveryId).orElseThrow(() -> new Exception("Delivery not found (ID " + deliveryId + ")."));
        if (del.getDriverId() == null) throw new Exception("Delivery has no driver assigned.");
        if (del.getStatus() == DeliveryStatus.DELIVERED || del.getStatus() == DeliveryStatus.CANCELLED)
            throw new Exception("Cannot unassign driver from a " + del.getStatus() + " delivery.");
        driverService.findById(del.getDriverId()).ifPresent(dr -> dr.setAvailable(true));
        del.setDriverId(null);
        del.setStatus(DeliveryStatus.SCHEDULED);
    }

    public void assignContainer(int deliveryId, int containerId) throws Exception {
        Delivery del = findById(deliveryId).orElseThrow(() -> new Exception("Delivery not found (ID " + deliveryId + ")."));
        Container c = containerService.findById(containerId).orElseThrow(() -> new Exception("Container not found (ID " + containerId + ")."));
        if (del.getStatus() == DeliveryStatus.DELIVERED || del.getStatus() == DeliveryStatus.CANCELLED)
            throw new Exception("Cannot add containers to a " + del.getStatus() + " delivery.");

        if (!del.getContainerIds().contains(containerId)) {
            del.getContainerIds().add(containerId);
        }
        containerService.assignToCustomer(containerId, del.getCustomerId(), del.getSubscriptionId());
    }

    public void removeContainer(int deliveryId, int containerId) throws Exception {
        Delivery del = findById(deliveryId).orElseThrow(() -> new Exception("Delivery not found (ID " + deliveryId + ")."));
        if (!del.getContainerIds().contains(containerId))
            throw new Exception("Container is not part of this delivery.");
        if (del.getStatus() == DeliveryStatus.DELIVERED || del.getStatus() == DeliveryStatus.IN_TRANSIT)
            throw new Exception("Cannot remove containers while delivery is " + del.getStatus() + ".");
        del.getContainerIds().remove(Integer.valueOf(containerId));
        containerService.updateStatus(containerId, ContainerStatus.AVAILABLE);
    }

    public void updateStatus(int deliveryId, DeliveryStatus status) throws Exception {
        Delivery del = findById(deliveryId).orElseThrow(() -> new Exception("Delivery not found (ID " + deliveryId + ")."));
        if (status == null) throw new Exception("Status cannot be empty.");
        if (status == DeliveryStatus.ASSIGNED && del.getDriverId() == null)
            throw new Exception("Assign a driver before marking ASSIGNED.");
        if ((status == DeliveryStatus.IN_TRANSIT || status == DeliveryStatus.DELIVERED)
                && del.getContainerIds().isEmpty())
            throw new Exception("Assign at least one container before marking " + status + ".");
        del.setStatus(status);

        if (status == DeliveryStatus.DELIVERED) {
            for (int cid : del.getContainerIds()) containerService.recordDelivered(cid);
        }
        if (status == DeliveryStatus.RETURNED) {
            for (int cid : del.getContainerIds()) containerService.recordReturned(cid);
        }
        if ((status == DeliveryStatus.DELIVERED || status == DeliveryStatus.CANCELLED) && del.getDriverId() != null) {
            driverService.findById(del.getDriverId()).ifPresent(dr -> dr.setAvailable(true));
        }
    }

    public List<Delivery> all() {
        return ctx.deliveries;
    }

    public List<Delivery> byCustomer(int customerId) {
        List<Delivery> out = new ArrayList<>();
        for (Delivery d : ctx.deliveries) {
            if (d.getCustomerId() == customerId) out.add(d);
        }
        return out;
    }
}

package service;

import model.Container;
import model.ContainerCondition;
import model.ContainerStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContainerService {
    private final AppContext ctx;

    public ContainerService(AppContext ctx) {
        this.ctx = ctx;
    }

    public Container prepareContainer(String code) throws Exception {
        if (code == null || !code.trim().toUpperCase().matches("CNT-\\d{4}"))
            throw new Exception("Container code must match CNT-0000 format (e.g. CNT-0006).");
        String norm = code.trim().toUpperCase();
        for (Container c : ctx.containers) {
            if (c.getContainerCode().equalsIgnoreCase(norm))
                throw new Exception("Container code already exists (" + norm + ").");
        }
        Container c = new Container(ctx.containerSeq++, norm);
        ctx.containers.add(c);
        return c;
    }

    public Container updateContainer(int containerId, String newCode,
            ContainerCondition condition, ContainerStatus status) throws Exception {
        Container c = findById(containerId)
                .orElseThrow(() -> new Exception("Container not found (ID " + containerId + ")."));
        if (newCode == null || !newCode.trim().toUpperCase().matches("CNT-\\d{4}"))
            throw new Exception("Container code must match CNT-0000 format (e.g. CNT-0006).");
        String norm = newCode.trim().toUpperCase();
        for (Container other : ctx.containers) {
            if (other.getId() != containerId && other.getContainerCode().equalsIgnoreCase(norm))
                throw new Exception("Container code already exists (" + norm + ").");
        }
        if (condition == null) throw new Exception("Condition cannot be empty.");
        if (status == null) throw new Exception("Status cannot be empty.");
        c.setContainerCode(norm);
        c.setCondition(condition);
        c.setStatus(status);
        if (condition == ContainerCondition.MAJOR_DAMAGE || condition == ContainerCondition.UNUSABLE) {
            c.setStatus(ContainerStatus.DAMAGED);
        }
        if (c.getStatus() == ContainerStatus.AVAILABLE) {
            c.setAssignedCustomerId(null);
            c.setAssignedSubscriptionId(null);
        }
        return c;
    }

    public void deleteContainer(int containerId) throws Exception {
        Container c = findById(containerId).orElseThrow(() -> new Exception("Container not found (ID " + containerId + ")."));
        if (c.getStatus() == ContainerStatus.ASSIGNED || c.getStatus() == ContainerStatus.IN_TRANSIT)
            throw new Exception("Cannot delete: container is currently " + c.getStatus() + ". Return it first.");
        boolean onDelivery = ctx.deliveries.stream().anyMatch(d -> d.getContainerIds().contains(containerId));
        if (onDelivery) throw new Exception("Cannot delete: container is linked to a delivery. Retire it instead.");
        ctx.containers.remove(c);
    }

    public Optional<Container> findById(int id) {
        return ctx.containers.stream().filter(c -> c.getId() == id).findFirst();
    }

    public Optional<Container> findByCode(String code) {
        if (code == null) return Optional.empty();
        return ctx.containers.stream()
                .filter(c -> c.getContainerCode().equalsIgnoreCase(code.trim())).findFirst();
    }

    public List<Container> available() {
        List<Container> out = new ArrayList<>();
        for (Container c : ctx.containers) {
            if (c.getStatus() == ContainerStatus.AVAILABLE) out.add(c);
        }
        return out;
    }

    public void assignToCustomer(int containerId, int customerId, Integer subscriptionId) throws Exception {
        Container c = findById(containerId).orElseThrow(() -> new Exception("Container not found (ID " + containerId + ")."));
        ctx.customers.stream().filter(x -> x.getId() == customerId).findFirst()
                .orElseThrow(() -> new Exception("Customer not found (ID " + customerId + ")."));
        if (subscriptionId != null)
            ctx.subscriptions.stream().filter(s -> s.getId() == subscriptionId).findFirst()
                    .orElseThrow(() -> new Exception("Subscription not found (ID " + subscriptionId + ")."));
        if (c.getStatus() != ContainerStatus.AVAILABLE && c.getStatus() != ContainerStatus.RETURNED) {
            throw new Exception("Container is not available (current status: " + c.getStatus() + ").");
        }
        c.setStatus(ContainerStatus.ASSIGNED);
        c.setAssignedCustomerId(customerId);
        c.setAssignedSubscriptionId(subscriptionId);
    }

    public void updateStatus(int containerId, ContainerStatus status) throws Exception {
        Container c = findById(containerId).orElseThrow(() -> new Exception("Container not found (ID " + containerId + ")."));
        if (status == null) throw new Exception("Status cannot be empty.");
        c.setStatus(status);
        if (status == ContainerStatus.AVAILABLE) {
            c.setAssignedCustomerId(null);
            c.setAssignedSubscriptionId(null);
        }
    }

    public void checkCondition(int containerId, ContainerCondition condition) throws Exception {
        Container c = findById(containerId).orElseThrow(() -> new Exception("Container not found (ID " + containerId + ")."));
        if (condition == null) throw new Exception("Condition cannot be empty.");
        c.setCondition(condition);
        if (condition == ContainerCondition.MAJOR_DAMAGE || condition == ContainerCondition.UNUSABLE) {
            c.setStatus(ContainerStatus.DAMAGED);
        }
    }

    public void recordDelivered(int containerId) throws Exception {
        Container c = findById(containerId).orElseThrow(() -> new Exception("Container not found (ID " + containerId + ")."));
        c.setStatus(ContainerStatus.DELIVERED);
    }

    public void recordReturned(int containerId) throws Exception {
        Container c = findById(containerId).orElseThrow(() -> new Exception("Container not found (ID " + containerId + ")."));
        c.setStatus(ContainerStatus.RETURNED);
    }

    public List<Container> all() {
        return ctx.containers;
    }
}

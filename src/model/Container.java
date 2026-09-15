package model;

public class Container {
    private final int id;
    private String containerCode;
    private ContainerStatus status;
    private ContainerCondition condition;
    private Integer assignedCustomerId;
    private Integer assignedSubscriptionId;

    public Container(int id, String containerCode) {
        this.id = id;
        this.containerCode = containerCode;
        this.status = ContainerStatus.AVAILABLE;
        this.condition = ContainerCondition.GOOD;
        this.assignedCustomerId = null;
        this.assignedSubscriptionId = null;
    }

    public int getId() { return id; }
    public String getContainerCode() { return containerCode; }
    public void setContainerCode(String containerCode) { this.containerCode = containerCode; }
    public ContainerStatus getStatus() { return status; }
    public void setStatus(ContainerStatus status) { this.status = status; }
    public ContainerCondition getCondition() { return condition; }
    public void setCondition(ContainerCondition condition) { this.condition = condition; }
    public Integer getAssignedCustomerId() { return assignedCustomerId; }
    public void setAssignedCustomerId(Integer assignedCustomerId) { this.assignedCustomerId = assignedCustomerId; }
    public Integer getAssignedSubscriptionId() { return assignedSubscriptionId; }
    public void setAssignedSubscriptionId(Integer assignedSubscriptionId) { this.assignedSubscriptionId = assignedSubscriptionId; }

    @Override
    public String toString() {
        return String.format("[CT%03d] %-10s | Status:%-10s | Condition:%-13s | CustomerId:%s",
                id, containerCode, status, condition, assignedCustomerId == null ? "-" : assignedCustomerId);
    }
}

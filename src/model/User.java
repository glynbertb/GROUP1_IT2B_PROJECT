package model;

public class  User {
    private final int id;
    private final String username;
    private final String password;
    private Role role;
    private final String fullName;
    /** Link to the Customer record for CUSTOMER logins (null when not linked). */
    private Integer customerId;
    /** Link to the Driver record for DELIVERY_PERSONNEL logins (null when not linked). */
    private Integer driverId;

    public User(int id, String username, String password, Role role, String fullName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.customerId = null;
        this.driverId = null;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getFullName() { return fullName; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public Integer getDriverId() { return driverId; }
    public void setDriverId(Integer driverId) { this.driverId = driverId; }
}

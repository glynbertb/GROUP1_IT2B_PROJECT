package model;

public class Driver {
    private final int id;
    private String name;
    private String contactNumber;
    private String licenseNumber;
    private boolean available;

    public Driver(int id, String name, String contactNumber, String licenseNumber) {
        this.id = id;
        this.name = name;
        this.contactNumber = contactNumber;
        this.licenseNumber = licenseNumber;
        this.available = true;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return String.format("[D%03d] %-18s | %-12s | Lic:%-10s | %s",
                id, name, contactNumber, licenseNumber, available ? "AVAILABLE" : "BUSY");
    }
}

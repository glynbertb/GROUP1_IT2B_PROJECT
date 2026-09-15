package model;

import java.time.LocalDate;

public class Customer {
    private final int id;
    private String name;
    private String address;
    private String contactNumber;
    private String email;
    private final LocalDate registeredDate;

    public Customer(int id, String name, String address, String contactNumber, String email) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
        this.registeredDate = LocalDate.now();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getRegisteredDate() { return registeredDate; }

    @Override
    public String toString() {
        return String.format("| C%03d | %-15s | %-12s | %-15s | %s |",
                id, name, contactNumber, email, address);
    }
}

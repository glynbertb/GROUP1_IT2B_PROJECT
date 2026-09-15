package service;

import model.Driver;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DriverService {
    private final AppContext ctx;

    public DriverService(AppContext ctx) {
        this.ctx = ctx;
    }

    public Driver add(String name, String contact, String license) throws Exception {
        validate(name, contact, license, -1);
        Driver d = new Driver(ctx.driverSeq++, name.trim(), contact.trim(), license.trim());
        ctx.drivers.add(d);
        return d;
    }

    public Driver update(int id, String name, String contact, String license) throws Exception {
        Driver d = findById(id).orElseThrow(() -> new Exception("Driver not found (ID " + id + ")."));
        validate(name, contact, license, id);
        d.setName(name.trim());
        d.setContactNumber(contact.trim());
        d.setLicenseNumber(license.trim());
        return d;
    }

    public void remove(int id) throws Exception {
        Driver d = findById(id).orElseThrow(() -> new Exception("Driver not found (ID " + id + ")."));
        if (!d.isAvailable()) throw new Exception("Cannot remove: driver is currently on an active delivery.");
        boolean assigned = ctx.deliveries.stream()
                .anyMatch(del -> del.getDriverId() != null && del.getDriverId() == id
                        && (del.getStatus().name().equals("ASSIGNED") || del.getStatus().name().equals("IN_TRANSIT")));
        if (assigned) throw new Exception("Cannot remove: driver is assigned to an active delivery.");
        ctx.drivers.remove(d);
    }

    private void validate(String name, String contact, String license, int excludeId) throws Exception {
        if (name == null || name.trim().length() < 2 || name.trim().length() > 60)
            throw new Exception("Driver name must be 2-60 characters.");
        if (contact == null || contact.replaceAll("[^0-9]", "").length() < 7
                || contact.replaceAll("[^0-9]", "").length() > 13)
            throw new Exception("Contact must contain 7-13 digits.");
        if (license == null || license.trim().length() < 3 || license.trim().length() > 30)
            throw new Exception("License number must be 3-30 characters.");
        for (Driver d : ctx.drivers) {
            if (d.getId() == excludeId) continue;
            if (d.getLicenseNumber().equalsIgnoreCase(license.trim()))
                throw new Exception("License already registered to driver D" + String.format("%03d", d.getId()) + ".");
        }
    }

    public Optional<Driver> findById(int id) {
        return ctx.drivers.stream().filter(d -> d.getId() == id).findFirst();
    }

    public List<Driver> searchByName(String keyword) {
        List<Driver> out = new ArrayList<>();
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        for (Driver d : ctx.drivers) {
            if (d.getName().toLowerCase().contains(k)) out.add(d);
        }
        return out;
    }

    public List<Driver> all() {
        return ctx.drivers;
    }

    public List<Driver> available() {
        List<Driver> out = new ArrayList<>();
        for (Driver d : ctx.drivers) {
            if (d.isAvailable()) out.add(d);
        }
        return out;
    }
}

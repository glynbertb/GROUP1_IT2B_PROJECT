package service;

import model.Customer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerService {
    private final AppContext ctx;

    public CustomerService(AppContext ctx) {
        this.ctx = ctx;
    }

    public Customer register(String name, String address, String contact, String email) throws Exception {
        validate(name, address, contact, email, -1);
        Customer c = new Customer(ctx.custSeq++, name.trim(), address.trim(), contact.trim(), email.trim());
        ctx.customers.add(c);
        return c;
    }

    public Customer update(int id, String name, String address, String contact, String email) throws Exception {
        Customer c = findById(id).orElseThrow(() -> new Exception("Customer not found (ID " + id + ")."));
        validate(name, address, contact, email, id);
        c.setName(name.trim());
        c.setAddress(address.trim());
        c.setContactNumber(contact.trim());
        c.setEmail(email.trim());
        return c;
    }

    public void delete(int id) throws Exception {
        Customer c = findById(id).orElseThrow(() -> new Exception("Customer not found (ID " + id + ")."));
        boolean hasSub = ctx.subscriptions.stream().anyMatch(s -> s.getCustomerId() == id);
        if (hasSub) throw new Exception("Cannot delete: customer has subscription record(s). Cancel them first.");
        boolean hasDel = ctx.deliveries.stream().anyMatch(d -> d.getCustomerId() == id);
        if (hasDel) throw new Exception("Cannot delete: customer has delivery record(s).");
        ctx.customers.remove(c);
    }

    private void validate(String name, String address, String contact, String email, int excludeId) throws Exception {
        if (name == null || name.trim().length() < 2 || name.trim().length() > 60)
            throw new Exception("Name must be 2-60 characters.");
        if (address == null || address.trim().length() < 5 || address.trim().length() > 120)
            throw new Exception("Address must be 5-120 characters.");
        if (contact == null || contact.replaceAll("[^0-9]", "").length() < 7
                || contact.replaceAll("[^0-9]", "").length() > 13)
            throw new Exception("Contact must contain 7-13 digits.");
        if (email == null || !email.trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            throw new Exception("Invalid email format (e.g. juan@email.com).");
        for (Customer c : ctx.customers) {
            if (c.getId() == excludeId) continue;
            if (c.getEmail().equalsIgnoreCase(email.trim()))
                throw new Exception("Email already registered to customer C" + String.format("%03d", c.getId()) + ".");
        }
    }

    public Optional<Customer> findById(int id) {
        return ctx.customers.stream().filter(c -> c.getId() == id).findFirst();
    }

    public List<Customer> searchByName(String keyword) {
        List<Customer> out = new ArrayList<>();
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        for (Customer c : ctx.customers) {
            if (c.getName().toLowerCase().contains(k)) out.add(c);
        }
        return out;
    }

    public List<Customer> all() {
        return ctx.customers;
    }
}

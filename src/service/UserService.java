package service;

import model.Role;
import model.User;

import java.util.List;
import java.util.Optional;

public class UserService {
    private final AppContext ctx;

    public UserService(AppContext ctx) {
        this.ctx = ctx;
    }

    public User signUp(String username, String password, String fullName) throws Exception {
        validateNewAccount(username, password, fullName);
        User u = new User(ctx.userSeq++, username.trim(), password, Role.CUSTOMER, fullName.trim());
        ctx.users.add(u);
        return u;
    }

    public User createAccount(String username, String password, String fullName, Role role) throws Exception {
        validateNewAccount(username, password, fullName);
        if (role == null) throw new Exception("Role cannot be empty.");
        User u = new User(ctx.userSeq++, username.trim(), password, role, fullName.trim());
        ctx.users.add(u);
        return u;
    }

    public User updateRole(int userId, Role newRole) throws Exception {
        User target = findById(userId)
                .orElseThrow(() -> new Exception("Account not found (ID " + userId + ")."));
        if (newRole == null) throw new Exception("Role cannot be empty.");
        if (ctx.currentUser != null && target.getId() == ctx.currentUser.getId())
            throw new Exception("You cannot change your own role while logged in.");
        if (target.getUsername().equalsIgnoreCase("admin") && newRole != Role.ADMIN)
            throw new Exception("The built-in admin account must stay ADMIN.");
        long adminCount = ctx.users.stream().filter(u -> u.getRole() == Role.ADMIN).count();
        if (target.getRole() == Role.ADMIN && newRole != Role.ADMIN && adminCount <= 1)
            throw new Exception("Cannot demote the last remaining ADMIN account.");
        target.setRole(newRole);
        return target;
    }

    private void validateNewAccount(String username, String password, String fullName) throws Exception {
        if (username == null || !username.trim().matches("[A-Za-z0-9._]{3,20}"))
            throw new Exception("Username must be 3-20 characters (letters, digits, . or _).");
        if (password == null || password.length() < 4 || password.length() > 30)
            throw new Exception("Password must be 4-30 characters.");
        if (fullName == null || fullName.trim().length() < 2 || fullName.trim().length() > 60)
            throw new Exception("Full name must be 2-60 characters.");
        for (User u : ctx.users) {
            if (u.getUsername().equalsIgnoreCase(username.trim()))
                throw new Exception("Username '" + username.trim() + "' is already taken.");
        }
    }

    public Optional<User> findById(int id) {
        return ctx.users.stream().filter(u -> u.getId() == id).findFirst();
    }

    public List<User> all() {
        return ctx.users;
    }
}

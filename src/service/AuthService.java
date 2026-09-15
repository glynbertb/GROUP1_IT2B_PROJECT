package service;

import model.User;

public class AuthService {
    private final AppContext ctx;

    public AuthService(AppContext ctx) {
        this.ctx = ctx;
    }

    public User login(String username, String password) {
        for (User u : ctx.users) {
            if (u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)) {
                ctx.currentUser = u;
                return u;
            }
        }
        return null;
    }

    public void logout() {
        ctx.currentUser = null;
    }
}
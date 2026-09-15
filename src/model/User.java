package model;

public class User {
    private final int id;
    private final String username;
    private final String password;
    private final Role role;
    private final String fullName;

    public User(int id, String username, String password, Role role, String fullName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public String getFullName() { return fullName; }
}
package git.xlamid.eventnotificatorservice.util.model;

public record TestUser(Long id, String login, String role) {

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getRole() {
        return role;
    }
}
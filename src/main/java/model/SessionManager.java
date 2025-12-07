package model;

import model.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private User player1;
    private User player2;
    private boolean isTwoPlayerMode;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
        this.isTwoPlayerMode = false;
        this.player1 = null;
        this.player2 = null;
    }

    public void loginTwoPlayers(User player1, User player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.currentUser = player1;
        this.isTwoPlayerMode = true;
    }

    public void logout() {
        this.currentUser = null;
        this.player1 = null;
        this.player2 = null;
        this.isTwoPlayerMode = false;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isTwoPlayerMode() {
        return isTwoPlayerMode;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public User getPlayer1() {
        return player1;
    }

    public User getPlayer2() {
        return player2;
    }

    public String getUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }
}
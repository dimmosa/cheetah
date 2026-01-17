package control;

import model.SysData;
import model.User;
import model.SessionManager;
import model.UserService;

public class SignUpControl {

    private final UserService userService;
    private final SysData sysData;
    private final SessionManager session;

    public SignUpControl() {
        this.userService = new UserService();
        this.sysData = SysData.getInstance();
        this.session = SessionManager.getInstance();
    }


    public User signup(String username, String password) {
        boolean success = userService.signup(username, password);
        if (success) {
            User newUser = userService.getUser(username);
            session.login(newUser);
            return newUser;
        }
        return null;
    }

    public SysData getSysData() {
        return sysData;
    }

    public SessionManager getSession() {
        return session;
    }
}
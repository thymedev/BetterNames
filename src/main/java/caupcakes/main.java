package caupcakes;

import javax.security.auth.login.LoginException;

public class main {
    public static void main(String[] args) throws LoginException, InterruptedException {
        new Init(args[0], SlashCommandListener.class, ButtonListener.class);
    }
}

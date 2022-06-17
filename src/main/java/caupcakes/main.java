package caupcakes;

import caupcakes.listeners.ButtonListener;
import caupcakes.listeners.NicknameListener;
import caupcakes.listeners.SlashCommandListener;

import javax.security.auth.login.LoginException;

public class main {
    public static void main(String[] args) throws LoginException, InterruptedException, InstantiationException, IllegalAccessException {
        new Init(args[0], SlashCommandListener.class, ButtonListener.class, NicknameListener.class);
    }
}

package caupcakes;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.regex.Pattern;

import static caupcakes.Commands.Setup.matches;

public class GuildMembers {
    public static int matchAndReplaceName(Guild g, Pattern p, int nonce) {
        // just replace offending characters with "";
        // if their name is a violation, use only a nonce

        for (Member m : g.getMembers()) {
            if (matches(p, m.getUser().getName())) {
                g.modifyNickname(m, nonce + "").queue();
                nonce++;
            } else if (matches(p, m.getNickname())) {
                String nickname = m.getNickname();
                nickname = p.matcher(nickname).replaceAll("");

                g.modifyNickname(m, nickname).queue();
            }
        }

        return nonce;
    }
}

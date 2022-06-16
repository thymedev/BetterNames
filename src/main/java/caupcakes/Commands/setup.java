package caupcakes.Commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static caupcakes.SlashCommandListener.*;

public class setup {
    public static void setup(SlashCommandInteractionEvent event) {
        // permissions checked.

        String regex = event.getOption("regex").getAsString();

        // check if this is a valid regex.

        boolean valid = false;

        try {
            Pattern.compile(regex);
            valid = true;
        } catch (Exception e) {
        }

        if (!valid) {
            replyFailure(event, "Invalid regex.");
            return;
        }

        // get the nickname

        String nickname = event.getOption("nickname") == null ? regex : event.getOption("nickname").getAsString();

        Pattern p = Pattern.compile(regex);

        // find offending users.

        List<Member> members = event.getGuild().getMembers();

        // find the first 10 offenders if possible.

        ArrayList<Member> offenders = new ArrayList<>();

        for (Member m : members) {
            if (matches(p, m.getUser().getName()) || matches(p, m.getNickname())) {
                offenders.add(m);

                if (offenders.size() == 10) {
                    break;
                }
            }
        }

        StringBuilder offenderString = new StringBuilder();

        for (Member m : offenders) {
            offenderString.append(m.getAsMention()).append("\n");
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor(event.getName(), null, event.getUser().getAvatarUrl());
        eb.setTitle("Preview of users who match your regex");
        eb.setDescription("Select ✅ to confirm or ❌ to cancel\n\n" + offenderString);
        eb.setColor(new Color(25, 153, 102));

        eb.setTimestamp(Instant.now());

        event.replyEmbeds(eb.build()).setEphemeral(EPHEMERAL).mentionRepliedUser(MENTION).addActionRow(List.of(
                Button.success(encodeData(event.getUser().getIdLong() + "", "confirm", nickname, regex), "✅"), Button.danger(encodeData(event.getUser().getIdLong() + "", "cancel", nickname, regex), "❌")
        )).queue();
    }


    private static boolean matches(Pattern p, String s) {
        return p.matcher(s).matches();
    }
}

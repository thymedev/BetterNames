package caupcakes;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static caupcakes.listeners.SlashCommandListener.EPHEMERAL;
import static caupcakes.listeners.SlashCommandListener.MENTION;
import static caupcakes.utils.StringCleaner.convertToAscii;
import static caupcakes.utils.Utils.*;

public class Rules {
    public static void letterize(SlashCommandInteractionEvent event) {
        EmbedBuilder eb = createDefaultBaseEmbed();

        eb.setTitle("Letterize");

        StringBuilder desc = new StringBuilder("Converts non regular (ascii) nicknames or names to regular nicknames. This will set their name to their user id if their name would be a blank regular name\n\nConfirming will change the nicknames of these users (preview limited to 10 users):\n");

        Guild g = event.getGuild();
        // non null

        ArrayList<Member> offenders = new ArrayList<>();

        for (Member m : g.getMembers()) {
            String nick = (m.getNickname() == null) ? m.getEffectiveName() : m.getNickname();

            if (!nick.equals(convertToAscii(nick))) {
                offenders.add(m);

                if (offenders.size() == 10) {
                    break;
                }
            }
        }

        for (Member m : offenders) {
            desc.append(m.getAsMention()).append("\n");
        }

        eb.setDescription(desc.toString());

        event.replyEmbeds(eb.build()).setEphemeral(EPHEMERAL).mentionRepliedUser(MENTION).addActionRow(List.of(
                Button.success(encodeData(event.getUser().getId(), "letterize", "on"), "Confirm"),
                Button.danger(encodeData(event.getUser().getId(), "letterize", "off"), "Cancel")
        )).queue();
    }

    public static void custom(SlashCommandInteractionEvent event) {
        String regex = event.getOption("regex").getAsString();
        String name = event.getOption("nickname").getAsString();

        // check if regex is valid

        boolean validregex = false;
        Pattern p = null;
        try {
            p = Pattern.compile(regex);
            validregex = true;
        } catch (Exception ignored) {
        }

        if (!validregex) {
            replyFailure(event, "Invalid regex");
            return;
        }

        EmbedBuilder eb = createDefaultBaseEmbed();

        eb.setTitle("Regex");

        StringBuilder desc = new StringBuilder("Removes characters matching this regex. This will set their name to their user id if their name would be a blank name\n\nConfirming will change the nicknames of these users (preview limited to 10 users):\n");

        Guild g = event.getGuild();
        // non null

        ArrayList<Member> offenders = new ArrayList<>();

        for (Member m : g.getMembers()) {
            String nick = (m.getNickname() == null) ? m.getEffectiveName() : m.getNickname();

            if (!p.matcher(nick).replaceAll("").equals(nick)) {
                offenders.add(m);

                if (offenders.size() == 10) {
                    break;
                }
            }
        }

        for (Member m : offenders) {
            desc.append(m.getAsMention()).append("\n");
        }

        eb.setDescription(desc.toString());

        event.replyEmbeds(eb.build()).setEphemeral(EPHEMERAL).mentionRepliedUser(MENTION).addActionRow(List.of(
                Button.success(encodeData(event.getUser().getId(), "custom", name, regex, "on"), "Confirm"),
                Button.danger(encodeData(event.getUser().getId(), "custom", name, regex, "off"), "Cancel")
        )).queue();
    }
}

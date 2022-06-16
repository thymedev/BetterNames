package caupcakes;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Base64;

import static caupcakes.Commands.Setup.setup;

public class SlashCommandListener extends ListenerAdapter {
    public static final boolean EPHEMERAL = false;
    public static final boolean MENTION = false;


    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        // only allow command in guilds and by admins

        if (event.getGuild() == null) {
            replyFailure(event, "This command can only be used in a server.");
            return;
        }

        if (!event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            replyFailure(event, "You must be an administrator to use this command.");
            return;
        }

        String commandname = event.getName().toLowerCase();

        switch (commandname) {
            case "setup":
                setup(event);
                break;
            case "list":
                list(event);
                break;
        }
    }


    public static void replySuccess(SlashCommandInteractionEvent event, String message) {
        event.reply(message).setEphemeral(EPHEMERAL).mentionRepliedUser(MENTION).queue();
    }

    public static void replyFailure(SlashCommandInteractionEvent event, String message) {
        event.reply(message).setEphemeral(true).mentionRepliedUser(true).queue();
    }

    public static void replyFailure(ButtonInteractionEvent event, String message) {
        event.reply(message).setEphemeral(true).mentionRepliedUser(true).queue();
    }

    public static void replySuccess(SlashCommandInteractionEvent event, MessageEmbed embed) {
        event.replyEmbeds(embed).setEphemeral(EPHEMERAL).mentionRepliedUser(MENTION).queue();
    }

    // encodes an arbitrary amount of data
    public static String encodeData(String... data) {
        // base64 encode s1, s2, s3
        // return the encoded string
        StringBuilder sb = new StringBuilder();

        for (String s : data) {
            sb.append(Base64.getEncoder().encodeToString(s.getBytes())).append(",");
        }

        return sb.toString();
    }

    public static String[] decodeData(String s) {
        String[] sarr = s.split(",");

        for (int i = 0; i < sarr.length; i++) {
            sarr[i] = new String(Base64.getDecoder().decode(sarr[i]));
        }

        return sarr;
    }
}

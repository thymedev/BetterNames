package caupcakes.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.awt.*;
import java.time.Instant;
import java.util.Base64;

import static caupcakes.listeners.SlashCommandListener.EPHEMERAL;
import static caupcakes.listeners.SlashCommandListener.MENTION;

public class Utils {
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

    public static EmbedBuilder createDefaultBaseEmbed() {
        return new EmbedBuilder().setColor(new Color(25, 153, 102)).setTimestamp(Instant.now());
    }
}

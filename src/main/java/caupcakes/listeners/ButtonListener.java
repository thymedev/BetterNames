package caupcakes.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static caupcakes.Init.database;
import static caupcakes.utils.StringCleaner.convertToAscii;
import static caupcakes.utils.Utils.decodeData;
import static caupcakes.utils.Utils.replyFailure;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.push;
import static com.mongodb.client.model.Updates.set;

public class ButtonListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        event.deferEdit().queue();
        // only allow command in guilds and by admins

        if (event.getGuild() == null) {
            replyFailure(event, "This command can only be used in a server.");
            return;
        }

        if (!event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            replyFailure(event, "You must be an administrator to use this command.");
            return;
        }

        String id = event.getButton().getId();
        String[] data = decodeData(id);

        String commandtype = data[1];

        if (commandtype.equals("letterize")) {
            // this is a default letterize command.
            // check that userids match

            boolean matches = event.getUser().getId().equals(data[0]);

            if (!matches) {
                replyFailure(event, "You must be the same user to use this command.");
            } else {
                boolean on = data[2].equals("on");

                if (on) {
                    // toggle in database
                    database.updateOne(eq("id", event.getGuild().getIdLong()), set("letterize", true));

                    for (Member m : event.getGuild().getMembers()) {
                        String nick = (m.getNickname() == null) ? m.getUser().getName() : m.getNickname();
                        String converted = convertToAscii(nick);

                        if (!nick.equals(converted)) {
                            if (converted.equals("")) {
                                m.modifyNickname(m.getId()).queue();
                            } else {
                                m.modifyNickname(converted).queue();
                            }
                        }
                    }

                    // edit the embed description to success.

                    event.editMessageEmbeds(new EmbedBuilder(event.getMessage().getEmbeds().get(0)).setDescription("Letterize is now on.").build()).setActionRow(List.of()).queue();
                } else {
                    database.updateOne(eq("id", event.getGuild().getIdLong()), set("letterize", false));

                    // edit the embed description to cancelled

                    event.editMessageEmbeds(new EmbedBuilder(event.getMessage().getEmbeds().get(0)).setDescription("Letterize is now off.").build()).setActionRow(List.of()).queue();
                }
            }
        } else if (commandtype.equals("custom")) {
            boolean matches = event.getUser().getId().equals(data[0]);

            if (!matches) {
                replyFailure(event, "You must be the same user to use this command.");
            } else {
                String nickname = data[2];
                String pattern = data[3];

                boolean on = data[4].equals("on");

                if (on) {
                    // update custom list
                    database.updateOne(eq("id", event.getGuild().getIdLong()), push("custom", new Document("name", nickname).append("regex", pattern)));

                    Pattern p = Pattern.compile(pattern);

                    for (Member m : event.getGuild().getMembers()) {
                        String nick = (m.getNickname() == null) ? m.getUser().getName() : m.getNickname();
                        Matcher matcher = p.matcher(nick);
                        String replaced = matcher.replaceAll("");

                        if (!replaced.equals(nick)) {
                            if (replaced.equals("")) {
                                m.modifyNickname(m.getId()).queue();
                            } else {
                                m.modifyNickname(replaced).queue();
                            }
                        }
                    }

                    event.editMessageEmbeds(new EmbedBuilder(event.getMessage().getEmbeds().get(0)).setDescription(nickname + " is now on.").build()).setActionRow(List.of()).queue();
                } else {
                    // edit the embed description to cancelled

                    event.editMessageEmbeds(new EmbedBuilder(event.getMessage().getEmbeds().get(0)).setDescription(nickname + " cancelled.").build()).setActionRow(List.of()).queue();
                }
            }
        }
    }
}

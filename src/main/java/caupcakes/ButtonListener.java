package caupcakes;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static caupcakes.GuildMembers.matchAndReplaceName;
import static caupcakes.Init.database;
import static caupcakes.SlashCommandListener.decodeData;
import static caupcakes.SlashCommandListener.replyFailure;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.push;
import static com.mongodb.client.model.Updates.set;

public class ButtonListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        event.deferEdit().queue();
        if (event.getGuild() == null) {
            replyFailure(event, "This command can only be used in a server.");
            return;
        }

        // get data

        String[] data = decodeData(event.getButton().getId());

        long userid = Long.parseLong(data[0]);

        if (event.getUser().getIdLong() != userid) {
            replyFailure(event, "You are not allowed to use this button.");
            return;
        }

        // check if they still have admin

        if (!event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            replyFailure(event, "You must be an administrator to use this command.");

            event.getMessage().delete().queue();

            return;
        }

        String action = data[1];
        String nickname = data[2];

        if (action.equals("confirm")) {
            // confirm
            // insert into database
            String regex = data[3];

            // it is valid regex

            // insert into database
            Document doc = database.find(eq("id", event.getGuild().getIdLong())).first();
            int nonce = 1;

            if (doc != null) {
                database.updateOne(eq("id", event.getGuild().getIdLong()), push("rules", new Document("nickname", nickname).append("regex", regex)));
                nonce = doc.getInteger("nonce");
            } else {
                doc = new Document("id", event.getGuild().getId());
                doc.append("nonce", 1);
                doc.append("rules", new ArrayList<Document>() {{
                    add(new Document("nickname", nickname).append("regex", regex));
                }});

                database.insertOne(doc);
            }

            // change embed description

            event.getMessage().editMessageEmbeds(new EmbedBuilder(event.getMessage().getEmbeds().get(0)).setDescription("Successfully added rule: `" + nickname + "`").build()).setActionRow(List.of()).queue();

            nonce = matchAndReplaceName(event.getGuild(), Pattern.compile(regex), nonce);

            database.updateOne(eq("id", event.getGuild().getIdLong()), set("nonce", nonce));
        } else if (action.equals("cancel")) {
            // cancel
            event.getMessage().editMessageEmbeds(new EmbedBuilder(event.getMessage().getEmbeds().get(0)).setDescription("Cancelled add rule: `" + nickname + "`").build()).setActionRow(List.of()).queue();
        }
    }
}

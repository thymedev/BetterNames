package caupcakes.listeners;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static caupcakes.Init.database;
import static caupcakes.Rules.custom;
import static caupcakes.Rules.letterize;
import static caupcakes.utils.Utils.replyFailure;
import static com.mongodb.client.model.Filters.eq;

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

        // create a document to represent their server
        // this is slower but convenient for coding

        Document doc = database.find(eq("id", event.getGuild().getIdLong())).first();

        if (doc == null) {
            doc = new Document("id", event.getGuild().getIdLong()).append("letterize", false).append("custom", new ArrayList<>());
            database.insertOne(doc);
        }

        switch (commandname) {
            case "letterize" -> letterize(event);
            case "custom" -> custom(event);
        }
    }
}

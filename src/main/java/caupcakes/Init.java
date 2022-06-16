package caupcakes;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.bson.Document;

import javax.security.auth.login.LoginException;

public class Init {
    public static final MongoCollection<Document> database;
    public static JDA jda;

    static {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        database = mongoClient.getDatabase("BetterNames").getCollection("settings");
        database.createIndex(new Document("id", 1)); // index by server id
    }

    @SafeVarargs
    public Init(String token, Class<? extends ListenerAdapter>... listeners) throws LoginException, InterruptedException {
        JDABuilder builder = JDABuilder.createLight(token);
        for (Class<? extends ListenerAdapter> listener : listeners) {
            builder.addEventListeners(listener);
        }

        jda = builder.build();

        jda.awaitReady();

        jda.retrieveCommands().queue(commands -> commands.forEach(command -> command.delete().queue()));

        jda.upsertCommand(Commands.slash("setup", "Set up a name filter").addOptions(new OptionData(OptionType.STRING, "nickname", "This is a nickname for this rule", false), new OptionData(OptionType.STRING, "regex", "regex to match names against", true))).queue();
        jda.upsertCommand(Commands.slash("list", "list filters for this server")).queue();
    }
}

package caupcakes;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;

import javax.security.auth.login.LoginException;

public class Init {
    public static final MongoCollection<Document> database;
    public static JDA jda;

    static {
        MongoClient mongoClient = new MongoClient("mongo", 27017);
        database = mongoClient.getDatabase("BetterNames").getCollection("settings");
        database.createIndex(new Document("id", 1)); // index by server id
    }

    @SafeVarargs
    public Init(String token, Class<? extends ListenerAdapter>... listeners) throws LoginException, InterruptedException, InstantiationException, IllegalAccessException {
        JDABuilder builder = JDABuilder.createLight(token);

        for (Class<? extends ListenerAdapter> listener : listeners) {
            builder.addEventListeners(listener.newInstance());
        }

        jda = builder.build();

        jda.awaitReady();
    }
}

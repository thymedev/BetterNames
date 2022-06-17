package caupcakes.listeners;

import com.ibm.icu.lang.UCharacter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static caupcakes.Init.database;
import static caupcakes.utils.StringCleaner.convertToAscii;
import static com.mongodb.client.model.Filters.eq;

public class NicknameListener extends ListenerAdapter {
    private static void updateNameWithRules(Guild guild, Member member, User user) {
        Document doc = database.find(eq("id", guild.getIdLong())).first();

        if (doc != null) {
            // see if they have any rules
            boolean letterize = doc.getBoolean("letterize");

            String name = (member.getNickname() == null) ? user.getName() : member.getNickname();

            if (letterize && !name.equals(convertToAscii(name))) {
                name = convertToAscii(name);
            }

            // see if they have any custom rules
            ArrayList<Document> customrules = (ArrayList<Document>) doc.get("custom");

            if (customrules.size() != 0) {
                ArrayList<String> regex = new ArrayList<>();

                for (Document d : customrules) {
                    regex.add(d.getString("regex"));
                }

                ArrayList<Pattern> patterns = new ArrayList<>();

                for (String r : regex) {
                    patterns.add(Pattern.compile(r));
                }

                for (Pattern p : patterns) {
                    Matcher matcher = p.matcher(name);

                    if (matcher.matches()) {
                        name = p.matcher(name).replaceAll("");
                    }
                }
            }

            // check if their name is all whitespace.
            boolean allwhitespace = true;

            for (char c : name.toCharArray()) {
                if (!UCharacter.isWhitespace(c)) {
                    allwhitespace = false;
                    break;
                }
            }

            String newnick = (allwhitespace) ? user.getId() : name;

            member.modifyNickname(newnick).queue();
        }
    }

    // on init, check that all servers are valid
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        // load rule from db.

        Document doc = database.find(eq("id", event.getGuild().getIdLong())).first();

        if (doc != null) {
            // see if they have any rules
            boolean letterize = doc.getBoolean("letterize");

            Hashtable<Long, String> offenders = new Hashtable<>();

            if (letterize) {
                // see if there are any offenders and process it

                for (Member m : event.getGuild().getMembers()) {
                    String nick = (m.getNickname() == null) ? m.getUser().getName() : m.getNickname();

                    if (!nick.equals(convertToAscii(nick))) {
                        offenders.put(m.getUser().getIdLong(), convertToAscii(nick));
                    }
                }
            }

            // see if they have any custom rules
            ArrayList<Document> customrules = (ArrayList<Document>) doc.get("custom");

            if (customrules.size() != 0) {
                ArrayList<String> regex = new ArrayList<>();

                for (Document d : customrules) {
                    regex.add(d.getString("regex"));
                }

                ArrayList<Pattern> patterns = new ArrayList<>();

                for (String r : regex) {
                    patterns.add(Pattern.compile(r));
                }

                for (Member m : event.getGuild().getMembers()) {
                    String nick = (m.getNickname() == null) ? m.getUser().getName() : m.getNickname();
                    long userid = m.getUser().getIdLong();

                    for (Pattern p : patterns) {
                        Matcher matcher = p.matcher(nick);

                        if (matcher.matches()) {
                            if (offenders.containsKey(userid)) {
                                String oldnick = offenders.get(m.getUser().getIdLong());
                                oldnick = p.matcher(oldnick).replaceAll("");

                                offenders.put(userid, oldnick);
                            } else {
                                offenders.put(userid, nick);
                            }
                        }
                    }
                }
            }

            for (Map.Entry<Long, String> entry : offenders.entrySet()) {
                // check if their name is all whitespace.
                boolean allwhitespace = true;

                for (char c : entry.getValue().toCharArray()) {
                    if (!UCharacter.isWhitespace(c)) {
                        allwhitespace = false;
                        break;
                    }
                }

                String newnick = (allwhitespace) ? entry.getKey().toString() : entry.getValue();

                event.getGuild().getMemberById(entry.getKey()).modifyNickname(newnick).queue();
            }
        }
    }

    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {
        updateNameWithRules(event.getGuild(), event.getMember(), event.getUser());
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        updateNameWithRules(event.getGuild(), event.getMember(), event.getUser());
    }
}

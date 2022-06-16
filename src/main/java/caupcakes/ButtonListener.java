package caupcakes;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import static caupcakes.SlashCommandListener.decodeData;
import static caupcakes.SlashCommandListener.replyFailure;

public class ButtonListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
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

            event.getMessage().delete().queue(

            return;
        }
    }
}

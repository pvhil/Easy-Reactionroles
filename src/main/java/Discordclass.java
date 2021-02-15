import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.List;
import java.util.Objects;

public class Discordclass extends ListenerAdapter {
    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.getMessage().getMember().hasPermission(Permission.MANAGE_ROLES)) {
            return;
        }
        String[] args = event.getMessage().getContentRaw().split("\\s+");

        if (args[0].equalsIgnoreCase("rr")) {
            if (args.length == 1) {
                event.getChannel().sendMessage("Please state a Message ID, mention a role and a usable emoji!").queue();
            }
            if (args[1].equalsIgnoreCase("delete") && event.getMember().hasPermission(Permission.MANAGE_ROLES)) {
                if (args[2].equalsIgnoreCase("all")) {
                    try {
                        FileUtils.cleanDirectory(new File("data"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    event.getChannel().sendMessage("Deleted all Reactionroles data!").queue();
                } else if (args.length == 3) {
                    File directory = new File("data");
                    for (File f : directory.listFiles()) {
                        if (f.getName().contains(args[2])) {
                            if (f.delete()) {
                                event.getChannel().sendMessage("Deleted Reactionrole data for this message!").queue();
                            }
                        } else {
                            event.getChannel().sendMessage("Could not find this Channel in the database!").queue();

                        }
                    }
                }
            } else {
                List<Role> roles = event.getMessage().getMentionedRoles();
                List<String> emojis = EmojiParser.extractEmojis(event.getMessage().getContentRaw());
                List<Emote> emotes = event.getMessage().getEmotes();
                TextChannel channel = event.getChannel();
                String message = "test";

                //check if one role and one emote is given
                if (roles.size() < 1 || (emotes.size() != 1 && emojis.size() != 1)) {
                    event.getChannel().sendMessage("Please state VALID a Message ID, mention a role and a usable emoji!").queue();
                    return;
                }

                StringBuilder roleTemp = new StringBuilder();
                for (int i = 0; i < roles.size(); i++) {
                    roleTemp.append(roles.get(i).getId() + ",");
                }
                String roleID = roleTemp.toString();

                String emoteID;

                for (int i = 1; i < args.length; i++) {
                    if (args[i].chars().allMatch(Character::isDigit) && !args[i].contains("@")) {
                        message = args[i];
                        break;
                    }
                }
                String emoteTemp;

                if (emojis.size() == 0) {
                    channel.retrieveMessageById(message).queue(message1 -> message1.addReaction(emotes.get(0)).queue());
                    emoteID = emotes.get(0).getId();
                    emoteTemp = emotes.get(0).getAsMention();
                } else {
                    channel.retrieveMessageById(message).queue(message1 -> message1.addReaction(emojis.get(0)).queue());
                    emoteID = EmojiParser.parseToHtmlDecimal(emojis.get(0));
                    emoteTemp = emojis.get(0);
                }

                messagejson info = new messagejson(message, emoteID, roleID);
                String json = gson.toJson(info);

                try {
                    FileWriter writer = new FileWriter("data/" + message + emoteID + ".json");
                    writer.write(json);
                    writer.close();

                } catch (IOException ignored) {
                }
                StringBuilder rolesmention = new StringBuilder();
                for (int i = 0; i < roles.size(); i++) {
                    rolesmention.append(roles.get(i).getAsMention() + ",");
                }

                event.getChannel().sendMessage("Added a Reactionlistener on this message!\nIf somebody reacts with " + emoteTemp + " he will get the Role " + rolesmention.toString() + "!").queue();
            }
        }
    }

    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (Objects.requireNonNull(event.getUser()).isBot()) {
            return;
        }
        String messageID = event.getMessageId();

        String emoteID;
        if (event.getReactionEmote().isEmote()) {
            emoteID = event.getReactionEmote().getId();
        } else {
            emoteID = EmojiParser.parseToHtmlDecimal(event.getReactionEmote().getAsReactionCode());
        }

        File json = new File("data/" + messageID + emoteID + ".json");
        if (!json.exists()) {
            return;
        }
        try {
            Reader reader = new FileReader("data/" + messageID + emoteID + ".json");

            JSONTokener token = new JSONTokener(reader);
            JSONObject obj = new JSONObject(token);

            String messageIDjson = obj.getString("messageID");
            String emoteIDjson = obj.getString("emoteID");
            String roleIDjson = obj.getString("roleID");

            String[] roles = roleIDjson.split(",");

            for (int i = 0; i < roles.length; i++) {
                event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(roles[i])).queue();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

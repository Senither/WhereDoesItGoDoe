package com.senither.wheredoesitgodoe.handlers;

import com.senither.wheredoesitgodoe.WhereDoesItGoDoe;
import com.senither.wheredoesitgodoe.utils.SendMessageHandler;
import com.senither.wheredoesitgodoe.utils.URLRedirect;
import com.senither.wheredoesitgodoe.utils.URLTrimmer;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.awt.*;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


public class MessageEventHandler extends ListenerAdapter {

    private static final String MENTION_MESSAGE = String.join("\n", Arrays.asList(
            "Hi there! I'm **%s**, a simple Discord bot built by %s!",
            "All I do is scan messages for links, if I find any links in any message I will check if they",
            "redirect anywhere, if they do I will let you know where they go and which sites they",
            "go through on the way there if they redirect multiple times.",
            "",
            "You can find all of my source code on github:",
            "https://github.com/Senither/WhereDoesItGoDoe",
            "",
            "You can invite me to your server with the link below:",
            "https://senither.com/WhereDoesItGoDoe"
    ));

    private static final Pattern URL_REGEX = Pattern.compile(
            "^(?:(?:https?):\\/\\/)(?:\\S+(?::\\S*)?@)?(?:(?!10(?:\\.\\d{1,3}){3})" +
                    "(?!127(?:\\.\\d{1,3}){3})(?!169\\.254(?:\\.\\d{1,3}){2})(?!192\\.168(?:\\.\\d{1,3}){2})" +
                    "(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])" +
                    "(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|" +
                    "(?:(?:[a-z\\x{00a1}-\\x{ffff}0-9]+-?)*[a-z\\x{00a1}-\\x{ffff}0-9]+)(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}0-9]+-?)" +
                    "*[a-z\\x{00a1}-\\x{ffff}0-9]+)*(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}]{2,})))(?::\\d{2,5})?(?:\\/[^\\s]*)?$"
    );

    private static final Pattern USER_REGEX = Pattern.compile("<@(!|)+[0-9]{16,}+>", Pattern.CASE_INSENSITIVE);

    private final WhereDoesItGoDoe bot;

    public MessageEventHandler(WhereDoesItGoDoe bot) {
        this.bot = bot;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        onMessageEvent(event.getMessage());
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        onMessageEvent(event.getMessage());
    }

    private void onMessageEvent(Message message) {
        if (!shouldScanForUrls(message)) {
            return;
        }

        Set<String> urls = new HashSet<>();
        for (String word : message.getContent().split(" ")) {
            word = URLTrimmer.trim(word);

            if (word.length() == 0) continue;
            if (URL_REGEX.matcher(word).find()) {
                // Skipping Discord invite links, they should always be safe anyway.
                if (word.startsWith("https://discord.gg")) {
                    continue;
                }
                urls.add(word);
            }
        }

        if (urls.isEmpty()) {
            return;
        }

        for (String url : urls) {
            try {
                List<String> redirects = URLRedirect.get(url);
                if (redirects.size() <= 1) {
                    continue;
                }

                List<String> links = new ArrayList<>();
                links.add(String.format("<%s> redirects to <%s>", url, redirects.get(redirects.size() - 1)));

                if (redirects.size() > 2) {
                    links.add("\n**The link jumps through the following sites:**");
                    for (int i = 1; i < redirects.size(); i++) {
                        links.add(String.format("<%s> :arrow_right: <%s>", redirects.get(i - 1), redirects.get(i)));
                    }
                }

                SendMessageHandler.sendSuccess(message, String.join("\n", links)).queue();
            } catch (UnknownHostException ex) {
                SendMessageHandler.sendFailure(message, String.format(
                        "<%s> linked by %s doesn't actually go anywhere D:",
                        url, message.getAuthor().getAsMention()
                )).queue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean shouldScanForUrls(Message message) {
        // Ignore messages sent by the bot itself, but still include messages sent by other bots.
        if (Objects.equals(message.getAuthor().getId(), bot.getJDA().getSelfUser().getId())) {
            return false;
        }

        // If it's a private message we can ignore it.
        if (!message.getChannelType().isGuild()) {
            sendTagInformationMessage(message);
            return false;
        }

        if (isBotMentionedWithNothingElse(message)) {
            sendTagInformationMessage(message);
            return false;
        }
        return true;
    }

    private boolean isBotMentionedWithNothingElse(Message message) {
        String[] args = message.getRawContent().trim().split(" ");
        return args.length == 1 &&
                USER_REGEX.matcher(args[0]).matches() &&
                message.getMentionedUsers().get(0).getId().equals(message.getJDA().getSelfUser().getId());
    }

    private void sendTagInformationMessage(Message message) {
        String author = "**Senither#8023**";
        if (message.getChannelType().isGuild() && message.getGuild().getMemberById(88739639380172800L) != null) {
            author = "<@88739639380172800>";
        }

        message.getChannel().sendMessage(new EmbedBuilder()
                .setColor(Color.decode("#E91E63"))
                .setDescription(String.format(MENTION_MESSAGE,
                        message.getJDA().getSelfUser().getName(),
                        author
                ))
                .setFooter("This message will be automatically deleted in one minute.", null)
                .build()
        ).queue(sentMessage -> sentMessage.delete().queueAfter(1, TimeUnit.MINUTES));
    }
}

package com.senither.wheredoesitgodoe;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Pattern;

public class WhereDoesItGoDoe extends ListenerAdapter {

    private static final Pattern URL_REGEX = Pattern.compile(
            "^(?:(?:https?):\\/\\/)(?:\\S+(?::\\S*)?@)?(?:(?!10(?:\\.\\d{1,3}){3})" +
                    "(?!127(?:\\.\\d{1,3}){3})(?!169\\.254(?:\\.\\d{1,3}){2})(?!192\\.168(?:\\.\\d{1,3}){2})" +
                    "(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])" +
                    "(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|" +
                    "(?:(?:[a-z\\x{00a1}-\\x{ffff}0-9]+-?)*[a-z\\x{00a1}-\\x{ffff}0-9]+)(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}0-9]+-?)" +
                    "*[a-z\\x{00a1}-\\x{ffff}0-9]+)*(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}]{2,})))(?::\\d{2,5})?(?:\\/[^\\s]*)?$"
    );

    private final JDA jda;

    public WhereDoesItGoDoe(JDA jda) {
        this.jda = jda;
        this.jda.addEventListener(this);
    }

    public static void main(String[] args) throws LoginException, RateLimitedException {
        if (args.length == 0) {
            System.err.println("Missing argument \"bot token\", this is required for the bot to work!");
            System.err.println("java -jar WhereDoesItGoDoe.jar <bot token>");
            System.exit(20);
        }

        new WhereDoesItGoDoe(new JDABuilder(AccountType.BOT)
                .setReconnectQueue(new SessionReconnectQueue())
                .setAutoReconnect(true)
                .setToken(args[0])
                .buildAsync()
        );
    }

    private void onMessageEvent(Message message) {
        // Ignore messages sent by the bot itself, but still include messages sent by other bots.
        if (Objects.equals(message.getAuthor().getId(), jda.getSelfUser().getId())) {
            return;
        }

        // If it's a private message we can ignore it.
        if (!message.getChannelType().isGuild()) {
            return;
        }

        Set<String> urls = new HashSet<>();
        for (String word : message.getContent().split(" ")) {
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
                List<String> redirects = fetchRedirect(url, new ArrayList<>());

                if (redirects.size() <= 1) {
                    return;
                }

                List<String> links = new ArrayList<>();
                links.add(String.format("<%s> redirects to <%s>", url, redirects.get(redirects.size() - 1)));

                if (redirects.size() > 2) {
                    links.add("\n**The link jumps through the following sites:**");
                    for (int i = 1; i < redirects.size(); i++) {
                        links.add(String.format("<%s> :arrow_right: <%s>", redirects.get(i - 1), redirects.get(i)));
                    }
                }

                message.getChannel().sendMessage(String.join("\n", links)).queue();
            } catch (UnknownHostException ex) {
                message.getChannel().sendMessage(String.format(
                        "<%s> linked by %s doesn't actually go anywhere D:",
                        url, message.getAuthor().getAsMention())
                ).queue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<String> fetchRedirect(String url, List<String> redirects) throws IOException {
        redirects.add(url);

        HttpURLConnection con = (HttpURLConnection) (new URL(url).openConnection());
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setInstanceFollowRedirects(false);
        con.connect();

        if (con.getHeaderField("Location") == null) {
            return redirects;
        }
        return fetchRedirect(con.getHeaderField("Location"), redirects);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        onMessageEvent(event.getMessage());
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        onMessageEvent(event.getMessage());
    }
}

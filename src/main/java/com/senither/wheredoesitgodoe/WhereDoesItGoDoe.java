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

public class WhereDoesItGoDoe extends ListenerAdapter {

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
        if (!message.getChannelType().isGuild()) {
            return;
        }
        System.out.println(message.getContent());
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

package com.senither.wheredoesitgodoe;

import com.senither.wheredoesitgodoe.handlers.MessageEventHandler;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;

import javax.security.auth.login.LoginException;

public class WhereDoesItGoDoe {

    private final JDA jda;

    public WhereDoesItGoDoe(JDA jda) {
        this.jda = jda;
        this.jda.addEventListener(new MessageEventHandler(this));
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

    public JDA getJDA() {
        return jda;
    }
}

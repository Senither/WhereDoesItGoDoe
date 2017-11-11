package com.senither.wheredoesitgodoe.utils;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.requests.RestAction;

import java.awt.*;

public class SendMessageHandler {

    public static RestAction<Message> sendSuccess(Message message, String string) {
        return send(message, string, Color.CYAN);
    }

    public static RestAction<Message> sendFailure(Message message, String string) {
        return send(message, string, Color.RED);
    }

    private static RestAction<Message> send(Message message, String string, Color color) {
        if (!canSendMessages(message)) {
            return new RestAction.EmptyRestAction<>(message.getJDA(), message);
        }

        if (!canSendEmbeddedMessages(message)) {
            return message.getTextChannel().sendMessage(string);
        }

        return message.getTextChannel().sendMessage(new EmbedBuilder()
                .setColor(color)
                .setDescription(string)
                .build()
        );
    }

    private static boolean canSendMessages(Message message) {
        return message.getTextChannel().canTalk();
    }

    private static boolean canSendEmbeddedMessages(Message message) {
        return message.getGuild().getSelfMember().hasPermission(
                message.getTextChannel(), Permission.MESSAGE_EMBED_LINKS
        );
    }
}

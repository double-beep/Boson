package org.sobotics.boson.sample;

import java.util.HashMap;
import java.util.Map;

import org.sobotics.boson.framework.model.chat.ChatRoom;
import org.sobotics.boson.framework.model.stackexchange.Tag;
import org.sobotics.boson.framework.services.chat.ChatRoomService;
import org.sobotics.boson.framework.services.chat.commands.Alive;
import org.sobotics.boson.framework.services.chat.commands.Command;
import org.sobotics.boson.framework.services.chat.commands.Help;
import org.sobotics.boson.framework.services.chat.filters.EmptyFilter;
import org.sobotics.boson.framework.services.chat.filters.Filter;
import org.sobotics.boson.framework.services.chat.listeners.MessageReplyEventListener;
import org.sobotics.boson.framework.services.chat.listeners.UserMentionedListener;
import org.sobotics.boson.framework.services.chat.monitors.Monitor;
import org.sobotics.boson.framework.services.chat.monitors.TagMonitor;
import org.sobotics.boson.framework.services.chat.printers.ListOfTagsPrinter;
import org.sobotics.chatexchange.chat.Room;

public class TagMonitorBot {

    private Room room;
    private String site;
    private int frequency;

    public TagMonitorBot(Room room, String site, int frequency) {
        this.room = room;
        this.site = site;
        this.frequency = frequency;
    }

    public void start() {
        ChatRoom chatRoom = new ChatRoom(room);

        Map<Command, Object[]> userMentionCommands = new HashMap<>();
        userMentionCommands.put(new Alive(), new Object[0]);
        userMentionCommands.put(new Help(), new Object[0]);
        chatRoom.setUserMentionedEventConsumer(new UserMentionedListener().getUserMentionedEventConsumer(room, userMentionCommands));

        Map<Command, Object[]> messageReplyCommands = new HashMap<>();
        messageReplyCommands.put(new Alive(), new Object[0]);
        messageReplyCommands.put(new Help(), new Object[0]);
        chatRoom.setMessageReplyEventConsumer(new MessageReplyEventListener().getMessageReplyEventListener(room, messageReplyCommands));

        Filter[] filters = {new EmptyFilter<Tag>()};
        Monitor[] monitors = {new TagMonitor(chatRoom, frequency, site, "", "", filters,
                new ListOfTagsPrinter(site), null)};

        ChatRoomService service = new ChatRoomService(chatRoom, monitors);
        service.initializeService();
        service.startService();

    }
}

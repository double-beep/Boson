package org.sobotics.boson.framework.services.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.sobotics.boson.framework.model.chat.ChatRoom;
import org.sobotics.boson.framework.services.chat.monitors.Monitor;
import org.sobotics.chatexchange.chat.event.EventType;

public class ChatRoomService {
    private ChatRoom chatRoom;
    private Monitor[] monitors;
    private List<ScheduledExecutorService> services;

    public ChatRoomService(ChatRoom chatRoom, Monitor[] monitors) {
        this.chatRoom = chatRoom;
        this.monitors = monitors;
        this.services = new ArrayList<>();
    }

    public void initializeService() {
        if (chatRoom.getUserMentionedEventConsumer() != null) {
            chatRoom.getRoom().addEventListener(EventType.USER_MENTIONED, chatRoom.getUserMentionedEventConsumer());
        }

        if (chatRoom.getMessageReplyEventConsumer() != null) {
            chatRoom.getRoom().addEventListener(EventType.MESSAGE_REPLY, chatRoom.getMessageReplyEventConsumer());
        }

        if (chatRoom.getMessageEditedEventConsumer() != null) {
            chatRoom.getRoom().addEventListener(EventType.MESSAGE_EDITED, chatRoom.getMessageEditedEventConsumer());
        }

        if (chatRoom.getMessagePostedEventConsumer() != null) {
            chatRoom.getRoom().addEventListener(EventType.MESSAGE_POSTED, chatRoom.getMessagePostedEventConsumer());
        }

        if (chatRoom.getUserEnteredEventConsumer() != null) {
            chatRoom.getRoom().addEventListener(EventType.USER_ENTERED, chatRoom.getUserEnteredEventConsumer());
        }

        if (chatRoom.getUserLeftEventConsumer() != null) {
            chatRoom.getRoom().addEventListener(EventType.USER_LEFT, chatRoom.getUserLeftEventConsumer());
        }

        if (chatRoom.getMessageStarredEventConsumer() != null) {
            chatRoom.getRoom().addEventListener(EventType.MESSAGE_STARRED, chatRoom.getMessageStarredEventConsumer());
        }

        if (chatRoom.getMessageDeletedEventConsumer() != null) {
            chatRoom.getRoom().addEventListener(EventType.MESSAGE_DELETED, chatRoom.getMessageDeletedEventConsumer());
        }

        if (chatRoom.getKickedEventConsumer() != null) {
            chatRoom.getRoom().addEventListener(EventType.KICKED, chatRoom.getKickedEventConsumer());
        }
    }

    public void startService() {
        for (Monitor monitor : monitors) {
            services.add(monitor.startMonitor());
        }
    }

    public void stopService() {
        for (Monitor monitor : monitors) {
            monitor.stopMonitor();
        }
        for (ScheduledExecutorService service: services) {
            service.shutdown();
        }
    }

    public void terminateService() {
        chatRoom.getRoom().leave();
    }

    public void addMonitor(Monitor monitor) {
        Monitor[] tempMonitor = new Monitor[monitors.length + 1];
        System.arraycopy(monitors, 0, tempMonitor, 0, monitors.length);
        tempMonitor[monitors.length] = monitor;
        monitors = tempMonitor;
        services.add(monitor.startMonitor());
    }
}

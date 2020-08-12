package org.sobotics.boson.bot;

import java.util.List;

import org.sobotics.boson.bot.model.FilterTypes;
import org.sobotics.boson.bot.model.PrinterTypes;
import org.sobotics.boson.bot.model.Type;
import org.sobotics.boson.framework.model.chat.ChatRoom;
import org.sobotics.boson.framework.services.chat.ChatRoomService;
import org.sobotics.chatexchange.chat.ChatHost;

public class Bot {
    private String identifier;
    private ChatRoom chatRoom;
    private ChatRoomService chatRoomService;
    private String creationMessageUrl;

    private Type type;
    private List<FilterTypes> filterTypes;
    private PrinterTypes printer;
    private String siteName;
    private Integer frequency;
    private Integer chatRoomId;
    private ChatHost chatHost;

    public Bot(String identifier, ChatRoom chatRoom, ChatRoomService chatRoomService, Type type,
               List<FilterTypes> filterTypes, PrinterTypes printer, Integer frequency, String creationMessageUrl) {
        this.identifier = identifier;
        this.chatRoom = chatRoom;
        this.chatRoomService = chatRoomService;
        this.creationMessageUrl = creationMessageUrl;

        this.type = type;
        this.filterTypes = filterTypes;
        this.printer = printer;
        this.frequency = frequency;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public ChatRoomService getChatRoomService() {
        return chatRoomService;
    }

    public void setChatRoomService(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    public String getCreationMessageUrl() {
        return creationMessageUrl;
    }

    public void setCreationMessageUrl(String creationMessageUrl) {
        this.creationMessageUrl = creationMessageUrl;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<FilterTypes> getFilterTypes() {
        return filterTypes;
    }

    public void setFilterTypes(List<FilterTypes> filterTypes) {
        this.filterTypes = filterTypes;
    }

    public PrinterTypes getPrinter() {
        return printer;
    }

    public void setPrinter(PrinterTypes printer) {
        this.printer = printer;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public Integer getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(Integer chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public ChatHost getChatHost() {
        return chatHost;
    }

    public void setChatHost(ChatHost chatHost) {
        this.chatHost = chatHost;
    }
}

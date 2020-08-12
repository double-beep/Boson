package org.sobotics.boson.bot;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.boson.bot.model.*;
import org.sobotics.boson.framework.model.chat.ChatRoom;
import org.sobotics.boson.framework.services.chat.ChatRoomService;
import org.sobotics.boson.framework.services.chat.commands.Alive;
import org.sobotics.boson.framework.services.chat.commands.Command;
import org.sobotics.boson.framework.services.chat.filters.*;
import org.sobotics.boson.framework.services.chat.listeners.MessageReplyEventListener;
import org.sobotics.boson.framework.services.chat.listeners.UserMentionedListener;
import org.sobotics.boson.framework.services.chat.monitors.*;
import org.sobotics.boson.framework.services.chat.printers.*;
import org.sobotics.boson.framework.services.dashboard.DashboardService;
import org.sobotics.boson.framework.services.dashboard.HiggsService;
import org.sobotics.boson.framework.services.others.HeatDetectorService;
import org.sobotics.chatexchange.chat.ChatHost;
import org.sobotics.chatexchange.chat.Message;
import org.sobotics.chatexchange.chat.Room;
import org.sobotics.chatexchange.chat.StackExchangeClient;
import org.sobotics.chatexchange.chat.event.EventType;

import io.swagger.client.ApiException;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class BosonBot {

    private static Logger logger = LoggerFactory.getLogger(BosonBot.class);

    private Room room;
    private StackExchangeClient client;
    private Map<String, Bot> bots;
    private String apiKey;
    private String apiToken;
    private String dashboardUrl;
    private String dashboardApi;
    private String dashboardKey;


    public BosonBot(Room room, StackExchangeClient client, String apiKey, String apiToken) {
        this.room = room;
        this.client = client;
        this.bots = new HashMap<>();
        this.apiKey = apiKey;
        this.apiToken = apiToken;
    }

    public BosonBot(Room room, StackExchangeClient client, String apiKey, String apiToken,
                    String dashboardUrl, String dashboardApi, String dashboardKey) {
        this.room = room;
        this.client = client;
        this.bots = new HashMap<>();
        this.apiKey = apiKey;
        this.apiToken = apiToken;
        this.dashboardUrl = dashboardUrl;
        this.dashboardApi = dashboardApi;
        this.dashboardKey = dashboardKey;
    }

    public void start() {
        try {
            room.send("Boson Started");
            logger.info("Started");
            room.addEventListener(EventType.USER_MENTIONED, event -> {
                Message message = event.getMessage();
                System.out.println(message.getPlainContent());
                String[] arguments = message.getPlainContent().split(" ");
                switch (arguments[1]) {
                    case "track":
                        trackCommand(message);
                        break;
                    case "help":
                        room.send("Use the command `track sitename posttype frequency` to start tracking sites "
                              + "(`track -h` gives a longer description)");
                        break;
                    case "alive":
                        room.send("Yes, I'm alive");
                        break;
                    case "stop":
                        stopCommand(arguments[2]);
                        break;
                    case "list":
                        listCommand();
                        break;
                }
            });
        }
        catch (Exception e) { // TODO : REMOVE THE POKEMON EXCEPTION HANDLING AFTER TESTING IS OVER
            e.printStackTrace();
        }
    }

    private void listCommand() {
        room.send(String.join(";", bots.keySet()));
    }

    private void stopCommand(String argument) {
        try {
            if (bots.containsKey(argument)) {
                ChatRoomService chatRoomService = bots.get(argument).getChatRoomService();
                int stoppingBotId = bots.get(argument).getChatRoom().getRoomId();
                chatRoomService.stopService();
                bots.remove(argument);
                if (findChatRoomByRoomId(stoppingBotId) == null && stoppingBotId != room.getRoomId()) {
                    chatRoomService.terminateService();
                }
                room.send("Bot " + argument + " stopped");
            } else {
                room.send("Wrong bot ID");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void trackCommand(Message message) {
        String[] splits = message.getPlainContent().split(" ");
        String[] arguments = Arrays.copyOfRange(splits, 2, splits.length);
        try {
            Namespace res = parseArguments(arguments);

            if (res == null) {
                return;
            }

            String site = res.getString("site");
            Type posttype = res.get("type");
            int frequency = res.getInt("frequency");
            Integer otherRoomId = res.getInt("room");
            ChatHost otherHost = res.get("host");
            List<FilterTypes> requestedFilters = res.get("filter");
            List<String> requestedValues = res.get("value");
            String id = res.get("name");
            if (id == null) {
                id = getUniqueId();
            }
            if (bots.containsKey(id)) {
                room.send("There is already another bot with the same name. Please create a unique name for your bot");
                return;
            }
            List<Filter> filters = new ArrayList<>();
            try {
                if (requestedFilters != null) {
                    for (int i = 0; i < requestedFilters.size(); i++) {
                        filters.add(getFilterFromRequestedFilter(requestedFilters.get(i), requestedValues.get(i), site));
                    }
                } else {
                    filters.add(new EmptyFilter());
                }
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
                room.send("`value` should be a Integer");
                return;
            }

            ChatRoom chatRoom;
            if (otherRoomId != null) {
                if (otherHost == null) {
                    throw new TrackParsingException("Unknown chat host. Use one of STACK_OVERFLOW or STACK_EXCHANGE");
                }
                chatRoom = getChatRoom(otherRoomId, otherHost);
            }
            else {
                chatRoom = new ChatRoom(room);
            }

            chatRoom.getRoom().send("Tracking " + posttype + " on " + site + " as directed in [" + room.getThumbs().getName()
                    + "](" + room.getHost().getBaseUrl() + "/rooms/" + room.getRoomId() + ")");

            PrinterService printerService = getPrinterServiceFromPrinter(res, chatRoom);
            DashboardService dashboardService = null;
            try {
                dashboardService = getDashboardService(res, id);
            }
            catch (ApiException e) {
                e.printStackTrace();
            }
            Monitor[] monitors = getMonitors(site, posttype, frequency, chatRoom,
                    filters.toArray(new Filter[0]), printerService, dashboardService);
            if (monitors == null) {
                room.send("The only types supported are questions, answers and tags");
            }
            else {
                ChatRoomService service;
                service = new ChatRoomService(chatRoom, monitors);
                String similarRoom;
                similarRoom = findChatRoomByRoomId(chatRoom.getRoomId());
                if (similarRoom == null && chatRoom.getRoomId() != room.getRoomId()) {
                    service.initializeService();
                }
                service.startService();
                bots.put(id, new Bot(id, chatRoom, service, posttype, requestedFilters, res.get("printer"), frequency,
                        room.getHost().getBaseUrl() + "/messages/" + message.getId() + "/history"));
                room.send("New tracker started: [" + id + "](" + bots.get(id).getCreationMessageUrl() + ")");
            }
        }
        catch (TrackParsingException e) {
            room.send("    " + e.getMessage().replace("\n", "\n    "));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DashboardService getDashboardService(Namespace res, String id) throws ApiException {
        DashboardTypes dashboard = res.get("dash");
        if (dashboard == null) {
            return null;
        }
        else {
            switch (dashboard) {
                case HIGGS: return new HiggsService(dashboardUrl, dashboardApi, dashboardKey, id);
                default: return null;
            }
        }
    }

    private PrinterService getPrinterServiceFromPrinter(Namespace res, ChatRoom chatRoom) {

        PrinterTypes printer = res.get("printer");
        Type type = res.get("type");
        String site = res.getString("site");

        if (printer == null) {
            switch (type) {
                case questions:
                case answers:
                case comments:
                case posts:
                    return new GenericContentPrinterService<>(site);
                case tags:
                    return new NewTagPrinter(site);
            }
        }

        switch (printer) {
            case ONE_BOX:
                return new ContentOneBoxPrinter<>(chatRoom);
            case LIST_TAGS:
                return new ListOfTagsPrinter(site);
            case HEAT_DETECTOR:
                return new HeatDetectorPrinter(chatRoom);
        }

        return new GenericContentPrinterService<>(site);

    }

    private Filter getFilterFromRequestedFilter(FilterTypes filter, String value, String site) throws NumberFormatException {

        if (filter != null) {

            switch (filter) {
                case REPUTATION:
                    return new ReputationFilter(Integer.parseInt(value));
                case LENGTH:
                    return new LengthFilter(Integer.parseInt(value));
                case USER_ID:
                    return new UserIdFilter(Integer.parseInt(value));
                case POST_ID:
                    return new PostIDFilter(Integer.parseInt(value));
                case CONTAINS:
                    return new BodyFilter(value.replace("%20", " "));
                case TAG:
                    return new TaggedFilter(value.split(";"));
                case BURNED_TAG:
                    return new BurnedTagFilter(value);
                case HEAT_DETECTOR:
                    return new HeatDetectorFilter(new HeatDetectorService(Integer.parseInt(value), "stackoverflow"));
            }

        }

        return new EmptyFilter();
    }

    private String getUniqueId() {
        char[] characs = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        Random secureRandom = new SecureRandom();
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            id.append(characs[secureRandom.nextInt(characs.length)]);
        }
        return id.toString();
    }

    private Monitor[] getMonitors(String site, Type posttype, int frequency, ChatRoom chatRoom, Filter[] filters,
                                  PrinterService printerService, DashboardService dashboardService) {

        switch (posttype) {
            case questions:
                return new Monitor[]{new QuestionMonitor(chatRoom, frequency, site, apiKey, apiToken,
                        filters, printerService, dashboardService)};
            case answers:
                return new Monitor[]{new AnswerMonitor(chatRoom, frequency, site, apiKey, apiToken,
                        filters, printerService, dashboardService)};
            case comments:
                return new Monitor[]{new CommentMonitor(chatRoom, frequency, site, apiKey, apiToken,
                        filters, printerService, dashboardService)};
            case posts:
                return new Monitor[]{new PostMonitor(chatRoom, frequency, site, apiKey, apiToken,
                        filters, printerService, dashboardService)};
            case tags:
                return new Monitor[]{new TagMonitor(chatRoom, frequency, site, apiKey, apiToken,
                        filters, printerService, dashboardService)};
        }
        return null;
    }

    private ChatRoom getChatRoom(int otherRoomId, ChatHost otherRoomHost) {
        ChatRoom chatRoom;
        Room otherRoom = client.joinRoom(otherRoomHost, otherRoomId);
        otherRoom.send("Boson started");
        chatRoom = new ChatRoom(otherRoom);
        Map<Command, Object[]> userMentionCommands = new HashMap<>();
        userMentionCommands.put(new Alive(), new Object[0]);
        chatRoom.setUserMentionedEventConsumer(new UserMentionedListener().getUserMentionedEventConsumer(otherRoom, userMentionCommands));

        Map<Command, Object[]> messageReplyCommands = new HashMap<>();
        messageReplyCommands.put(new Alive(), new Object[0]);
        chatRoom.setMessageReplyEventConsumer(new MessageReplyEventListener().getMessageReplyEventListener(otherRoom, messageReplyCommands));
        return chatRoom;
    }

    private String findChatRoomByRoomId(int roomId) {
        for (String key: bots.keySet()) {
            System.out.println("Searching " + key + " for roomID " + roomId);
            Bot bot = bots.get(key);
            if (bot.getChatRoom().getRoomId() == roomId) {
                System.out.println("Found " + key + " for roomID " + roomId);
                return key;
            }
        }
        return null;
    }

    private Namespace parseArguments(String[] args) throws TrackParsingException {

        ArgumentParser parser = ArgumentParsers.newFor("track").addHelp(false).build()
                .description("Argument Parser for the bot.");

        parser.addArgument("site").type(String.class).metavar("sitename")
                .help("Site where the bot has to be run");

        parser.addArgument("type").type(Type.class).metavar("trackingType")
                .help("Type of the tracker needed. Can be one of {questions,answers,comments,posts,tags}");

        parser.addArgument("frequency").type(Integer.class)
                .help("Frequency of the tracker in seconds");

        parser.addArgument("-d", "--dash").type(DashboardTypes.class).nargs("?")
                .help("Set the dashboard to be used by the bot");

        parser.addArgument("-f", "--filter").type(FilterTypes.class).nargs("*")
                .help("Set the filters used by the bot");

        parser.addArgument("-h", "--help").action(new BosonHelpArgumentAction())
                .help("Display this message");

        parser.addArgument("-n", "--name").type(String.class).nargs("?")
                .help("Give a name to your bot (we will generate a 10 digit value, if you don't)");

        parser.addArgument("-p", "--printer").type(PrinterTypes.class).nargs("?")
                .help("Set the printer to be used by the bot");

        parser.addArgument("-r", "--room").type(Integer.class).nargs("?")
                .help("Set the room where it has to run");

        parser.addArgument("-t", "--host").type(ChatHost.class).nargs("?")
                .choices(ChatHost.STACK_OVERFLOW, ChatHost.STACK_EXCHANGE)
                .help("Set the chat host of that room");

        parser.addArgument("-v", "--value").type(String.class).nargs("*")
                .help("Set the value needed for the filter, depending on it's type");

        Namespace res;
        try {
            res = parser.parseArgs(args);
        } catch (BosonHelpScreenParserException e) {
            throw new TrackParsingException(e.getParserMessage());
        } catch (ArgumentParserException e) {
            StringWriter out = new StringWriter();
            PrintWriter writer = new PrintWriter(out);
            parser.handleError(e, writer);
            throw new TrackParsingException(out.toString());
        }
        System.out.println(res);

        return res;
    }

}

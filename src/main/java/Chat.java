import org.eclipse.jetty.websocket.api.*;
import org.json.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import static j2html.TagCreator.*;
import static spark.Spark.*;

public class Chat {


    private Map<Session, String> userNames = new ConcurrentHashMap<>();
    private Map<String, String> userInChannel = new ConcurrentHashMap<>();
    private List<String> channels = new CopyOnWriteArrayList<String>();

    private ChatBot chatbot= new ChatBot();

    public Map<Session, String> getUsernames() {
        return userNames;
    }
    public Map<String, String> getUserToChannel() {
        return userInChannel;
    }

    public void initialize(){
        staticFiles.location("/public"); //index.html is served at localhost:4567 (default port)
        staticFiles.expireTime(1);

        channels.add("chatbot");
        webSocket("/chat", ChatWebSocketHandler.class);
        init();
    }

    public void refresh(){
        userNames.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(String.valueOf(new JSONObject()
                        .put("userMessage", "")
                        .put("channellist", channels)
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    public void broadcastMessage(String sender, String message, String channel) {
        userNames.keySet().stream().filter(Session::isOpen)
                .filter(session -> {
                    try{
                        return userInChannel.get( userNames.get(session) ).equals(channel);
                    }catch(NullPointerException ex){return false;}
                })
                .forEach(session -> {
                    try {
                        session.getRemote().sendString(String.valueOf(new JSONObject()
                                .put("userMessage", createHtmlMessageFromSender(sender, message))
                                .put("channellist", channels)
                        ));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

    }

    //Builds a HTML element with a sender-name, a message, and a timestamp,
    private String createHtmlMessageFromSender(String sender, String message) {
        return article().with(
                b(sender + " says:"),
                p(message),
                span().withClass("timestamp").withText(new SimpleDateFormat("HH:mm:ss").format(new Date()))
        ).render();
    }

    public void addChannel(String channelName){
        channels.add(channelName);
    }


    public void addUserToChannel(String username, String channel){
        if(userInChannel.containsKey(username)){
            removeUserFromChannel(username);
        }
        userInChannel.put(username, channel);
        broadcastMessage(channel, (username + " joined to " + channel), channel);
    }

    public boolean addUsername(Session user, String username){
        try{
            if(userNames.containsValue(username)){
                user.getRemote().sendString(String.valueOf(new JSONObject()
                        .put("userMessage", "TAKEN_USERNAME") ) );
                return false;
            }else{
                userNames.put(user, username);
                return true;
            }
        } catch(Exception ex){return false;}

    }

    public void removeUserFromChannel(String username){
        String channelLeft = userInChannel.get(username);
        userInChannel.remove(username);
        broadcastMessage(channelLeft, (username + " left the " + channelLeft), channelLeft);
    }

    public void removeUser(Session user) {
        String username = userNames.get(user);
        removeUserFromChannel(username);
        userNames.remove(user);
    }

    public void refreshForUser(Session user) {
        try {
            user.getRemote().sendString(String.valueOf(new JSONObject()
                    .put("userMessage", "")
                    .put("channellist", channels)
            ));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendMessageToUser(Session user, String message) {
        try {
            user.getRemote().sendString(String.valueOf(new JSONObject()
                    .put("userMessage", createHtmlMessageFromSender("Server", message))
                    .put("channellist", channels)
            ));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void askChatbot(String question){
        String answer = chatbot.getAnswer(question);
        broadcastMessage("chatbot", answer, "chatbot");
    }

}
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class ChatWebSocketHandler {

    private Chat chat;

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        chat=Main.getChatInstance();
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        String username = chat.getUsernames().get(user);
        chat.removeUserFromChannel(username);
        chat.removeUser(user);
    }

    /*
     * user|wiadomosc - wiadomosc od usera na danym kanale
     * name|wiadomosc - nazwa uzytkownika
     * addChannel|"nazwa kanalu" - dodanie kanalu
     * channelEnter|"kanal 1"- dodanie siebie na kanal
     * channelExit| - wyjscie z kanalu
     */
    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        String prefix = message.substring( 0, 3 );
        String postfix = message.substring( 3,message.length() );
        switch( prefix ){
            case "msg":
                String username = chat.getUsernames().get(user);
                if(!chat.getUserToChannel().containsKey(username))
                    chat.sendMessageToUser(user, "First you should join to channel!");
                else{
                    String channel = chat.getUserToChannel().get(username);
                    chat.broadcastMessage(username, postfix, channel);

                    if(channel.equals("chatbot"))
                        chat.askChatbot(postfix);
                }
                break;
            case "usr":
                if(chat.addUsername(user, postfix))
                    chat.refreshForUser(user);
                break;
            case "add":
                chat.addChannel(postfix);
                chat.refresh();
                break;
            case "ent":
                chat.addUserToChannel(chat.getUsernames().get(user), postfix);
                break;
            case "ext":
                if(!chat.getUserToChannel().containsKey( chat.getUsernames().get(user) )){
                    chat.sendMessageToUser(user, "Nie jestes na zadnym kanale !");
                }
                else{
                    chat.removeUserFromChannel(chat.getUsernames().get(user));
                }
                break;

        }
    }

}
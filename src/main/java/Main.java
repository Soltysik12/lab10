public class Main {

    private static Chat chat;

    public static void main(String[] args){
        chat= new Chat();
        chat.initialize();
    }

    public static Chat getChatInstance(){
        return chat;
    }
}
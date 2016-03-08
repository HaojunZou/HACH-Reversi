//package Client;
//
//import java.net.Socket;
//
//public class ObserverPool {
//    private Socket socket;
//    private MessageBoy messageBoy;
//    private JSONPost jsonPost = new JSONPost();
//
//    public ObserverPool(Socket s){
//        this.socket = s;
//        this.messageBoy = new MessageBoy("");
//        Object message = jsonPost.getString(socket);
//        ScorePanel scorePanel = new ScorePanel();
//        DialogPanel dialogPanel = new DialogPanel();
//        GamePanel gamePanel = new GamePanel();
//        messageBoy.addObserver(scorePanel);
//        messageBoy.addObserver(dialogPanel);
//        messageBoy.addObserver(gamePanel);
//        messageBoy.setMessage(message.toString());
//    }
//
////    public static void main(String [] args){
////        new ObserverPool(socket);
////    }
//}

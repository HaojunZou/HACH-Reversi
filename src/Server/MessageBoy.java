package Server;

import java.net.Socket;
import java.util.LinkedList;

public interface MessageBoy {
    public void getMessage();
    public void sendMessage(Player player, String key, Object value);
    public void sendAllMessage(LinkedList<Player> players, String key, Object value);
}

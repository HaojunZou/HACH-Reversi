package Server;

import java.util.LinkedList;

public interface MessageBoy {
    public void getMessage();
    public void sendMessage(Player player, String key, Object value);   //send message to current player
    public void sendAllMessage(LinkedList<Player> players, String key, Object value);   //send message to both player
}

package Server;

import java.net.Socket;

public interface MessageBoy {
    public void getMessage();
    public void sendMessage(Socket socket, String key, Object value, String who);
}

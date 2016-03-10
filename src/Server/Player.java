package Server;

import java.net.Socket;

public class Player {
    private Socket socket;
    private int color;

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Socket getSocket() {
        return socket;
    }

    public int getColor() {
        return color;
    }
}

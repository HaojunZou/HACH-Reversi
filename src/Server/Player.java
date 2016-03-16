package Server;

import java.net.Socket;

public class Player {
    private Socket socket;
    private int color;
    private boolean inGame = false;

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    public Socket getSocket() {
        return socket;
    }

    public int getColor() {
        return color;
    }

    public boolean isInGame() {
        return inGame;
    }
}

package Server;

import java.net.Socket;

public class Player {
    private Socket socket;
    private int color;
    private boolean inGame = false;
    private int tableID;
    private int algorithmID;

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    public void setTableID(int id)  {
        this.tableID = id;
    }

    public void setAlgorithmID(int id)  {
        this.algorithmID = id;
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

    public int getTableID() {
        return tableID;
    }

    public int getAlgorithmID() {
        return algorithmID;
    }
}

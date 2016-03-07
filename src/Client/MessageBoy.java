package Client;

import java.util.Observable;

public class MessageBoy extends Observable {
    private String message;


    public MessageBoy(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        super.setChanged();
        this.message = message;
        super.notifyObservers(message);
    }
}

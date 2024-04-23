package codes.tyr.longshiplink.pubnub;

public class ChatMessage {
    private  String sender;
    private  String message;
    private String serverID;

    public ChatMessage(String sender, String message, String serverID) {
        this.sender = sender;
        this.message = message;
        this.serverID = serverID;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getServerID() {
        return serverID;
    }
    public void setServerID(String serverID) {
        this.serverID = serverID;
    }
    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
}

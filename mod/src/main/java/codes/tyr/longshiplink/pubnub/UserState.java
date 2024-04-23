package codes.tyr.longshiplink.pubnub;

public class UserState {
    private final String username;
    private String serverID;

    public UserState(String username, String serverID) {
        this.username = username;
        this.serverID = serverID;
    }

    public String getUsername() {
        return username;
    }

}

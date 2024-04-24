package codes.tyr.longshiplink.pubnub;

public class OnlineState {
    private boolean online;
    private UserState userState;

    public OnlineState(boolean online, UserState userState) {
        this.online = online;
        this.userState = userState;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}

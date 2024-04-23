package codes.tyr.longshiplink.auth;

import com.google.gson.annotations.SerializedName;

public class UserAuthResponse {
    @SerializedName("mid")
    private String mid;
    @SerializedName("sub_key")
    private String subKey;
    @SerializedName("pub_key")
    private String pubKey;
    @SerializedName("server_id")
    private String serverID;
    @SerializedName("token")
    private String token;

    public UserAuthResponse(String mid, String subKey, String pubKey, String serverID, String token) {
        this.mid = mid;
        this.subKey = subKey;
        this.pubKey = pubKey;
        this.serverID = serverID;
        this.token = token;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getSubKey() {
        return subKey;
    }

    public void setSubKey(String subKey) {
        this.subKey = subKey;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

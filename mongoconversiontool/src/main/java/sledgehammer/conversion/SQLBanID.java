package sledgehammer.conversion;

public class SQLBanID {

    private String id;
    private String username;
    private String reason;

    public SQLBanID(String id, String username, String reason) {
        setID(id);
        setUsername(username);
        setReason(reason);
    }

    @Override
    public String toString() {
        return "(SteamID=\"" + getID() + "\" username=\"" + getUsername() + "\" reason=\"" + getReason() + "\")";
    }

    public String getID() {
        return this.id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

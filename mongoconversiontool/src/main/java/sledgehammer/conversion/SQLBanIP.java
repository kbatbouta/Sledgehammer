package sledgehammer.conversion;

public class SQLBanIP {

    private String ip;
    private String username;
    private String reason;

    public SQLBanIP(String ip, String username, String reason) {
        setIP(ip);
        setUsername(username);
        setReason(reason);
    }

    @Override
    public String toString() {
        return "(IP=\"" + getIP() + "\" username=\"" + getUsername() + "\" reason=\"" + getReason() + "\")";
    }

    public String getIP() {
        return this.ip;
    }

    public void setIP(String ip) {
        this.ip = ip;
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
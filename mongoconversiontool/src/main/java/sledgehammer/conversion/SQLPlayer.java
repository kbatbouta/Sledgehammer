package sledgehammer.conversion;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

class SQLPlayer {

    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String passwordEncrypted;
    private String username;
    private boolean administrator = false;
    private boolean banned;
    private String steamId;
    private String ownerId;
    private Long lastConnection;

    public SQLPlayer(String username, String password, boolean encrypted) {
        setUsername(username);
        String passwordEncrypted;
        if (encrypted) {
            passwordEncrypted = password;
        } else {
            passwordEncrypted = encrypt(password);
        }
        setEncryptedPassword(passwordEncrypted);
    }

    public boolean isAdministrator() {
        return this.administrator;
    }

    public void setAdministrator(boolean administrator) {
        this.administrator = administrator;
    }

    public String getEncryptedPassword() {
        return this.passwordEncrypted;
    }

    public void setEncryptedPassword(String passwordEncrypted) {
        this.passwordEncrypted = passwordEncrypted;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isBanned() {
        return this.banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public String getSteamId() {
        return this.steamId;
    }

    public void setSteamId(String steamId) {
        this.steamId = steamId;
    }

    public String getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Long getLastConnection() {
        return this.lastConnection;
    }

    public void setLastConnection(String lastConnection) {
        if(lastConnection != null) {
            try {
                this.lastConnection = dateFormat.parse(lastConnection).getTime();
            } catch (ParseException e) {
            }
        }
    }

    public static String encrypt(String previousPwd) {
        if (previousPwd == null || previousPwd.isEmpty()) {
            return "";
        } else {
            byte[] encrypted = null;
            try {
                encrypted = MessageDigest.getInstance("MD5").digest(previousPwd.getBytes());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            StringBuilder hashString = new StringBuilder();
            if (encrypted != null) {
                for (byte crypt : encrypted) {
                    String hex = Integer.toHexString(crypt);
                    if (hex.length() == 1) {
                        hashString.append('0');
                        hashString.append(hex.charAt(hex.length() - 1));
                    } else {
                        hashString.append(hex.substring(hex.length() - 2));
                    }
                }
            }
            return hashString.toString();
        }
    }
}
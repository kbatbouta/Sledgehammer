package sledgehammer.lua.core.send;

import sledgehammer.SledgeHammer;
import sledgehammer.lua.Send;
import sledgehammer.lua.core.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Send designed to handle and format Lua to send to the Client-side Sledgehammer engine.
 *
 * @author Jab
 */
public class SendLua extends Send {

    /**
     * If set to true, the lua code will be packaged line by line with preservation, so that the code can be debugged.
     */
    public static boolean debugLua = true;

    /**
     * The string of the lua code to send to the Player.
     */
    private String luaString;
    /**
     * The Player to associate with the SendLua instance. (Optional)
     */
    private Player player;

    /**
     * Main constructor.
     *
     * @param files The Lua Files to digest.
     */
    public SendLua(File... files) {
        super("core", "sendLua");
        appendLuaFiles(files);
    }

    /**
     * Player constructor.
     *
     * @param player The Player to set.
     */
    public SendLua(Player player) {
        super("core", "sendLua");
        setPlayer(player);
    }

    /**
     * Composite constructor.
     *
     * @param player The Player to set.
     * @param files  The Lua Files to digest.
     */
    public SendLua(Player player, File... files) {
        super("core", "sendLua");
        setPlayer(player);
        appendLuaFiles(files);
    }

    @Override
    public void onExport() {
        // @formatter:off
        set("lua"   , getLuaString());
        set("player", getPlayer()   );
        // @formatter:on
    }

    /**
     * Sets the raw Lua String to only the Files given here.
     * <p>
     * (Note: This does not append. To append, use <code>appendLuaFile(files);</code>)
     *
     * @param luaFiles The Lua Files to digest.
     */
    public void setLuaFiles(File... luaFiles) {
        this.luaString = packageLuaFiles(luaFiles);
    }

    /**
     * Appends LuaFiles by digesting them into a raw, compressed String format.
     *
     * @param luaFiles The Lua Files to digest.
     */
    public void appendLuaFiles(File... luaFiles) {
        String lua = packageLuaFiles(luaFiles);
        if (luaString == null || luaString.isEmpty()) {
            luaString = lua;
        } else {
            luaString += " " + lua;
        }
    }

    /**
     * Appends a SendLua's compiled LuaString to the SendLua instance.
     *
     * @param other The other SendLua to append.
     */
    public void append(SendLua other) {
        if (other == null) {
            throw new IllegalArgumentException("SendLua provided is null.");
        }
        String luaString = other.getLuaString();
        if (luaString == null || luaString.isEmpty()) {
            System.err.println("SendLua provided is empty and cannot be appended.");
            return;
        }
        if (this.luaString == null || this.luaString.isEmpty()) {
            this.luaString = luaString;
        } else {
            this.luaString += " " + luaString;
        }
    }

    /**
     * Resets the SendLua Object to have no Lua code.
     */
    public void reset() {
        this.luaString = "";
    }

    /**
     * Sends the Lua code to the Player set.
     */
    public void send() {
        Player player = getPlayer();
        if(player == null) {
            errln("Player is not set. Cannot send the SendLua Object.");
            return;
        }
        SledgeHammer.instance.send(this, player);
    }

    /**
     * @return Returns the raw Lua String of the LuaSend Object.
     */
    public String getLuaString() {
        return this.luaString;
    }

    /**
     * Sets the raw Lua String of the LuaSend Object.
     *
     * @param luaString The String to set.
     */
    public void setLuaString(String luaString) {
        this.luaString = luaString;
    }

    /**
     * @return Returns the Player Object set to pass and reference when building Lua Strings.
     * (Note: This is optional, and null can be returned. Make sure to set null checks)
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Sets a Player Object to pass and reference when building Lua Strings. (Optional).
     *
     * @param player The Player to set.
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * Packages Lua files into compressed strings without comments or spaces.
     * <p>
     * (Note: If 'SendLua.debugLua' is set to true, lines will be preserved)
     *
     * @param files The Array of Files to digest.
     * @return Returns the String equivalent of the Lua Files.
     */
    public static String packageLuaFiles(File... files) {
        StringBuilder sb = new StringBuilder();
        for (File file : files) {
            sb.append(" ");
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim().replace("\t", " ");
                    // Remove lines with only comments.
                    if (line.startsWith("--")) continue;
                    // If the line has a leading comment, remove it.
                    if (line.contains("--")) {
                        line = line.split("--")[0].trim();
                    }
                    // Append everything with spaces to separate lines without
                    // semi-colons.
                    sb.append(" ");
                    char[] chars = line.toCharArray();
                    boolean space = false;
                    for (char charCurrent : chars) {
                        if (!space) {
                            if (charCurrent == ' ') {
                                space = true;
                            }
                            sb.append(charCurrent);
                        } else if (charCurrent != ' ') {
                            space = false;
                            sb.append(charCurrent);
                        }
                    }
                    // If the lua needs to be debugged, then line numbers will be preserved.
                    if (debugLua) {
                        sb.append("\n");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // @formatter:off
            try {if(reader != null) reader.close(); } catch(Exception e) { e.printStackTrace();}
            // @formatter:on
        }
        return sb.toString().trim();
    }
}

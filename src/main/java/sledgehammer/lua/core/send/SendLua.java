package sledgehammer.lua.core.send;

import sledgehammer.lua.Send;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class SendLua extends Send {

    private String luaString;

    /**
     * Main constructor.
     */
    public SendLua(File... files) {
        super("core", "sendLua");
        setLuaFiles(files);
    }

    @Override
    public void onExport() {
        set("lua", getLuaString());
    }

    public void setLuaFiles(File...luaFiles) {
        this.luaString = packageLuaFiles(luaFiles);
    }

    public String getLuaString() {
        return this.luaString;
    }

    public void setLuaString(String luaString) {
        this.luaString = luaString;
    }

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
                    for(char charCurrent : chars) {
                        if(!space) {
                            if(charCurrent == ' ') {
                                space = true;
                            }
                            sb.append(charCurrent);
                        } else {
                            if (charCurrent != ' ') {
                                space = false;
                                sb.append(charCurrent);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // @formatter:off
            try {if(reader != null) reader.close(); } catch(Exception e) {}
            // @formatter:on
        }
        return sb.toString().trim();
    }
}

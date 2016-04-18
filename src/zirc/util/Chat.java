package zirc.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zirc.ZIRC;
import zombie.characters.IsoPlayer;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.core.raknet.UdpEngine;
import zombie.network.GameServer;
import zombie.network.PacketTypes;

public class Chat {
	public static HashMap<String, String> mapColors;

	public static final String CHAT_COLOR_WHITE        = " <RGB:1,1,1>"       ;
	public static final String CHAT_COLOR_LIGHT_GRAY   = " <RGB:0.7,0.7,0.7>" ;
	public static final String CHAT_COLOR_DARK_GRAY    = " <RGB:0.3,0.3,0.3>" ;	
	public static final String CHAT_COLOR_BLACK        = " <RGB:0,0,0>"       ;
	public static final String CHAT_COLOR_LIGHT_RED    = " <RGB:1,0.6,0.6>"   ;
	public static final String CHAT_COLOR_RED          = " <RGB:1,0.25,0.25>" ;
	public static final String CHAT_COLOR_DARK_RED     = " <RGB:0.6,0,0>"     ;
	public static final String CHAT_COLOR_BEIGE        = " <RGB:1,0.65,0.38>" ;
	public static final String CHAT_COLOR_ORANGE       = " <RGB:1,0.45,0.18>" ;
	public static final String CHAT_COLOR_BROWN        = " <RGB:0.6,0.2,0.1>" ;
	public static final String CHAT_COLOR_LIGHT_YELLOW = " <RGB:1,1,0.8>"     ;
	public static final String CHAT_COLOR_YELLOW       = " <RGB:1,1,0.25>"    ;
	public static final String CHAT_COLOR_DARK_YELLOW  = " <RGB:0.6,0.6,0>"   ;
	public static final String CHAT_COLOR_LIGHT_GREEN  = " <RGB:0.6,1,0.6>"   ;
	public static final String CHAT_COLOR_GREEN        = " <RGB:0.25,1,0.25>" ;
	public static final String CHAT_COLOR_DARK_GREEN   = " <RGB:0,0.6,0>"     ;
	public static final String CHAT_COLOR_LIGHT_BLUE   = " <RGB:0.6,1,1>"     ;
	public static final String CHAT_COLOR_BLUE         = " <RGB:0.25,1,1>"    ;
	public static final String CHAT_COLOR_DARK_BLUE    = " <RGB:0.25,0.25,1>" ;
	public static final String CHAT_COLOR_INDIGO       = " <RGB:0.5,0.5,1>"   ;
	public static final String CHAT_COLOR_LIGHT_PURPLE = " <RGB:1,0.6,1>"     ;
	public static final String CHAT_COLOR_PURPLE       = " <RGB:1,0.25,1>"    ;
	public static final String CHAT_COLOR_DARK_PURPLE  = " <RGB:0.6,0,0.6>"   ;
	public static final String CHAT_COLOR_PINK         = " <RGB:1,0.45,1>"    ;
	public static final String CHAT_LINE               = " <LINE>"            ;
	
	Map<String, Long> mapPlayerTimeStamps;
	
	static {
		mapColors = new HashMap<>();
		mapColors.put("white"       , CHAT_COLOR_WHITE       );
		mapColors.put("light-gray"  , CHAT_COLOR_LIGHT_GRAY  );
		mapColors.put("dark-gray"   , CHAT_COLOR_DARK_GRAY   );
		mapColors.put("black"       , CHAT_COLOR_BLACK       );
		mapColors.put("light-red"   , CHAT_COLOR_LIGHT_RED   );
		mapColors.put("red"         , CHAT_COLOR_RED         );
		mapColors.put("dark-red"    , CHAT_COLOR_DARK_RED    );
		mapColors.put("beige"       , CHAT_COLOR_BEIGE       );
		mapColors.put("orange"      , CHAT_COLOR_ORANGE      );
		mapColors.put("brown"       , CHAT_COLOR_BROWN       );
		mapColors.put("light-yellow", CHAT_COLOR_LIGHT_YELLOW);
		mapColors.put("yellow"      , CHAT_COLOR_YELLOW      );
		mapColors.put("dark-yellow" , CHAT_COLOR_DARK_YELLOW );
		mapColors.put("light-green" , CHAT_COLOR_LIGHT_GREEN );
		mapColors.put("green"       , CHAT_COLOR_GREEN       );
		mapColors.put("dark-green"  , CHAT_COLOR_DARK_GREEN  );
		mapColors.put("indigo"      , CHAT_COLOR_INDIGO      );
		mapColors.put("light-blue"  , CHAT_COLOR_LIGHT_BLUE  );
		mapColors.put("blue"        , CHAT_COLOR_BLUE        );
		mapColors.put("dark-blue"   , CHAT_COLOR_DARK_BLUE   );
		mapColors.put("light-purple", CHAT_COLOR_LIGHT_PURPLE);
		mapColors.put("purple"      , CHAT_COLOR_PURPLE      );
		mapColors.put("dark-purple" , CHAT_COLOR_DARK_PURPLE );
		mapColors.put("pink"        , CHAT_COLOR_PINK        );
	}
	
	public static String listColors() {
		String str = "Colors:" + CHAT_LINE + " " ;
		str += listColor("white"       );
		str += listColor("light-gray"  );
		str += listColor("black"       );
		str += listColor("dark-gray"   );
		str += listColor("light-red"   );
		str += listColor("red"         );
		str += listColor("dark-red"    );
		str += listColor("beige"       );
		str += listColor("orange"      );
		str += listColor("brown"       );
		str += listColor("light-yellow");
		str += listColor("yellow"      );
		str += listColor("dark-yellow" );
		str += listColor("light-green" );
		str += listColor("green"       );
		str += listColor("dark-green"  );
		str += listColor("indigo"      );
		str += listColor("light-blue"  );
		str += listColor("blue"        );
		str += listColor("dark-blue"   );
		str += listColor("light-purple");
		str += listColor("purple"      );
		str += listColor("dark-purple" );
		str += listColor("pink"        );
		return str + CHAT_LINE + " ";
	}
	
	private static String listColor(String color) {
		return mapColors.get(color) + " [" + color + "] " + CHAT_COLOR_WHITE + " ";
	}
	
	public static String getColor(String color) {
		return mapColors.get(color.toLowerCase());
	}
	
	public static String getStripped(String text, boolean newLine) {
		if(text == null) return null;
		String stripped = "";
		char[] textArray = text.toCharArray();
		
		boolean inCode = false;
		
		for(int index = 0; index < text.length(); index++) {
			char c = textArray[index];
			
			if(inCode) {
				if(c == '>') {
					inCode = false;
					index += 1;
					continue;
				} else {
					continue;
				}
			} else {
				if(c == '<') {
					if(index + 4 <= text.length() - 1) {
						char cn1 = '\u9999';
						if(index-1 >= 0) cn1 = textArray[index - 1];
						char c1 = textArray[index + 1];
						char c2 = textArray[index + 2];
						char c3 = textArray[index + 3];
						char c4 = textArray[index + 4];
						if(c1 == 'r' || c1 == 'R') {
							if(c2 == 'g' || c2 == 'G') {
								if(c3 == 'b' || c3 == 'B') {
									if(c4 == ':') {
										inCode = true;
										if(cn1 == ' ') {
											stripped = stripped.substring(0, stripped.length() - 1);
										}
										continue;
									}
								}
							}
						} else
						if(c1 == 'L' || c1 == 'l') {
							if(c2 == 'I' || c2 == 'i') {
								if(c3 == 'N' || c3 == 'n') {
									if(c4 == 'E' || c4 == 'e') {
										inCode = true;
										if(cn1 == ' ') {
											stripped = stripped.substring(0, stripped.length() - 1) + (newLine ? "\n":"");
										}
									}
								}
							}
						}
					}
				} else {
					stripped += c;
				}
			}
		}
		return stripped;
	}

	private List<String> listGlobalMuters;
	private UdpEngine udpEngine;
	
	public Chat(UdpEngine udpEngine) {
		this.udpEngine = udpEngine;
		this.mapPlayerTimeStamps = new HashMap<>();
		this.listGlobalMuters = new ArrayList<>();
	}
	
	public List<String> getGlobalMuters() {
		return this.listGlobalMuters;
	}
	
	public String messagePlayer(String username, String header, String headerColor, String text, String textColor, boolean addTimeStamp, boolean bypassMute) {
		try {
			IsoPlayer player = GameServer.getPlayerByUserName(username);
			if(player != null) {
				return messagePlayer(GameServer.getPlayerByUserName(username), header, headerColor, text, textColor, addTimeStamp, bypassMute);			
			} else {
				return "Player not found: " + username + ".";
			}
		} catch(Exception e) {
			
		}
		return null;
	}
	
	public String messagePlayer(IsoPlayer player, String header, String headerColor, String text, String textColor, boolean addTimeStamp, boolean bypassMute) {
		UdpConnection connection = GameServer.getConnectionFromPlayer(player);
		if(connection != null) {
			return messagePlayer(GameServer.getConnectionFromPlayer(player), header, headerColor, text, textColor, addTimeStamp, bypassMute);			
		} else {
			return "Connection does not exist.";
		}
	}
	
	public String privateMessage(String commander, String username, String text) {
		return messagePlayer(username, "[PM][" + commander + "]: ", CHAT_COLOR_LIGHT_GREEN, text, CHAT_COLOR_LIGHT_GREEN, true, true);
	}
	
	public String warnPlayer(String commander, String username, String text) {
		return messagePlayer(username, "[WARNING]["+ commander + "]: ", CHAT_COLOR_LIGHT_RED, text, CHAT_COLOR_LIGHT_RED, true, true);
	}
	
	public String privateMessage(String commander, UdpConnection connection, String text) {
		return messagePlayer(connection, "[PM][" + commander + "]: ", CHAT_COLOR_LIGHT_GREEN, text, CHAT_COLOR_LIGHT_GREEN, true, true);
	}
	
	public void localMessage(UdpConnection connection, int playerID, String text, byte chatType, byte sayIt) {
		ByteBufferWriter bufferWriter = connection.startPacket();
		PacketTypes.doPacket((byte) 38, bufferWriter);
		bufferWriter.putInt(playerID);
		bufferWriter.putByte(chatType);
		bufferWriter.putUTF(text);
		bufferWriter.putByte(sayIt);
		connection.endPacketImmediate();
	}

	public String messagePlayer(UdpConnection connection, String header, String headerColor, String text, String textColor,  boolean addTimeStamp, boolean bypassMute) {
		
		if(!bypassMute && listGlobalMuters.contains(connection.username.toLowerCase())) return "User muted their global chat.";
		
		String message = "";
		if(addTimeStamp) message += "[T]";

		if(header != null && !header.isEmpty()) message += headerColor + " " + header;
		
		if(textColor != null && !textColor.isEmpty()) message += textColor + " ";
		
		message += text + CHAT_COLOR_WHITE + " ";
		
		ByteBufferWriter b2 = connection.startPacket();
		PacketTypes.doPacket((byte) 81, b2);
		b2.putUTF(message);
		connection.endPacketImmediate();
		
		return "Message sent.";
	}

	public void globalMessage(String message) {
		if(udpEngine == null) {
			ZIRC.println("Chat.globalMessage(): UDPEngine is Null.");
			return;
		}
		for (UdpConnection connection : udpEngine.connections) {
			messagePlayer(connection, null, null, message, null, true, false);
		}

	}
	
	public void globalMessage(String header, String message) {
		if(udpEngine == null) return;
		for (UdpConnection connection : udpEngine.connections) {
			messagePlayer(connection, header, CHAT_COLOR_WHITE, message, CHAT_COLOR_WHITE, true, false);
		}
	}
	
	public void globalMessage(String header, String headerColor, String message, String messageColor) {
		if(udpEngine == null) return;
		for (UdpConnection connection : udpEngine.connections) {
			messagePlayer(connection, header, headerColor, message, messageColor, true, false);
		}
	}
	
	public void globalMessage(String header, String headerColor, String message, String messageColor, boolean timeStamp) {
		if(udpEngine == null) return;
		for (UdpConnection connection : udpEngine.connections) {
			messagePlayer(connection, header, headerColor, message, messageColor, timeStamp, false);
		}
	}
	
	public void broadcastChat(String message, String messageColor) {
		if(messageColor == null || messageColor.isEmpty()) messageColor = CHAT_COLOR_LIGHT_RED;
		for (UdpConnection connection : udpEngine.connections) {
			String messageOut = "[B]" + messageColor + " " + message;
			ByteBufferWriter bufferWriter = connection.startPacket();
			PacketTypes.doPacket((byte) 81, bufferWriter);
			bufferWriter.putUTF(messageOut);
			connection.endPacketImmediate();
		}
	}
}

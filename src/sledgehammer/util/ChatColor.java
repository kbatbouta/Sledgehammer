package sledgehammer.util;

import java.util.HashMap;

public class ChatColor {
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
}

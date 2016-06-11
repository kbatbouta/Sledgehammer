package sledgehammer.util;

import java.util.HashMap;

public class ChatColor {
public static HashMap<String, String> mapColors;
	
	public static final String COLOR_WHITE        = " <RGB:1,1,1>"       ;
	public static final String COLOR_LIGHT_GRAY   = " <RGB:0.7,0.7,0.7>" ;
	public static final String COLOR_DARK_GRAY    = " <RGB:0.3,0.3,0.3>" ;	
	public static final String COLOR_BLACK        = " <RGB:0,0,0>"       ;
	public static final String COLOR_LIGHT_RED    = " <RGB:1,0.6,0.6>"   ;
	public static final String COLOR_RED          = " <RGB:1,0.25,0.25>" ;
	public static final String COLOR_DARK_RED     = " <RGB:0.6,0,0>"     ;
	public static final String COLOR_BEIGE        = " <RGB:1,0.65,0.38>" ;
	public static final String COLOR_ORANGE       = " <RGB:1,0.45,0.18>" ;
	public static final String COLOR_BROWN        = " <RGB:0.6,0.2,0.1>" ;
	public static final String COLOR_LIGHT_YELLOW = " <RGB:1,1,0.8>"     ;
	public static final String COLOR_YELLOW       = " <RGB:1,1,0.25>"    ;
	public static final String COLOR_DARK_YELLOW  = " <RGB:0.6,0.6,0>"   ;
	public static final String COLOR_LIGHT_GREEN  = " <RGB:0.6,1,0.6>"   ;
	public static final String COLOR_GREEN        = " <RGB:0.25,1,0.25>" ;
	public static final String COLOR_DARK_GREEN   = " <RGB:0,0.6,0>"     ;
	public static final String COLOR_LIGHT_BLUE   = " <RGB:0.6,1,1>"     ;
	public static final String COLOR_BLUE         = " <RGB:0.25,1,1>"    ;
	public static final String COLOR_DARK_BLUE    = " <RGB:0.25,0.25,1>" ;
	public static final String COLOR_INDIGO       = " <RGB:0.5,0.5,1>"   ;
	public static final String COLOR_LIGHT_PURPLE = " <RGB:1,0.6,1>"     ;
	public static final String COLOR_PURPLE       = " <RGB:1,0.25,1>"    ;
	public static final String COLOR_DARK_PURPLE  = " <RGB:0.6,0,0.6>"   ;
	public static final String COLOR_PINK         = " <RGB:1,0.45,1>"    ;
	public static final String NEW_LINE           = " <LINE>"            ;
	
	static {
		mapColors = new HashMap<>();
		mapColors.put("white"       , COLOR_WHITE       );
		mapColors.put("light-gray"  , COLOR_LIGHT_GRAY  );
		mapColors.put("dark-gray"   , COLOR_DARK_GRAY   );
		mapColors.put("black"       , COLOR_BLACK       );
		mapColors.put("light-red"   , COLOR_LIGHT_RED   );
		mapColors.put("red"         , COLOR_RED         );
		mapColors.put("dark-red"    , COLOR_DARK_RED    );
		mapColors.put("beige"       , COLOR_BEIGE       );
		mapColors.put("orange"      , COLOR_ORANGE      );
		mapColors.put("brown"       , COLOR_BROWN       );
		mapColors.put("light-yellow", COLOR_LIGHT_YELLOW);
		mapColors.put("yellow"      , COLOR_YELLOW      );
		mapColors.put("dark-yellow" , COLOR_DARK_YELLOW );
		mapColors.put("light-green" , COLOR_LIGHT_GREEN );
		mapColors.put("green"       , COLOR_GREEN       );
		mapColors.put("dark-green"  , COLOR_DARK_GREEN  );
		mapColors.put("indigo"      , COLOR_INDIGO      );
		mapColors.put("light-blue"  , COLOR_LIGHT_BLUE  );
		mapColors.put("blue"        , COLOR_BLUE        );
		mapColors.put("dark-blue"   , COLOR_DARK_BLUE   );
		mapColors.put("light-purple", COLOR_LIGHT_PURPLE);
		mapColors.put("purple"      , COLOR_PURPLE      );
		mapColors.put("dark-purple" , COLOR_DARK_PURPLE );
		mapColors.put("pink"        , COLOR_PINK        );
	}
	
	public static String listColors() {
		String str = "Colors:" + NEW_LINE + " " ;
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
		return str + NEW_LINE + " ";
	}
	
	private static String listColor(String color) {
		return mapColors.get(color) + " [" + color + "] " + COLOR_WHITE + " ";
	}
	
	public static String getColor(String color) {
		return mapColors.get(color.toLowerCase());
	}
}

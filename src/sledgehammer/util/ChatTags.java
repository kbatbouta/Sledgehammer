/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 */
package sledgehammer.util;

import java.util.HashMap;

/**
 * TODO: Document.
 * 
 * @author Jab
 */
public class ChatTags {

	public static HashMap<String, String> mapColors;

	public static final String COLOR_WHITE = " <RGB:1,1,1>";
	public static final String COLOR_LIGHT_GRAY = " <RGB:0.7,0.7,0.7>";
	public static final String COLOR_DARK_GRAY = " <RGB:0.3,0.3,0.3>";
	public static final String COLOR_BLACK = " <RGB:0,0,0>";
	public static final String COLOR_LIGHT_RED = " <RGB:1,0.6,0.6>";
	public static final String COLOR_RED = " <RGB:1,0.25,0.25>";
	public static final String COLOR_DARK_RED = " <RGB:0.6,0,0>";
	public static final String COLOR_BEIGE = " <RGB:1,0.65,0.38>";
	public static final String COLOR_ORANGE = " <RGB:1,0.45,0.18>";
	public static final String COLOR_BROWN = " <RGB:0.6,0.2,0.1>";
	public static final String COLOR_LIGHT_YELLOW = " <RGB:1,1,0.8>";
	public static final String COLOR_YELLOW = " <RGB:1,1,0.25>";
	public static final String COLOR_DARK_YELLOW = " <RGB:0.6,0.6,0>";
	public static final String COLOR_LIGHT_GREEN = " <RGB:0.6,1,0.6>";
	public static final String COLOR_GREEN = " <RGB:0.25,1,0.25>";
	public static final String COLOR_DARK_GREEN = " <RGB:0,0.6,0>";
	public static final String COLOR_LIGHT_BLUE = " <RGB:0.6,1,1>";
	public static final String COLOR_BLUE = " <RGB:0.25,1,1>";
	public static final String COLOR_DARK_BLUE = " <RGB:0.25,0.25,1>";
	public static final String COLOR_INDIGO = " <RGB:0.5,0.5,1>";
	public static final String COLOR_LIGHT_PURPLE = " <RGB:1,0.6,1>";
	public static final String COLOR_PURPLE = " <RGB:1,0.25,1>";
	public static final String COLOR_DARK_PURPLE = " <RGB:0.6,0,0.6>";
	public static final String COLOR_PINK = " <RGB:1,0.45,1>";
	public static final String NEW_LINE = " <LINE>";

	static {
		mapColors = new HashMap<>();
		mapColors.put("white", COLOR_WHITE);
		mapColors.put("light-gray", COLOR_LIGHT_GRAY);
		mapColors.put("dark-gray", COLOR_DARK_GRAY);
		mapColors.put("black", COLOR_BLACK);
		mapColors.put("light-red", COLOR_LIGHT_RED);
		mapColors.put("red", COLOR_RED);
		mapColors.put("dark-red", COLOR_DARK_RED);
		mapColors.put("beige", COLOR_BEIGE);
		mapColors.put("orange", COLOR_ORANGE);
		mapColors.put("brown", COLOR_BROWN);
		mapColors.put("light-yellow", COLOR_LIGHT_YELLOW);
		mapColors.put("yellow", COLOR_YELLOW);
		mapColors.put("dark-yellow", COLOR_DARK_YELLOW);
		mapColors.put("light-green", COLOR_LIGHT_GREEN);
		mapColors.put("green", COLOR_GREEN);
		mapColors.put("dark-green", COLOR_DARK_GREEN);
		mapColors.put("indigo", COLOR_INDIGO);
		mapColors.put("light-blue", COLOR_LIGHT_BLUE);
		mapColors.put("blue", COLOR_BLUE);
		mapColors.put("dark-blue", COLOR_DARK_BLUE);
		mapColors.put("light-purple", COLOR_LIGHT_PURPLE);
		mapColors.put("purple", COLOR_PURPLE);
		mapColors.put("dark-purple", COLOR_DARK_PURPLE);
		mapColors.put("pink", COLOR_PINK);
	}

	public static String listColors() {
		String str = "Colors:" + NEW_LINE + " ";
		str += listColor("white");
		str += listColor("light-gray");
		str += listColor("black");
		str += listColor("dark-gray");
		str += listColor("light-red");
		str += listColor("red");
		str += listColor("dark-red");
		str += listColor("beige");
		str += listColor("orange");
		str += listColor("brown");
		str += listColor("light-yellow");
		str += listColor("yellow");
		str += listColor("dark-yellow");
		str += listColor("light-green");
		str += listColor("green");
		str += listColor("dark-green");
		str += listColor("indigo");
		str += listColor("light-blue");
		str += listColor("blue");
		str += listColor("dark-blue");
		str += listColor("light-purple");
		str += listColor("purple");
		str += listColor("dark-purple");
		str += listColor("pink");
		return str + NEW_LINE + " ";
	}

	private static String listColor(String color) {
		return mapColors.get(color) + " [" + color + "] " + COLOR_WHITE + " ";
	}

	public static String getColor(String color) {
		return mapColors.get(color.toLowerCase());
	}

	public static String stripTags(String text, boolean newLine) {
		if (text == null) {
			return null;
		}
		String stripped = "";
		char[] textArray = text.toCharArray();
		boolean inCode = false;
		for (int index = 0; index < text.length(); index++) {
			char c = textArray[index];
			if (inCode) {
				if (c == '>') {
					inCode = false;
					index++;
					continue;
				} else
					continue;
			} else {
				if (c == '<') {
					if (index + 4 <= text.length() - 1) {
						char cn1 = (index - 1 >= 0) ? textArray[index - 1] : '\u9999';
						char c1 = textArray[index + 1];
						char c2 = textArray[index + 2];
						char c3 = textArray[index + 3];
						char c4 = textArray[index + 4];
						if ((c1 == 'R' || c1 == 'r') && (c2 == 'G' || c2 == 'g') && (c3 == 'B' || c3 == 'b')
								&& c4 == ':') {
							inCode = true;
							if (cn1 == ' ')
								stripped = stripped.substring(0, stripped.length() - 1);
							continue;
						} else if ((c1 == 'L' || c1 == 'l') && (c2 == 'I' || c2 == 'i') && (c3 == 'N' || c3 == 'n')
								&& (c4 == 'E' || c4 == 'e')) {
							inCode = true;
							if (cn1 == ' ')
								stripped = stripped.substring(0, stripped.length() - 1) + (newLine ? "\n" : "");
						}
					}
				} else
					stripped += c;
			}
		}
		return stripped;
	}

	public static boolean isValidColor(String colorNew) {
		return mapColors.containsKey(colorNew.toLowerCase());
	}

	public static boolean isLightColor(String color) {
		String test = color.toLowerCase();
		return test.contains("white") || test.contains("white");
	}

	public static boolean isDarkColor(String color) {
		String test = color.toLowerCase();
		return test.contains("dark") || test.contains("black") || test.contains("brown");
	}
}
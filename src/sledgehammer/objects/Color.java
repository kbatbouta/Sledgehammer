package sledgehammer.objects;

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

import java.util.HashMap;
import java.util.Map;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.lua.LuaTable;

/**
 * Color Lua class for SledgeHammer Lua.
 * @author Jab
 *
 */
public class Color extends LuaTable implements Comparable<Color> {

	/**
	 * Name of the LuaObject.
	 */
	public static final String NAME = "Color";

	public static final Color WHITE        = new Color(1.0f,1.0f,1.0f);
	public static final Color LIGHT_GRAY   = new Color(0.7f,0.7f,0.7f);
	public static final Color DARK_GRAY    = new Color(0.3f,0.3f,0.3f);	
	public static final Color BLACK        = new Color(0.0f,0.0f,0.0f);
	public static final Color LIGHT_RED    = new Color(1.0f,0.6f,0.6f);
	public static final Color RED          = new Color(1.0f,0.25f,0.25f);
	public static final Color DARK_RED     = new Color(0.6f,0.0f,0.0f);
	public static final Color BEIGE        = new Color(1.0f,0.65f,0.38f);
	public static final Color ORANGE       = new Color(1.0f,0.45f,0.18f);
	public static final Color BROWN        = new Color(0.6f,0.2f,0.1f);
	public static final Color LIGHT_YELLOW = new Color(1.0f,1.0f,0.8f);
	public static final Color YELLOW       = new Color(1.0f,1.0f,0.25f);
	public static final Color DARK_YELLOW  = new Color(0.6f,0.6f,0.0f);
	public static final Color LIGHT_GREEN  = new Color(0.6f,1.0f,0.6f);
	public static final Color GREEN        = new Color(0.25f,1.0f,0.25f);
	public static final Color DARK_GREEN   = new Color(0.0f,0.6f,0.0f);
	public static final Color LIGHT_BLUE   = new Color(0.6f,1.0f,1.0f);
	public static final Color BLUE         = new Color(0.25f,1.0f,1.0f);
	public static final Color DARK_BLUE    = new Color(0.25f,0.25f,1.0f);
	public static final Color INDIGO       = new Color(0.5f,0.5f,1f);
	public static final Color LIGHT_PURPLE = new Color(1.0f,0.6f,1.0f);
	public static final Color PURPLE       = new Color(1.0f,0.25f,1.0f);
	public static final Color DARK_PURPLE  = new Color(0.6f,0.0f,0.6f);
	public static final Color PINK         = new Color(1.0f,0.45f,1.0f);
	
	public static Map<String, Color> mapColors;
	static {
		mapColors = new HashMap<>();
		mapColors.put("white"       , WHITE       );
		mapColors.put("light-gray"  , LIGHT_GRAY  );
		mapColors.put("dark-gray"   , DARK_GRAY   );
		mapColors.put("black"       , BLACK       );
		mapColors.put("light-red"   , LIGHT_RED   );
		mapColors.put("red"         , RED         );
		mapColors.put("dark-red"    , DARK_RED    );
		mapColors.put("beige"       , BEIGE       );
		mapColors.put("orange"      , ORANGE      );
		mapColors.put("brown"       , BROWN       );
		mapColors.put("light-yellow", LIGHT_YELLOW);
		mapColors.put("yellow"      , YELLOW      );
		mapColors.put("dark-yellow" , DARK_YELLOW );
		mapColors.put("light-green" , LIGHT_GREEN );
		mapColors.put("green"       , GREEN       );
		mapColors.put("dark-green"  , DARK_GREEN  );
		mapColors.put("indigo"      , INDIGO      );
		mapColors.put("light-blue"  , LIGHT_BLUE  );
		mapColors.put("blue"        , BLUE        );
		mapColors.put("dark-blue"   , DARK_BLUE   );
		mapColors.put("light-purple", LIGHT_PURPLE);
		mapColors.put("purple"      , PURPLE      );
		mapColors.put("dark-purple" , DARK_PURPLE );
		mapColors.put("pink"        , PINK        );
	}
	
	public static Color getColor(String color) {
		return mapColors.get(color.toLowerCase());
	}
	
	/**
	 * Red (0.0-1.0 Float) (Default is 0.0F)
	 */
	private float red = 0.0F;
	
	/**
	 * Green (0.0-1.0 Float) (Default is 0.0F)
	 */
	private float green = 0.0F;
	
	/**
	 * Blue (0.0-1.0 Float) (Default is 0.0F)
	 */
	private float blue = 0.0F;
	
	/**
	 * Alpha (0.0-1.0 Float) (Default is 1.0F)
	 */
	private float alpha = 1.0F;
	
	/**
	 * Main Constructor
	 */
	public Color() {
		super(NAME);
	}
	
	public Color(float r, float g, float b, float a) {
		super(NAME);
		setRed(r);
		setGreen(g);
		setBlue(b);
		setAlpha(a);
	}
	
	public Color(float r, float g, float b) {
		super(NAME);
		setRed(r);
		setGreen(g);
		setBlue(b);
	}
	
	public Color(KahluaTable table) {
		super(NAME, table);
	}

	/**
	 * Sets all color values, and the Alpha value.
	 * @param red
	 * @param green
	 * @param blue
	 * @param alpha
	 */
	public void set(float red, float green, float blue, float alpha) {
		
		// Set all color values.
		setRed(red);
		setGreen(green);
		setBlue(blue);
		setAlpha(alpha);
	}
	
	/**
	 * Sets all color values.
	 * @param red
	 * @param green
	 * @param blue
	 */
	public void set(float red, float green, float blue) {
		
		// Set all color values.
		setRed(red);
		setGreen(green);
		setBlue(blue);
	}
	
	/**
	 * Returns the color Red. (0.0F - 1.0F)
	 * @return
	 */
	public float getRed() {
		return this.red;
	}
	
	/**
	 * Sets the color Red. (0.0F - 1.0F)
	 * @param red
	 */
	public void setRed(float red) {
		float limit = limit(red);
		this.red = limit;
	}
	
	/**
	 * Returns the color Green. (0.0F - 1.0F)
	 * @return
	 */
	public float getGreen() {
		return this.green;
	}
	
	/**
	 * Sets the color Green. (0.0F - 1.0F)
	 * @param red
	 */
	public void setGreen(float green) {
		float limit = limit(green);
		this.green = limit;
	}
	
	/**
	 * Returns the color Blue. (0.0F - 1.0F)
	 * @return
	 */
	public float getBlue() {
		return this.blue;
	}
	
	/**
	 * Sets the color Blue. (0.0F - 1.0F)
	 * @param red
	 */
	public void setBlue(float blue) {
		float limit = limit(blue);
		this.blue = limit;
	}
	
	/**
	 * Returns the Alpha value. (0.0F - 1.0F)
	 * @return
	 */
	public float getAlpha() {
		return this.alpha;
	}
	
	/**
	 * Sets the Alpha value. (0.0F - 1.0F)
	 * @param red
	 */
	public void setAlpha(float alpha) {
		float limit = limit(alpha);
		this.alpha = limit;
	}
	
	/**
	 * Limits a float value between 0 and 1.
	 * @param value
	 * @return
	 */
	public static float limit(float value) {
		return limit(value, 0.0F, 1.0F);
	}
	
	/**
	 * Limits a float value between a given minimum value, and maximum value.
	 * @param value
	 * @param minimum
	 * @param maximum
	 * @return
	 */
	public static float limit(float value, float minimum, float maximum) {
		float f = value;
		if (f > maximum) f = maximum;
		else if (f < minimum) f = minimum;
		return f;
	}
	
	@Override
	public void onLoad(KahluaTable table) {
		setRed(  (float) table.rawget("r"));
		setGreen((float) table.rawget("g"));
		setBlue( (float) table.rawget("b"));
		setAlpha((float) table.rawget("a"));
	}
	
	@Override
	public String toString() {
		return getRed() + ";" + getGreen() + ";" + getBlue() + ";" + getAlpha();
	}

	@Override
	/**
	 * Returns 0 if the other color matches. Returns 1 if not matching. Returns 2 if the colors match, however the alpha do not.
	 */
	public int compareTo(Color other) {
		
		// Variables
		float r, g, b, a, or, og, ob, oa;
		
		// This color.
		r = getRed();
		g = getGreen();
		b = getBlue();
		a = getAlpha();
		
		// Other color
		or = other.getRed();
		og = other.getGreen();
		ob = other.getBlue();
		oa = other.getAlpha();
		
		// Matching.
		if(r == or && g == og && b == ob && a == oa) return 0;
		
		// Matching color, but not alpha.
		else if(r == or && g == og && b == ob && a == oa) return 2;
		
		// Not matching.
		else return 1;
	}

	@Override
	public void onExport() {
		set("r", getRed());
		set("g", getGreen());
		set("b", getBlue());
		set("a", getAlpha());
	}
	
}

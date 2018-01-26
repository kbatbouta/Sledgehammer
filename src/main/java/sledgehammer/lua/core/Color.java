/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

package sledgehammer.lua.core;

import java.util.HashMap;
import java.util.Map;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.lua.LuaTable;

/**
 * LuaTable that handles the storage of color data, and color utilities for the Sledgehammer engine.
 *
 * @author Jab
 */
public class Color extends LuaTable implements Comparable<Color> {

  // @formatter:off
  public static final Color WHITE = new Color(1.0f, 1.0f, 1.0f);
  public static final Color LIGHT_GRAY = new Color(0.7f, 0.7f, 0.7f);
  public static final Color DARK_GRAY = new Color(0.3f, 0.3f, 0.3f);
  public static final Color BLACK = new Color(0.0f, 0.0f, 0.0f);
  public static final Color LIGHT_RED = new Color(1.0f, 0.6f, 0.6f);
  public static final Color RED = new Color(1.0f, 0.25f, 0.25f);
  public static final Color DARK_RED = new Color(0.6f, 0.0f, 0.0f);
  public static final Color BEIGE = new Color(1.0f, 0.65f, 0.38f);
  public static final Color ORANGE = new Color(1.0f, 0.45f, 0.18f);
  public static final Color BROWN = new Color(0.6f, 0.2f, 0.1f);
  public static final Color LIGHT_YELLOW = new Color(1.0f, 1.0f, 0.8f);
  public static final Color YELLOW = new Color(1.0f, 1.0f, 0.25f);
  public static final Color DARK_YELLOW = new Color(0.6f, 0.6f, 0.0f);
  public static final Color LIGHT_GREEN = new Color(0.6f, 1.0f, 0.6f);
  public static final Color GREEN = new Color(0.25f, 1.0f, 0.25f);
  public static final Color DARK_GREEN = new Color(0.0f, 0.6f, 0.0f);
  public static final Color LIGHT_BLUE = new Color(0.6f, 1.0f, 1.0f);
  public static final Color BLUE = new Color(0.25f, 1.0f, 1.0f);
  public static final Color DARK_BLUE = new Color(0.25f, 0.25f, 1.0f);
  public static final Color INDIGO = new Color(0.5f, 0.5f, 1f);
  public static final Color LIGHT_PURPLE = new Color(1.0f, 0.6f, 1.0f);
  public static final Color PURPLE = new Color(1.0f, 0.25f, 1.0f);
  public static final Color DARK_PURPLE = new Color(0.6f, 0.0f, 0.6f);
  public static final Color PINK = new Color(1.0f, 0.45f, 1.0f);
  public static final Color DARK_AQUA = new Color(0.0F, 0.5F, 0.5F);
  public static final Color AQUA = new Color(0.0F, 1.0F, 1.0F);
  public static final Color GOLD = new Color(1.0F, 0.84F, 0.0F);
  // @formatter:on

  /** The Map storing String keys, paired with the Color constants. */
  public static Map<String, Color> mapColors;

  /** Red (0.0-1.0 Float) (Default is 0.0F) */
  private float red = 0.0F;
  /** Green (0.0-1.0 Float) (Default is 0.0F) */
  private float green = 0.0F;
  /** Blue (0.0-1.0 Float) (Default is 0.0F) */
  private float blue = 0.0F;
  /** Alpha (0.0-1.0 Float) (Default is 1.0F) */
  private float alpha = 1.0F;

  /**
   * New constructor.
   *
   * @param r The Float red value, between 0 and 1.
   * @param g The Float green value, between 0 and 1.
   * @param b The Float blue value, between 0 and 1.
   */
  public Color(float r, float g, float b) {
    super("Color");
    setRed(r);
    setGreen(g);
    setBlue(b);
  }

  /**
   * New constructor with alpha option.
   *
   * @param r The Float red value, between 0 and 1.
   * @param g The Float green value, between 0 and 1.
   * @param b The Float blue value, between 0 and 1.
   * @param a The Float alpha value, between 0 and 1.
   */
  public Color(float r, float g, float b, float a) {
    super("Color");
    setRed(r);
    setGreen(g);
    setBlue(b);
    setAlpha(a);
  }

  /**
   * Lua load constructor.
   *
   * @param table The KahluaTable storing the Color data.
   */
  public Color(KahluaTable table) {
    super("Color", table);
  }

  /**
   * Empty Constructor. The default color is Black, being that red will be 0, green will be 0, blue
   * will be 0, and alpha will be 1.
   */
  public Color() {
    super("Color");
  }

  /**
   * Returns 0 if the other color matches. Returns 1 if not matching. Returns 2 if the colors match,
   * however the alpha do not.
   *
   * @param other The Color to compare.
   * @return Returns the standard Integer result from Comparators.
   */
  @Override
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
    if (r == or && g == og && b == ob && a == oa) {
      return 0;
    }
    // Matching color, but not alpha.
    else if (r == or && g == og && b == ob && a == oa) {
      return 2;
    }
    // Not matching.
    else {
      return 1;
    }
  }

  @Override
  public void onLoad(KahluaTable table) {
    setRed((float) table.rawget("r"));
    setGreen((float) table.rawget("g"));
    setBlue((float) table.rawget("b"));
    setAlpha((float) table.rawget("a"));
  }

  @Override
  public void onExport() {
    // @formatter:off
    set("r", getRed());
    set("g", getGreen());
    set("b", getBlue());
    set("a", getAlpha());
    // @formatter:on
  }

  @Override
  public String toString() {
    return getRed() + ";" + getGreen() + ";" + getBlue() + ";" + getAlpha();
  }

  /**
   * Sets all Color values, and the Alpha value.
   *
   * @param red The Float red value, between 0 and 1.
   * @param green The Float green value, between 0 and 1.
   * @param blue The Float blue value, between 0 and 1.
   * @param alpha The Float alpha value, between 0 and 1.
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
   *
   * @param red The Float red value, between 0 and 1.
   * @param green The Float green value, between 0 and 1.
   * @param blue The Float blue value, between 0 and 1.
   */
  public void set(float red, float green, float blue) {
    // Set all color values.
    setRed(red);
    setGreen(green);
    setBlue(blue);
  }

  /** @return Returns the color Red. (0.0F - 1.0F) */
  public float getRed() {
    return this.red;
  }

  /**
   * Sets the color Red. (0.0F - 1.0F)
   *
   * @param red The Float to set.
   */
  public void setRed(float red) {
    this.red = limit(red);
  }

  /** @return Returns the color Green. (0.0F - 1.0F) */
  public float getGreen() {
    return this.green;
  }

  /**
   * Sets the color Green. (0.0F - 1.0F)
   *
   * @param green The Float to set.
   */
  public void setGreen(float green) {
    this.green = limit(green);
  }

  /** @return Returns the color Blue. (0.0F - 1.0F) */
  public float getBlue() {
    return this.blue;
  }

  /**
   * Sets the color Blue. (0.0F - 1.0F)
   *
   * @param blue The Float to set.
   */
  public void setBlue(float blue) {
    this.blue = limit(blue);
  }

  /** @return Returns the Alpha value. (0.0F - 1.0F) */
  public float getAlpha() {
    return this.alpha;
  }

  /**
   * Sets the Alpha value. (0.0F - 1.0F)
   *
   * @param alpha The Float to set.
   */
  public void setAlpha(float alpha) {
    this.alpha = limit(alpha);
  }

  /** @return Returns a String encoded color tag for Lua. */
  public String toTag() {
    return "<RGB:" + getRed() + "," + getGreen() + "," + getBlue() + ">";
  }

  /**
   * Limits a float value between 0 and 1.
   *
   * @param value The Float value to limit. If the value is lower than 0, 0 will be returned. if the
   *     value is larger than 1, then 1 is returned.
   * @return Returns a value between or equal to 0 or 1.
   */
  public static float limit(float value) {
    return limit(value, 0.0F, 1.0F);
  }

  /**
   * Limits a float value between a given minimum value, and maximum value.
   *
   * @param value The Float value to limit. If the value is lower than the Float minimum value, the
   *     minimum value will be returned. if the value is larger than the Float maximum value, then
   *     the maximum value is returned.
   * @param minimum The Float minimum value for the returned Float value to be returned.
   * @param maximum The Float maximum value for the returned Float value to be returned.
   * @return Returns the processed Float value between the Float minimum and Float maximum value
   *     given.
   */
  public static float limit(float value, float minimum, float maximum) {
    return value > maximum ? maximum : value < minimum ? minimum : value;
  }

  /**
   * @param color The String name of the color.
   * @return Returns a Color Object with the String name. If no Color is registered with the String
   *     name given, null is returned.
   */
  public static Color getColor(String color) {
    return mapColors.get(color.toLowerCase());
  }

  static {
    mapColors = new HashMap<>();
    // @formatter:off
    mapColors.put("white", WHITE);
    mapColors.put("light-gray", LIGHT_GRAY);
    mapColors.put("dark-gray", DARK_GRAY);
    mapColors.put("black", BLACK);
    mapColors.put("light-red", LIGHT_RED);
    mapColors.put("red", RED);
    mapColors.put("dark-red", DARK_RED);
    mapColors.put("beige", BEIGE);
    mapColors.put("orange", ORANGE);
    mapColors.put("brown", BROWN);
    mapColors.put("light-yellow", LIGHT_YELLOW);
    mapColors.put("yellow", YELLOW);
    mapColors.put("dark-yellow", DARK_YELLOW);
    mapColors.put("light-green", LIGHT_GREEN);
    mapColors.put("green", GREEN);
    mapColors.put("dark-green", DARK_GREEN);
    mapColors.put("indigo", INDIGO);
    mapColors.put("light-blue", LIGHT_BLUE);
    mapColors.put("blue", BLUE);
    mapColors.put("dark-blue", DARK_BLUE);
    mapColors.put("light-purple", LIGHT_PURPLE);
    mapColors.put("purple", PURPLE);
    mapColors.put("dark-purple", DARK_PURPLE);
    mapColors.put("pink", PINK);
    mapColors.put("dark-aqua", DARK_AQUA);
    mapColors.put("aqua", AQUA);
    mapColors.put("gold", GOLD);
    // @formatter:on
  }
}

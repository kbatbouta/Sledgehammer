package sledgehammer.objects;

import java.util.Map;

import se.krka.kahlua.vm.KahluaTable;

/**
 * Color Lua class for SledgeHammer Lua.
 * @author Jab
 *
 */
public class LuaObject_Color extends LuaObject implements Comparable<LuaObject_Color> {

	/**
	 * Name of the LuaObject.
	 */
	public static final String NAME = "Color";
	
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
	public LuaObject_Color() {
		super(NAME);
	}
	
	public LuaObject_Color(KahluaTable table) {
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
		this.set("r", limit);
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
		this.set("g", limit);
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
		this.set("b", limit);
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
		this.set("a", limit);
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
	public void construct(Map<String, Object> definitions) {
		definitions.put("r", getRed()  );
		definitions.put("g", getGreen());
		definitions.put("b", getBlue() );
		definitions.put("a", getAlpha());
	}

	@Override
	public void load(KahluaTable table) {
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
	public int compareTo(LuaObject_Color other) {
		
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
	
}

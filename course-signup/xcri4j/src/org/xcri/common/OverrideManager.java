package org.xcri.common;

import java.util.HashMap;
import java.util.Map;

import org.xcri.types.XcriElement;

public class OverrideManager {

	private static Map<Class, XcriElement> overrides = new HashMap<Class, XcriElement>();
	
	public static void registerOverride(Class baseClass, XcriElement override) {
		overrides.put(baseClass, override);
	}
	
	public static Map<Class, XcriElement> getOverrides() {
		return overrides;
	}
	
	public static void clear() {
		overrides.clear();
	}
}

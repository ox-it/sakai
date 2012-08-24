package org.xcri.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.xcri.Extension;

public class ExtensionManager {

	private static List<Extension> extensions = new ArrayList<Extension>();
	
	public static void registerExtension(Extension extension) {
		extensions.add(extension);
	}
	
	public static Collection<Extension> getExtensions() {
		return extensions;
	}
}

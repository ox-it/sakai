package org.xcri.factory;

import org.xcri.common.OverrideManager;
import org.xcri.core.Presentation;

public class PresentationFactory {

	public static Presentation getPresentation(Class baseClass) throws InstantiationException, IllegalAccessException {
		if (OverrideManager.getOverrides().containsKey(baseClass)) {
			return (Presentation) OverrideManager.getOverrides().get(baseClass).getClass().newInstance();
		} else {
			return new Presentation();
		}
	}

}

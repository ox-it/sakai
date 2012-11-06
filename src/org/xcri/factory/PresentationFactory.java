package org.xcri.factory;

import org.xcri.common.OverrideManager;
import org.xcri.core.Course;
import org.xcri.core.Course;
import org.xcri.core.Presentation;
import org.xcri.core.Presentation;
import org.xcri.extensions.oxcap.OxcapPresentation;

public class PresentationFactory {

	public static Presentation getPresentation(Class baseClass) throws InstantiationException, IllegalAccessException {
		if (OverrideManager.getOverrides().containsKey(baseClass)) {
			return (Presentation) OverrideManager.getOverrides().get(baseClass).getClass().newInstance();
		} else {
			return new Presentation();
		}
	}

}

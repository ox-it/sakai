package uk.ac.ox.oucs.oxam.logic;

import uk.ac.ox.oucs.oxam.model.AcademicYear;

public class AcademicYearServiceImpl implements AcademicYearService {

	public AcademicYear getAcademicYear(int year) {
		// As AcademicYear instances aren't mutable, we could return the same instance
		// every time for the same arguments.
		// If we end up creating 1000s of these then it may be worth it.
		return new AcademicYear(year);
	}

}

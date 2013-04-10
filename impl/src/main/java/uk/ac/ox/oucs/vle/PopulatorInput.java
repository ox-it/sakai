package uk.ac.ox.oucs.vle;

import java.io.InputStream;

public interface PopulatorInput {

	public abstract InputStream getInput(PopulatorContext context);

}

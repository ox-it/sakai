package uk.ac.ox.oucs.oxam.logic;

import java.io.InputStream;

/**
 * A simple value source that chain's a couple of sources together.
 * If the first valuesource doesn't return anything, it tries the second.
 * @author buckett
 *
 */
public class ChainedValueSource implements ValueSource {

	private ValueSource first;
	private ValueSource second;
	
	public ChainedValueSource(ValueSource first, ValueSource second) {
		this.first = first;
		this.second = second;
	}
	
	@Override
	public InputStream getInputStream() {
		InputStream is = first.getInputStream();
		if (is == null) {
			is = second.getInputStream();
		}
		return is;
	}

}

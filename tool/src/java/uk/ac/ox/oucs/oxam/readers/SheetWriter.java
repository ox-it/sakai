package uk.ac.ox.oucs.oxam.readers;

public interface SheetWriter {

	public void nextRow();
	public void writeColumn(String value);
	public void flush();
}

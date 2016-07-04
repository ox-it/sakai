package uk.ac.ox.oucs.oxam.readers;


/**
 * This is a layer over XLS and CSV, so we deal in the abstract.
 * @author buckett
 *
 */
public interface SheetReader {
	
	/**
	 * Move to the next row.
	 * @return <code>false</code> if there are no more rows
	 */
	boolean nextRow();
	

	/**
	 * Move to the next column.
	 * @return <code>false</code> if there are no more columns
	 */
	boolean nextColumn();
	
	
	int getRow();
	int getColumn();
	
	/**
	 * Gets the contents of a cell.
	 */
	String getContents();
	
}

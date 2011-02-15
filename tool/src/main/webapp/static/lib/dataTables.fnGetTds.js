/*
 * Function: $().dataTable().fnGetTds
 * Purpose:  Get an array of TD nodes from a row, taking into account column visibility
 * Returns:  node: - TD element
 * Inputs:   mixed:mTr - 
 *             node: - TR element to get the TD child from
 *             int: - aoData index
 */
$.fn.dataTableExt.oApi.fnGetTds  = function ( oSettings, mTr )
{
	var anTds = [];
	var anVisibleTds = [];
	var iCorrector = 0;
	var nTd, iColumn, iColumns;
	
	/* Take either a TR node or aoData index as the mTr property */
	var iRow = (typeof mTr == 'object') ? 
		oSettings.oApi._fnNodeToDataIndex(oSettings, mTr) : mTr;
	var nTr = oSettings.aoData[iRow].nTr;
	
	/* Get an array of the visible TD elements */
	for ( iColumn=0, iColumns=nTr.childNodes.length ; iColumn<iColumns ; iColumn++ )
	{
		nTd = nTr.childNodes[iColumn];
		if ( nTd.nodeName.toUpperCase() == "TD" )
		{
			anVisibleTds.push( nTd );
		}
	}
	
	/* Construct and array of the combined elements */
	for ( iColumn=0, iColumns=oSettings.aoColumns.length ; iColumn<iColumns ; iColumn++ )
	{
		if ( oSettings.aoColumns[iColumn].bVisible )
		{
			anTds.push( anVisibleTds[iColumn-iCorrector] );
		}
		else
		{
			anTds.push( oSettings.aoData[iRow]._anHidden[iColumn] );
			iCorrector++;
		}
	}
	
	return anTds;
}

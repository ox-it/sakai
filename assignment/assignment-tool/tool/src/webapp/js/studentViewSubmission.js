var ASN_SVS = ASN_SVS || {};

/* For the cancel button - if the user made progress, we need them to confirm that they want to discard their progress */
ASN_SVS.confirmDiscardOrSubmit = function(editorInstanceName, attachmentsModified)
{
	var inlineProgress = false;
	var ckEditor = CKEDITOR.instances[editorInstanceName];
	if (ckEditor)
	{
		inlineProgress = ckEditor.checkDirty();
	}
	var showDiscardDialog = inlineProgress || attachmentsModified;
	var submitPanel = document.getElementById("submitPanel");
	var confirmationDialogue = document.getElementById("confirmationDialogue");
	if (showDiscardDialog)
	{
		submitPanel.style.display = "none"
		confirmationDialogue.style.display = "block";
	}
	else
	{
		SPNR.disableControlsAndSpin( this, null );
		ASN.submitForm( 'addSubmissionForm', 'cancel', null, null );
	}
};

ASN_SVS.undoCancel = function()
{
	var submitPanel = document.getElementById("submitPanel");
	var confirmationDialogue = document.getElementById("confirmationDialogue");
	submitPanel.style.display = "block";
	confirmationDialogue.style.display = "none";
};

$(document).ready(function()
{
	var inTool = false;

	window.addEventListener("click", function (event) {
		if ($.contains(document.getElementById('addSubmissionForm'), event.target)) {
			inTool = true;
		}
		else {
			inTool = false;
		}
	});

	window.addEventListener("beforeunload", function (event) {
		// Add a returnValue so that a pop-up occurs if there is an attachment and the user
		// tries to click on anything other than attachment buttons/links.
		if ($("#attachments").length != 0 && (inTool === false)) {
			event.returnValue = "";
		}
	});
});
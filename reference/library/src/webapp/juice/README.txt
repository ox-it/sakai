Readme

1. This has been tested with Juice 0.6.4
2. Download juice-basic-0.6.4.zip from http://code.google.com/p/juice-project/downloads/list
3. Unzip juice-basic-0.6.4.zip into an appropriate web accessible directory on the server where you wish to deploy Juice. For the purposes of this guide, it is assumed the directory is called 'juice'
4. Add the file sakaich_metadef.js to /juice/metadefs
5. Add the file daiaAvailability.js to /juice/extensions
6. Add the file juice-weblearn.js to /juice
7. In the webpage(s) where you wish to deploy the juice extension add the following lines before the closing </body> tag:
		<script type="text/javascript" src="juice/jquery-1.3.2.min.js"></script>
		<script type="text/javascript">
		jQuery.noConflict();
		</script>
		<script type="text/javascript" src="juice/juice.js"></script>
		<script type="text/javascript" src="juice/juice-weblearn.js"></script>
		
8. You will need to edit the paths to the /juice directory in these lines and also in juice-weblearn.js if your webpage is not in a directory containing the /juice directory
9. Other variables set in juice-weblearn.js can be edited as desired - including:
	availServer = the URL of the LIAS availability service for print items (currently http://cod.oucs.ox.ac.uk:40080/library-availability/library)
	eavailServer = the URL of the EIAS availability service for electronic items (currently http://cod.oucs.ox.ac.uk:40080/library-availability/eias)
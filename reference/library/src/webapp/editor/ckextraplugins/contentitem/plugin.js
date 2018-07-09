/**
 * Basic sample plugin inserting current date and time into CKEditor editing area.
 */

// Register the plugin with the editor.
// http://docs.cksource.com/ckeditor_api/symbols/CKEDITOR.plugins.html
var ContentItemIFrameWindow = null;
CKEDITOR.plugins.add( 'contentitem',
{ requires : [ 'iframedialog' ], lang: ['en'],
    // The plugin initialization logic goes inside this method.
    // http://docs.cksource.com/ckeditor_api/symbols/CKEDITOR.pluginDefinition.html#init
    init: function( editor )
    {
        // http://ckeditor.com/forums/CKEditor-3.x/iframe-dialog-how-get-ok-button-pressed-event
        // https://gist.github.com/garryyao/1170303
        var height = 480, width = 750;
        CKEDITOR.dialog.addIframe(
               'ContentItemDialog',
               'Select Content Item',
               sakai.editor.contentItemUrl, width, height,
               function()
               {
                    // Iframe loaded callback.
                    var iframe = document.getElementById( this._.frameId );
                    ContentItemIFrameWindow = iframe.contentWindow;
                    // console.log(ContentItemIFrameWindow);
               },

               {
                    onOk : function()
                    {
                        // Dialog onOk callback.
                        // console.log(ContentItemIFrameWindow.returned_content_item);
                        var items = ContentItemIFrameWindow.returned_content_item;
                        if ( items ) for(var i=0; i < items.length; i++) {
                            var item = items[i];
                            console.log(item['@type']);
                            var linktarget;
                            var cssClass;
                            switch(item['@type']) {
                                case 'LtiLinkItem':
                                    cssClass = "lti-launch";
                                    linktarget = 'window';
                                case 'ContentItem':
                                    cssClass = "lti-contentitem";
                                    linktarget = 'window';
                                    break;
                                case 'FileItem':
                                    cssClass = 'lti-image';
                                    linktarget = 'embed';
                                    break;
                                default:
                                    // TODO
                            }

                            var title = item.title;
                            var width, height, windowTarget;
                            if (item['placementAdvice']) {
                                // Might be empty
                                width = item['placementAdvice'].displayWidth;
                                height = item['placementAdvice'].displayHeight;
                                windowTarget = item['placementAdvice'].presentationDocumentTarget;
                                linktarget = item['placementAdvice'].presentationDocumentTarget || linktarget;
                            }

                            // windowTarget looks to possibly override the placementAdvice.
                            var content = "";
                            switch (linktarget) {
                                // Link to current frame.
                                case 'frame':
                                    content = '<a href="'+ item.launch+ '" target="'+ (windowTarget || "_self")+
                                        ' class="'+ cssClass+'">'+ item.title+ '</a><br/>';
                                    break;
                                // Embeded iframe
                                case 'iframe':
                                    content = '<iframe src="'+ item.launch+ '" '+
                                        'height="'+ (height || 300)+ '" '+
                                        'width="'+ (width || 500)+ '" '+
                                        '></iframe>';
                                    break;
                                // Opening a link
                                case 'window':
                                    content = '<a href="'+ item.launch+ '" target="'+ (windowTarget || "_blank")+
                                        ' class="'+ cssClass+'">'+ item.title+ '</a><br/>';
                                    break;
                                // Embedded in the page
                                case 'embed':
                                    if (item['mediaType'].startsWith('image/')) {
                                        content = '<img src="'+ item.launch+ '" class="'+ cssClass+ '" '+
                                            (height)?'height="'+ height+ '" ':''+
                                            (width)?'width="'+ width+ '" ':''+
                                            ' class="'+ cssClass+'"><br/>';
                                    } else if (item['mediaType'].startsWith('video/')) {
                                        content = '<embed type="'+ item['mediaType']+ '" '+ 'src="'+ item.url+ '" '+
                                        (height)?'height="'+ height+ '" ':''+
                                        (width)?'width="'+ width+ '" ':''+
                                        ' class="'+ cssClass+ '"><br/>';
                                    } else {
                                        // TODO
                                    }
                                    break;
                                default:
                                    // TODO
                                    break;
                            }
                            editor.insertHtml(content);

                            // try {
                            //     if ( item['@type'] == 'LtiLinkItem') {
                            //         editor.insertHtml( '<a href="' + item.launch + '" target="_blank" class="lti-launch">'+item.title+'</a><br/>' );
                            //     } else if ( item['@type'] == 'ContentItem') {
                            //         editor.insertHtml( '<a href="' + item.url + '" target="_blank" class="lti-contentitem">'+item.title+'</a><br/>' );
                            //     } else if ( item['@type'] == 'FileItem' && item['mediaType'].startsWith('image/') ) {
                            //         editor.insertHtml( '<img src="' + item.url + '" target="_blank" class="lti-image"><br/>' );
                            //     } else {
                            //         console.log('Not handled: '+item['@type']);
                            //     }
                            // } catch(err) {
                            //     console.log(err);
                            // }
                        }
                    }
               }
        );
        editor.addCommand( 'ContentItemDialog', new CKEDITOR.dialogCommand( 'ContentItemDialog' ) );

        // Create a toolbar button that executes the plugin command.
        // http://docs.cksource.com/ckeditor_api/symbols/CKEDITOR.ui.html#addButton
	editor.ui.addButton( 'ContentItem',
        {
            // Toolbar button tooltip.
            label: 'Insert ContentItem',
            // Reference to the plugin command name.
            command: 'ContentItemDialog',
            // Button's icon file path.
            icon: this.path + 'images/contentitem.png'
        } );
    },


} );

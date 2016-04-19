/**
 * DocumentList and DocumentActions (details page) email actions
 *
 * Adding action event handlers to Alfresco.doclib.Actions, which is picked up
 * by both Alfresco.DocumentList and Alfresco.DocumentActions
 *
 * Note. this file must be loaded before document-actions.js and documentlist.js
 *
 * @author ecmstuff.blogspot.com
 */
(function() {
	var $div = '';
    YAHOO.Bubbling.fire("registerAction",
    {
        actionName: "onActionAddImagesToPdfFiles",
        fn: function mycompany_onActionAddImagesToPdfFiles(file) {
			var ticket = '';
			var obj = this;
			if($('#dialog').length == 0){
				$( '<div id="dialog"/>' ).appendTo(document.body);
				$( "#dialog" ).dialog({
				  autoOpen: false,
				  height:400,
				  width:530,
				  draggable: false,
				  modal: true,
				  buttons: {
					"Ok": function() {
						if(	$('#pagenumber').val() != '' 
							&& $('#nodeRef').val() != ''
							&& $('#cordinate_x').val() != ''
							&& $('#cordinate_y').val() != ''){
								obj.modules.actions.genericAction(
					            {
					                success:
					                {
					                    message: obj.msg("message.addimages.success", file.displayName, Alfresco.constants.USERNAME)
					                },
					                failure:
					                {
					                    message: obj.msg("message.addimages.failure", file.displayName, Alfresco.constants.USERNAME)
					                },
					                webscript:
					                {
					                    name: "handlefile?"+$('#frm1').serialize(),
					                    stem: Alfresco.constants.PROXY_URI,
					                    method: Alfresco.util.Ajax.GET
					                },
					                config:
					                {
					                }
					            });
								$( this ).dialog( "close" );
						}
					},
					Cancel: function() {
						$( this ).dialog( "close" );
					}
				  }
				});
			}
			if($( "#dialog" ).length > 0){
				$.ajax({
					url: Alfresco.constants.PROXY_URI+'addimages',
					cache: false,
					success: function (data) {
						$('#dialog').html(data);
						$('#sourcenodeRef').val(file.nodeRef);
						$('#containment-wrapper').css("background-image", "url("+Alfresco.constants.PROXY_URI+"picture?pagenumber=1&"+$('#sourcenodeRef').serialize()+")");
						$( "#dialog" ).dialog( "open" );
					}
				});
			}
        }
    });
})();
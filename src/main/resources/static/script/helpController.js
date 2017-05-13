/**
 * Controller for the help dialog.
 *
 * @author Marc Plouhinec
 */
var helpController = {

    /**
     * Handle events.
     */
    initialize: function () {
        $('#help-panel-close-button').click(function () {
            $('#help-panel').hide();
        });
    },

    /**
     * Open the help dialog.
     */
    openHelpDialog: function () {
        $('#help-panel').show();
    }

};
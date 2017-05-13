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
     * Open the about dialog.
     */
    openAboutDialog: function () {
        $('#help-panel').show();
        $('#about-information').show();
        $('#agency-rating-information').hide();
        $('#help-panel-title').text('About');
    },

    /**
     * Open the about dialog.
     */
    openRatingDialog: function () {
        $('#help-panel').show();
        $('#about-information').hide();
        $('#agency-rating-information').show();
        $('#help-panel-title').text('Rating information');
    }

};
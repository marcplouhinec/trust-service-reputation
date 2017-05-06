/**
 * Application entry point.
 *
 * @author Marc Plouhinec
 */
var mainController = {

    /**
     * Initialize the application.
     */
    main: function () {
        treeController.initialize();
        toolbarController.initialize();
    }

};

$(document).ready(function () {
    mainController.main();
});

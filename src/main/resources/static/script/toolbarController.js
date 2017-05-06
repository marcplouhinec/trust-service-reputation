/**
 * Controller for the top toolbar.
 *
 * @author Marc Plouhinec
 */
var toolbarController = {

    /**
     * Initialize event listeners.
     */
    initialize: function () {
        $('button#collapse-all').click(function () {
            treeController.collapseAll();
        });
        $('button#expand-all').click(function () {
            treeController.expandAll();
        });
        $('button#collapse-documents').click(function () {
            treeController.collapseDocuments();
        });
        $('button#collapse-all-but-top-agencies').click(function () {
            treeController.collapseAllButTopAgencies();
        });
    }

};
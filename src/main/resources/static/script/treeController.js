/**
 * Controller for the tree representing agencies and documents.
 *
 * @author Marc Plouhinec
 */
var treeController = {

    /**
     * Initialize event listeners and default display configuration.
     */
    initialize: function () {
        // Handle the expand/collapse buttons
        $('.expandable-tree-node > button').click(function () {
            var $subNodes = $(this).parent().parent().children('.tree-sub-nodes');

            if ($(this).text() === '-') {
                $subNodes.hide();
                $(this).text('+');
            } else {
                $subNodes.show();
                $(this).text('-');
            }
        });

        // Hide long lists of agency names with a "show all" button
        $('table.agency-names').each(function () {
            var $agencyNameRows = $(this).find('> tbody > tr');
            if ($agencyNameRows.length >= 3) {

                // Just display two agency names
                var $hiddenRows = $agencyNameRows.filter(function (index) {
                    return index >= 2;
                });
                $hiddenRows.hide();

                // Create a "show all" button
                var $showAllRow = $(
                    '<tr><td colspan="2">' +
                    '<button class="show-agency-names">Show all</button>' +
                    '</td></tr>');
                $(this).append($showAllRow);

                $showAllRow.find('button').click(function () {
                    $hiddenRows.show();
                    $showAllRow.remove();
                });
            }
        });
    }

};
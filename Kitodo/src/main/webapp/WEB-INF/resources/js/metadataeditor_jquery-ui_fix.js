/**
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

/* eslint-disable complexity */

/**
 * Overwrite jquery-ui method with optimized implementation.
 * 
 * https://github.com/jquery/jquery-ui/blob/440f38940dcb0727a0f6144e991fcb50ed1d5755/ui/widgets/droppable.js#L317-L358
 * (MIT Licensed, see: https://github.com/jquery/jquery-ui/blob/main/LICENSE.txt)
 * 
 * The following method caused a short freeze when beginning to drag an object that has 
 * many droppable positions. The main loop "droppablesLoop" contains statements that both 
 * adapt the DOM layout and measure the DOM layout for element positions (offsets). Because 
 * of that, the browser needs to re-calculate the current layout of the page multiple times 
 * while this loop is processed, which is very computationally expensive for large pages and 
 * causes a freeze of the browser.
 * 
 * By splitting this loop into two, the performance is improved a lot:
 * - the first loop just determines which elements are active and updates some styling, but 
 *   does not measure the offsets of elements in the DOM layout
 * - the second loop retrieves all offset positions, but does not update any styling
 * 
 * Because of this two-phase implementation, only one page layout calculation is required.
 */
 $.ui.ddmanager.prepareOffsets = function( t, event ) {

    var i, j,
        m = $.ui.ddmanager.droppables[ t.options.scope ] || [],
        type = event ? event.type : null, // workaround for #2317
        list = ( t.currentItem || t.element ).find( ":data(ui-droppable)" ).addBack(),
        filteredIdx = [];


    // first do stuff that influences layout
    droppablesLoop: for ( i = 0; i < m.length; i++ ) {

        // No disabled and non-accepted
        if ( m[ i ].options.disabled || ( t && !m[ i ].accept.call( m[ i ].element[ 0 ],
            ( t.currentItem || t.element ) ) ) ) {
            continue;
        }

        // Filter out elements in the current dragged item
        for ( j = 0; j < list.length; j++ ) {
            if ( list[ j ] === m[ i ].element[ 0 ] ) {
                m[ i ].proportions().height = 0;
                continue droppablesLoop;
            }
        }

        m[ i ].visible = m[ i ].element.css( "display" ) !== "none";
        if ( !m[ i ].visible ) {
            continue;
        }

        // Activate the droppable if used directly from draggables
        if ( type === "mousedown" ) {
            m[ i ]._activate.call( m[ i ], event );
        }

        // remember idx of elements that are measured for offset
        filteredIdx.push(i);

    }
   
    // collect all offsets
    for ( j = 0; j < filteredIdx.length; j++ ) {

        i = filteredIdx[j];

        m[ i ].offset = m[ i ].element.offset();
        m[ i ].proportions( {
            width: m[ i ].element[ 0 ].offsetWidth,
            height: m[ i ].element[ 0 ].offsetHeight
        } );

    }
};

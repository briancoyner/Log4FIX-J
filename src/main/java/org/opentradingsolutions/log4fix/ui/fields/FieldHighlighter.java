/*
 * The Log4FIX Software License
 * Copyright (c) 2006 - 2011 Brian M. Coyner All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 3. Neither the name of the product (Log4FIX), nor Brian M. Coyner,
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL BRIAN M. COYNER OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

package org.opentradingsolutions.log4fix.ui.fields;

import java.awt.Color;
import java.awt.Component;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.opentradingsolutions.log4fix.core.LogField;

/**
 * This class applies the color to each field row. Header, trailer, and body fields
 * are painted different colors.
 *
 * @author Brian M. Coyner
 * @todo - make the colors configurable.
 */
public class FieldHighlighter extends Highlighter {

    private Color dataFieldColor = new Color(198, 158, 236);
    private Color headerFieldColor = new Color(252, 152, 108);
    private Color trailerFieldColor = new Color(88, 211, 113);

    protected Color computeBackground(Component renderer, ComponentAdapter adapter) {
        JXTreeTable table = (JXTreeTable) adapter.getComponent();
        FieldTreeNode node = (FieldTreeNode) table.getPathForRow(adapter.row).getLastPathComponent();

        LogField field = node.getField();

        if (field.isHeaderField()) {
            return headerFieldColor;
        } else if (field.isTrailerField()) {
            return trailerFieldColor;
        } else {
            return dataFieldColor;
        }
    }
}

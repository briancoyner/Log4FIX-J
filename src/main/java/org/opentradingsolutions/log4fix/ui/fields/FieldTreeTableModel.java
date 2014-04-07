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

import javax.swing.tree.TreeNode;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.opentradingsolutions.log4fix.core.LogField;

/**
 * @author Brian M. Coyner
 */
public class FieldTreeTableModel extends DefaultTreeTableModel {

    public Object getChild(Object parent, int index) {
        return ((TreeNode) parent).getChildAt(index);
    }

    public int getChildCount(Object parent) {
        TreeNode node = (TreeNode) parent;
        if (node.getAllowsChildren()) {
            return node.getChildCount();
        }

        return 0;
    }

    public int getColumnCount() {
        return 5;
    }

    public String getColumnName(int column) {

        switch (column) {
            case 0:
                return "Field Name";
            case 1:
                return "Tag";
            case 2:
                return "Value";
            case 3:
                return "Field Value Name";
            case 4:
                return "Required";
            default:
                return "Dang It!";
        }
    }

    public Object getValueAt(Object node, int column) {

        FieldTreeNode fieldTreeNode = (FieldTreeNode) node;
        LogField field = fieldTreeNode.getField();

        switch (column) {
            case 0:
                return field.getFieldName();
            case 1:
                return field.getTag();
            case 2:
                return field.getValue();
            case 3:
                return field.getFieldValueName();
            case 4:
                return field.isRequired();
            default:
                return "Dang It!";
        }
    }
}

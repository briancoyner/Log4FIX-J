/*
 * The Log4FIX Software License
 * Copyright (c) 2006 - 2007 opentradingsolutions.org  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Neither the name of the product (Log4FIX), nor opentradingsolutions.org,
 *    nor the names of its contributors may be used to endorse or promote
 *    products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL OPENTRADINGSOLUTIONS.ORG OR
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

import org.opentradingsolutions.log4fix.core.LogField;
import org.opentradingsolutions.log4fix.core.LogGroup;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Brian M. Coyner
 */
public class FieldTreeNode implements TreeNode {

    private TreeNode parent;
    private final LogField field;
    private List<FieldTreeNode> groups = new ArrayList<FieldTreeNode>();

    public FieldTreeNode(LogField field) {
        this.field = field;

        if (field.isRepeatingGroup()) {

            List<LogGroup> logGroups = field.getGroups();
            for (LogGroup logGroup : logGroups) {
                List<LogField> fields = logGroup.getFields();
                for (LogField logField : fields) {
                    groups.add(new FieldTreeNode(logField));
                }
            }
        }
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public TreeNode getChildAt(int childIndex) {
        return groups.get(childIndex);
    }

    public int getChildCount() {
        return field.isRepeatingGroup() ? groups.size() : 0;
    }

    public TreeNode getParent() {
        return parent;
    }

    public int getIndex(TreeNode node) {
        return groups.indexOf(node);
    }

    public boolean getAllowsChildren() {
        return field.isRepeatingGroup();
    }

    public boolean isLeaf() {
        return !field.isRepeatingGroup();
    }

    public Enumeration children() {
        return Collections.enumeration(groups);
    }

    public LogField getField() {
        return field;
    }

    public String toString() {
        return field.getFieldName();
    }
}

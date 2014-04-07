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

import quickfix.Field;
import quickfix.field.MsgType;
import quickfix.field.Symbol;

import org.opentradingsolutions.log4fix.core.AbstractSessionTestCase;
import org.opentradingsolutions.log4fix.core.LogField;

/**
 * @author Brian M. Coyner
 */
public class RootNodeTest extends AbstractSessionTestCase {
    private RootNode root;

    public void doSetUp() {
        root = new RootNode();
    }

    public void testParentIsNull() {
        assertNull("The root node should not have a parent node.", root.getParent());
    }

    public void testAllowsChildren() {
        assertTrue("The root node must allow children.", root.getAllowsChildren());
    }

    public void testNotALeafNode() {
        assertFalse("The root node cannot be a leaf node.", root.isLeaf());
    }

    public void testGetChildrenWhenThereAreNoChildren() {
        assertEquals(0, root.getChildCount());
        assertFalse(root.children().hasMoreElements());
    }

    public void testGetChildrenWithIndexWhenThereAreNoChildrenThrows() {
        try {
            root.getChildAt(0);
            fail("Accessing a child when there are no children should fail fast.");
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    public void testAddFieldTreeNode() {

        FieldTreeNode node = new FieldTreeNode(createBasicField());
        root.addFieldTreeNode(node);

        assertEquals(1, root.getChildCount());
        assertSame(node, root.getChildAt(0));
    }

    private LogField createBasicField() {
        MsgType messageType = new MsgType(MsgType.ORDER_SINGLE);
        Field field = new Symbol("COYNER");
        return LogField.createLogField(messageType, field, getDictionary());
    }

    public void testAddMultipleFieldTreeNodes() {
        FieldTreeNode nodeA = new FieldTreeNode(createBasicField());
        root.addFieldTreeNode(nodeA);

        FieldTreeNode nodeB = new FieldTreeNode(createBasicField());
        root.addFieldTreeNode(nodeB);

        assertEquals(2, root.getChildCount());
        assertSame(nodeA, root.getChildAt(0));
        assertSame(nodeB, root.getChildAt(1));
    }
}

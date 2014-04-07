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

package org.opentradingsolutions.log4fix.ui.messages;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

/**
 * @author Brian M. Coyner
 */
public class MessageSearchField extends JTextField {
    private static final Border CANCEL_BORDER = new CancelBorder();

    public boolean armed;

    public MessageSearchField() {
        super(15);
        initCancelButtonBorder();
        initEscapeKeyListener();
    }

    private void initCancelButtonBorder() {
        setBorder(new CompoundBorder(getBorder(), CANCEL_BORDER));

        MouseInputListener mouseInputListener = new ClearSearchFieldMouseListener();
        addMouseListener(mouseInputListener);
        addMouseMotionListener(mouseInputListener);
    }

    private void initEscapeKeyListener() {
        addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancel();
                } else {
                    postActionEvent();
                }
            }
        });
    }

    private void cancel() {
        setText("");
        postActionEvent();
    }

    private static class CancelBorder extends EmptyBorder {

        private static final Color ARMED_COLOR = Color.GRAY.darker();

        CancelBorder() {
            super(0, 0, 0, 15);
        }

        public void paintBorder(Component c, Graphics oldGraphics, int x, int y,
                int width, int height) {
            MessageSearchField field = (MessageSearchField) c;
            if (field.getText().length() == 0) {
                return;
            }

            Graphics2D g = (Graphics2D) oldGraphics;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            final int circleL = 14;
            final int circleX = x + width - circleL;
            final int circleY = y + (height - 1 - circleL) / 2;
            g.setColor(field.armed ? ARMED_COLOR : Color.GRAY);
            g.fillOval(circleX, circleY, circleL, circleL);

            final int lineL = circleL - 8;
            final int lineX = circleX + 4;
            final int lineY = circleY + 4;
            g.setColor(Color.WHITE);
            g.drawLine(lineX, lineY, lineX + lineL, lineY + lineL);
            g.drawLine(lineX, lineY + lineL, lineX + lineL, lineY);
        }
    }

    private class ClearSearchFieldMouseListener extends MouseInputAdapter {

        public void mouseDragged(MouseEvent e) {
            arm(e);
        }

        public void mouseEntered(MouseEvent e) {
            arm(e);
        }

        public void mouseExited(MouseEvent e) {
            disarm();
        }

        public void mousePressed(MouseEvent e) {
            arm(e);
        }

        public void mouseReleased(MouseEvent e) {
            if (armed) {
                cancel();
            }
            disarm();
        }

        private void arm(MouseEvent e) {
            armed = (isOverButton(e) && SwingUtilities.isLeftMouseButton(e));
            repaint();
        }

        private void disarm() {
            armed = false;
            repaint();
        }

        private boolean isOverButton(MouseEvent e) {
            if (!contains(e.getPoint())) {
                return false;
            }

            Rectangle rectangle = SwingUtilities.calculateInnerArea(
                    MessageSearchField.this, null);
            return !rectangle.contains(e.getPoint());
        }
    }
}
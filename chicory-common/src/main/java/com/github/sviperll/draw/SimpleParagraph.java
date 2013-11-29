/*
 * Copyright (c) 2012, Victor Nazarov <asviraspossible@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Victor Nazarov nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.sviperll.draw;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Map;
import java.util.TreeMap;
import com.github.sviperll.draw.TextLine.TextAlignment;

public class SimpleParagraph implements Drawable {
    private final TextLine lines;
    private final ParagraphPlacement placement;
    private final Integer width;

    public SimpleParagraph(TextLine textLine, ParagraphPlacement paragraphPlacement, Integer width) {
        this.lines = textLine;
        this.placement = paragraphPlacement;
        this.width = width;
    }

    @Override
    public void draw(Graphics2D graphics, Point location) {
        Map<TextAttribute, Object> attrs = new TreeMap<TextAttribute, Object>();
        attrs.put(TextAttribute.FONT, graphics.getFont());

        AttributedString as = new AttributedString(lines.text(), attrs);
        AttributedCharacterIterator asi = as.getIterator();
        FontRenderContext frc = graphics.getFontRenderContext();

        float y = location.y;
        if (placement.alignment().equals(ParagraphPlacement.Alignment.BOTTOM)) {
            float height = 0;
            float firstLineHeight = 0;
            LineBreakMeasurer lbm = new LineBreakMeasurer(asi, frc);
            lbm.setPosition(asi.getBeginIndex());
            if (lbm.getPosition() < asi.getEndIndex()) {
                TextLayout tl = lbm.nextLayout(width);
                firstLineHeight = tl.getAscent() + tl.getDescent();
                height = firstLineHeight;
                while (lbm.getPosition() < asi.getEndIndex()) {
                    tl = lbm.nextLayout(width);
                    height += tl.getLeading() + tl.getAscent() + tl.getDescent();
                }
            }
            y = y - height + firstLineHeight;
        }

        LineBreakMeasurer lbm = new LineBreakMeasurer(asi, frc);
        lbm.setPosition(asi.getBeginIndex());
        while (lbm.getPosition() < asi.getEndIndex()) {
            TextLayout tl = lbm.nextLayout(width);
            float x = location.x;
            if (lines.alignment().equals(TextAlignment.RIGHT))
                x -= tl.getAdvance();
            float dy = 0;
            switch (placement.relativeTo()) {
                case BASELINE:
                    dy = 0;
                    break;
                case BORDER:
                    switch (placement.alignment()) {
                        case TOP:
                            dy = tl.getAscent();
                            break;
                        case BOTTOM:
                            dy = -tl.getDescent();
                            break;
                    }
                    break;
            }
            tl.draw(graphics, x, y + dy);
            y += tl.getAscent() + tl.getLeading() + tl.getDescent();
        }
    }

}

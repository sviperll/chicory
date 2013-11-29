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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

public class OptimalSizedParagraph implements Drawable {
    private final TextLine lines;
    private final ParagraphPlacement placement;
    private final Dimension dimension;

    public OptimalSizedParagraph(TextLine lines, ParagraphPlacement placement, Dimension dimension) {
        this.placement = placement;
        this.dimension = dimension;
        this.lines = lines;
    }

    @Override
    public void draw(Graphics2D graphics, Point point) {
        setOptimalFontSize(graphics);
        SimpleParagraph p = new SimpleParagraph(lines, placement, dimension.width);
        p.draw(graphics, point);
    }

    private void setOptimalFontSize(Graphics2D graphics) {
        FontRenderContext frc = graphics.getFontRenderContext();
        Font font = graphics.getFont();
        float size = font.getSize2D();
        for(;;) {
            LineBreakTester lbt = new LineBreakTester(frc, font);
            if (lbt.testLine())
                break;
            size -= 0.5;
            font = font.deriveFont(size);
        }
        graphics.setFont(font);
    }

    private class LineBreakTester {
        private final FontRenderContext frc;
        private final Font font;
        public LineBreakTester(FontRenderContext frc, Font font) {
            this.frc = frc;
            this.font = font;
        }
        public boolean testLine() {
            return testLine(1, 0);
        }

        private String getChar(int i) {
            return lines.text().substring(i, i + 1);
        }

        private boolean isSpace(int i) {
            return getChar(i).equals(" ");
        }

        private boolean isPunctuation(int i) {
            return getChar(i).equals("-");
        }

        private boolean isEof(int i) {
            return i == lines.text().length();
        }

        private boolean testLine(float height, int i) {
            while (isSpace(i))
                i++;
            if (isEof(i))
                return true;
            else {
                StringBuilder line = new StringBuilder();
                while (!isEof(i)) {
                    if (!isSpace(i))
                        line.append(getChar(i));
                    if (isSpace(i)
                        || isPunctuation(i)) {
                        TextLayout tl = new TextLayout(line.toString(), font, frc);
                        if (tl.getAdvance() <= dimension.getWidth()) {
                            float lineHeight = tl.getAscent() + tl.getDescent();
                            if (height + lineHeight <= dimension.getHeight()) {
                                float newHeight = height + lineHeight + tl.getLeading();
                                if (testLine(newHeight, i + 1))
                                    return true;
                            }
                        }
                    }
                    i++;
                }
                TextLayout tl = new TextLayout(line.toString(), font, frc);
                if (tl.getAdvance() <= dimension.getWidth()) {
                    float lineHeight = tl.getAscent() + tl.getDescent();
                    return height + lineHeight <= dimension.getHeight();
                } else
                    return false;
            }
        }
    }
}

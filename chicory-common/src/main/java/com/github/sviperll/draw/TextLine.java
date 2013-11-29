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
import java.awt.font.TextLayout;

public class TextLine implements Drawable {
    private final String text;
    private final TextAlignment alignment;

    public TextLine(String text, TextAlignment alignment) {
        this.text = text;
        this.alignment = alignment;
    }

    public TextLine(String text) {
        this(text, TextAlignment.LEFT);
    }

    public TextAlignment alignment() {
        return alignment;
    }

    public String text() {
        return text;
    }

    @Override
    public void draw(Graphics2D graphics, Point location) {
        TextLayout tl = new TextLayout(text, graphics.getFont(), graphics.getFontRenderContext());
        float x = location.x;
        if (alignment.equals(TextAlignment.RIGHT))
            x -= tl.getAdvance();
        tl.draw(graphics, x, location.y);
    }

    public enum TextAlignment {LEFT, RIGHT};

}

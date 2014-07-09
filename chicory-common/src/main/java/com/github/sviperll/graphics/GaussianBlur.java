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
package com.github.sviperll.graphics;

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

public class GaussianBlur implements BufferedImageOp {
    public static GaussianBlur prepare(double sigmaHor, double sigmaVer, int width, int height, RenderingHints renderingHints) {
        BufferedImageOp opHor = null;
        if (sigmaVer != 0) {
            float[] kernelValuesHor = gausianKernelValues(sigmaHor, width);
            Kernel kernelHor = new Kernel(kernelValuesHor.length, 1, kernelValuesHor);
            opHor = new ConvolveOp(kernelHor, ConvolveOp.EDGE_NO_OP, renderingHints);
        }

        BufferedImageOp opVer = null;
        if (sigmaVer != 0) {
            float[] kernelValuesVer = gausianKernelValues(sigmaVer, height);
            Kernel kernelVer = new Kernel(1, kernelValuesVer.length, kernelValuesVer);
            opVer = new ConvolveOp(kernelVer, ConvolveOp.EDGE_NO_OP, renderingHints);
        }
        return new GaussianBlur(opHor, opVer, renderingHints);
    }

    private static float[] gausianKernelValues(double sigma, int n) {
        double[] dvs = new double[n * 2 + 1];
        double sum = 0.0;
        for (int i = 0; i < dvs.length; i++) {
            double x = i - n;
            double v = 1.0 / Math.sqrt(2 * Math.PI * sigma * sigma) * Math.exp(- x * x / (2 * sigma * sigma));
            dvs[i] = v;
            sum += v;
        }

        float[] res = new float[n * 2 + 1];
        for (int i = 0; i < res.length; i++) {
            res[i] = (float)(dvs[i] / sum);
        }
        return res;
    }
    private final BufferedImageOp opHor;
    private final BufferedImageOp opVer;
    private final RenderingHints renderingHints;

    private GaussianBlur(BufferedImageOp opHor, BufferedImageOp opVer, RenderingHints renderingHints) {
        this.opHor = opHor;
        this.opVer = opVer;
        this.renderingHints = renderingHints;
    }

    public BufferedImage filter(BufferedImage image) {
        if (opHor != null) {
            image = opHor.filter(image, null);
        }
        if (opVer != null) {
            image = opVer.filter(image, null);
        }
        return image;
    }

    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        if (opHor != null && opVer != null) {
            BufferedImage image = opHor.filter(src, null);
            return opVer.filter(image, dest);
        } else if (opVer != null) {
            return opVer.filter(src, dest);
        } else if (opHor != null) {
            return opHor.filter(src, dest);
        } else {
            if (dest == null) {
                dest = createCompatibleDestImage(src, null);
            }
            src.copyData(dest.getRaster());
            return dest;
        }
    }

    @Override
    public Rectangle2D getBounds2D(BufferedImage src) {
        if (opHor != null && opVer != null) {
            return opHor.getBounds2D(src).createIntersection(opVer.getBounds2D(src));
        } else if (opVer != null) {
            return opVer.getBounds2D(src);
        } else if (opHor != null) {
            return opHor.getBounds2D(src);
        } else
            return new Rectangle2D.Float(src.getMinX(), src.getMinY(), src.getWidth(), src.getHeight());
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        if (destCM == null)
            destCM = src.getColorModel();
        WritableRaster raster = src.getRaster().createCompatibleWritableRaster();
        Hashtable<String, Object> properties = new Hashtable<>();
        for (String name: src.getPropertyNames()) {
            properties.put(name, src.getProperty(name));
        }
        return new BufferedImage(destCM, raster, src.isAlphaPremultiplied(), properties);
    }

    @Override
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null)
            dstPt = new Point2D.Double();
        dstPt.setLocation(srcPt);
        return dstPt;
    }

    @Override
    public RenderingHints getRenderingHints() {
        return renderingHints;
    }
}

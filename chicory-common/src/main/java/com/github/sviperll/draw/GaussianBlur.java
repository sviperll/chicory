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

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class GaussianBlur {
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
        return new GaussianBlur(opHor, opVer);
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

    private GaussianBlur(BufferedImageOp opHor, BufferedImageOp opVer) {
        this.opHor = opHor;
        this.opVer = opVer;
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
}

/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.cli;

import com.github.sviperll.Property;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class CLIHandlers {
    public static CLIFlagHandler booleanHandler(final Property<Boolean> property) {
        return new CLIFlagHandler() {
            @Override
            public void handleCLIFlag() {
                property.set(true);
            }
        };
    }

    public static CLIParameterHandler charset(final Property<Charset> property) {
        return new CLIParameterHandler() {
            @Override
            public void handleCLIParameter(String param) throws CLIParameterFormatException {
                try {
                    property.set(Charset.forName(param));
                } catch (IllegalCharsetNameException ex) {
                    throw new CLIParameterFormatException("Argument is not one of supported charecter sets: " + param, ex);
                } catch (UnsupportedCharsetException ex) {
                    throw new CLIParameterFormatException("Argument is not one of supported charecter sets: " + param, ex);
                }
            }
        };
    }

    public static CLIParameterHandler file(final Property<File> property) {
        return new CLIParameterHandler() {
            @Override
            public void handleCLIParameter(String param) throws CLIParameterFormatException {
                property.set(new File(param));
            }
        };
    }

    public static CLIParameterHandler integerPoint2D(final Property<Point2D> property) {
        return new CLIParameterHandler() {
            @Override
            public void handleCLIParameter(String param) throws CLIParameterFormatException {
                String[] fields = param.split("x", -1);
                if (fields.length != 2)
                    throw new CLIParameterFormatException("Size should be <width>x<height>, like 300x150: " + param);
                int width;
                try {
                    width = Integer.parseInt(fields[0]);
                } catch (NumberFormatException ex) {
                    throw new CLIParameterFormatException("Width is not integer number: " + fields[0], ex);
                }
                int height;
                try {
                    height = Integer.parseInt(fields[1]);
                } catch (NumberFormatException ex) {
                    throw new CLIParameterFormatException("Height is not integer number: " + fields[1], ex);
                }
                property.set(new Point(width, height));
            }
        };
    }

    public static CLIParameterHandler integer(final Property<Integer> property) {
        return new CLIParameterHandler() {
            @Override
            public void handleCLIParameter(String param) throws CLIParameterFormatException {
                try {
                    property.set(Integer.parseInt(param));
                } catch (NumberFormatException ex) {
                    throw new CLIParameterFormatException("Wrong format: " + param, ex);
                }
            }
        };
    }

    public static CLIParameterHandler stringArray(final Property<String[]> property) {
        return new CLIParameterHandler() {
            @Override
            public void handleCLIParameter(String param) throws CLIParameterFormatException {
                property.set(param.split(":"));
            }
        };
    }

    public static CLIParameterHandler string(final Property<String> property) {
        return new CLIParameterHandler() {
            @Override
            public void handleCLIParameter(String param) throws CLIParameterFormatException {
                property.set(param);
            }
        };
    }

    public static CLIParameterHandler url(final Property<URL> property) {
        return new CLIParameterHandler() {
            @Override
            public void handleCLIParameter(String param) throws CLIParameterFormatException {
                try {
                    property.set(new URL(param));
                } catch (MalformedURLException ex) {
                    throw new CLIParameterFormatException("Argument is not well-formed URL: " + param, ex);
                }
            }
        };
    }

    private CLIHandlers() {
    }
}

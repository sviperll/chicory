/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.cli;

import com.github.sviperll.Property;
import com.github.sviperll.Strings;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Utility class that provides commonly used handlers for command line arguments
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class CLIHandlers {
    /**
     * Handler sets `property` to `true` if flag is present in command line arguments
     *
     * @param property property to set
     * @return instance of a handler
     */
    public static CLIFlagHandler booleanHandler(final Property<Boolean> property) {
        return new CLIFlagHandler() {
            @Override
            public void handleCLIFlag() {
                property.set(true);
            }
        };
    }

    /**
     * Handler sets `property` to charset with a name specified in command line
     *
     * @param property property to set
     * @return instance of a handler
     */
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

            @Override
            public String getDefaultValue() {
                Charset charset = property.get();
                return charset == null ? null : charset.name();
            }
        };
    }

    /**
     * Handler sets `property` to file with a name specified in command line
     *
     * @param property property to set
     * @return instance of a handler
     */
    public static CLIParameterHandler file(final Property<File> property) {
        return new CLIParameterHandler() {
            @Override
            public void handleCLIParameter(String param) throws CLIParameterFormatException {
                property.set(new File(param));
            }

            @Override
            public String getDefaultValue() {
                File file = property.get();
                return file == null ? null : file.getPath();
            }
        };
    }

    /**
     * Handler sets `property` to a point with coordinates specified in command line.
     *
     * Format of command line argument should be /xxx/`x`/yyy/
     *
     * @param property property to set
     * @return instance of a handler
     */
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

            @Override
            public String getDefaultValue() {
                Point2D point = property.get();
                return point == null ? null : Math.round(point.getX()) + "x" + Math.round(point.getY());
            }
        };
    }

    /**
     * Handler sets `property` to integer number specified in command line
     *
     * @param property property to set
     * @return instance of a handler
     */
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

            @Override
            public String getDefaultValue() {
                Integer integer = property.get();
                return integer == null ? null : integer.toString();
            }
        };
    }

    /**
     * Handler sets `property` to an array of strings specified in command line
     *
     * Format of strings specified in command line shoult be
     *
     * /string1/`:`/string2/`:`/.../`:`/stringN/
     *
     * @param property property to set
     * @return instance of a handler
     */
    public static CLIParameterHandler stringArray(final Property<String[]> property) {
        return new CLIParameterHandler() {
            @Override
            public void handleCLIParameter(String param) throws CLIParameterFormatException {
                property.set(param.split(":"));
            }

            @Override
            public String getDefaultValue() {
                String[] strings = property.get();
                return strings == null ? null : Strings.join(strings, ":");
            }
        };
    }

    /**
     * Handler sets `property` to a string specified in command line
     *
     * @param property property to set
     * @return instance of a handler
     */
    public static CLIParameterHandler string(final Property<String> property) {
        return new CLIParameterHandler() {
            @Override
            public void handleCLIParameter(String param) throws CLIParameterFormatException {
                property.set(param);
            }

            @Override
            public String getDefaultValue() {
                return property.get();
            }
        };
    }

    /**
     * Handler sets `property` to a URL specified in command line
     *
     * @param property property to set
     * @return instance of a handler
     */
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

            @Override
            public String getDefaultValue() {
                URL url = property.get();
                return url == null ? null : url.toString();
            }
        };
    }

    private CLIHandlers() {
    }
}

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
package com.github.sviperll.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CLISpecification {
    private final Map<String, String> usage = new TreeMap<>();
    private final Map<Character, CLIFlagHandler> shortFlagHandlers = new TreeMap<>();
    private final Map<String, CLIFlagHandler> longFlagHandlers = new TreeMap<>();
    private final Map<Character, CLIParameterHandler> shortParameterHandlers = new TreeMap<>();
    private final Map<String, CLIParameterHandler> longParameterHandlers = new TreeMap<>();
    private final Appendable usageWriter;

    public CLISpecification(Appendable usageWriter) {
        this.usageWriter = usageWriter;
    }

    public void add(char c, String s, String description, CLIFlagHandler handler) {
        addFlagUsageEntry(c, description);
        addFlagUsageEntry(s, description);
        shortFlagHandlers.put(c, handler);
        longFlagHandlers.put(s, handler);
    }
    public void add(String s, String description, CLIFlagHandler handler) {
        addFlagUsageEntry(s, description);
        longFlagHandlers.put(s, handler);
    }
    public void add(char c, String s, String description, CLIParameterHandler handler) {
        addParameterUsageEntry(c, description);
        addParameterUsageEntry(s, description);
        shortParameterHandlers.put(c, handler);
        longParameterHandlers.put(s, handler);
    }
    public void add(String s, String description, CLIParameterHandler handler) {
        addParameterUsageEntry(s, description);
        longParameterHandlers.put(s, handler);
    }

    private void addFlagUsageEntry(char c, String description) {
        String s = new String(new char[] {c});
        if (usage.put(s, "\t\t -" + s + "\t\t" + description) != null)
            throw new IllegalArgumentException("Option " + s + " already defined!");
    }

    private void addFlagUsageEntry(String s, String description) {
        if (usage.put(s, "\t\t--" + s + "\t\t" + description) != null)
            throw new IllegalArgumentException("Option " + s + " already defined!");
    }

    private void addParameterUsageEntry(char c, String description) {
        String s = new String(new char[] {c});
        if (usage.put(s, "\t\t -" + s + " OPTION\t" + description) != null)
            throw new IllegalArgumentException("Option " + s + " already defined!");
    }

    private void addParameterUsageEntry(String s, String description) {
        if (usage.put(s, "\t\t--" + s + "=OPTION\t" + description) != null)
            throw new IllegalArgumentException("Option " + s + " already defined!");
    }

    public String[] run(String[] args) throws CLIException {
        List<String> unprocessed = new ArrayList<>();
        Processor p = new Processor(new Parser(args));
        for (;;) {
            if (p.isEndOfArgs()) {
                break;
            } else if (p.isSingleDash()) {
                unprocessed.add(p.current());
                p.next();
            } else if (p.isLongOption()) {
                p.processLongOption();
            } else if (p.isShortOptions()) {
                p.processShortOptions();
            } else {
                unprocessed.add(p.current());
                p.next();
            }
        }
        String[] res = new String[unprocessed.size()];
        return unprocessed.toArray(res);
    }

    public void usage() throws IOException {
        for (Map.Entry<String, String> e: usage.entrySet()) {
            usageWriter.append(e.getValue());
            usageWriter.append("\n");
        }
    }

    private class Processor {
        private final Parser parser;
        Processor(Parser parser) {
            this.parser = parser;
        }

        private boolean isEndOfArgs() {
            return parser.isEndOfArgs();
        }

        private boolean isSingleDash() throws CLIException {
            return current().equals("-");
        }

        private String current() throws CLIException {
            return parser.current();
        }

        private void next() {
            parser.next();
        }

        private boolean isLongOption() throws CLIException {
            return current().startsWith("--");
        }

        private boolean isShortOptions() throws CLIException {
            return current().startsWith("-") && !isLongOption();
        }

        private void processLongOption() throws CLIException {
            for (Map.Entry<String, CLIFlagHandler> e: longFlagHandlers.entrySet()) {
                if (parser.current().equals("--" + e.getKey())) {
                    e.getValue().handleCLIFlag();
                    parser.next();
                    return;
                }
            }
            for (Map.Entry<String, CLIParameterHandler> e: longParameterHandlers.entrySet()) {
                String option = "--" + e.getKey();
                String prefix = option + "=";
                if (parser.current().startsWith(prefix)) {
                    try {
                        e.getValue().handleCLIParameter(parser.current().substring(prefix.length()));
                    } catch (CLIParameterFormatException ex) {
                        throw new CLIException("Error reading option: " + option + ": " + ex.getMessage(), ex);
                    }
                    parser.next();
                    return;
                } else if (parser.current().equals(option)) {
                    parser.next();
                    try {
                        e.getValue().handleCLIParameter(parser.readParameter(e.getKey()));
                    } catch (CLIParameterFormatException ex) {
                        throw new CLIException("Error reading option: " + option + ": " + ex.getMessage(), ex);
                    }
                    return;
                }
            }
            throw new CLIException("Unknown option " + parser.current());
        }

        private void processShortOptions() throws CLIException {
            char[] c = parser.current().toCharArray();
            Map.Entry<Character, CLIParameterHandler> handler = null;
            for (int i = 1; i < c.length; i++) {
                boolean found = false;
                for (Map.Entry<Character, CLIFlagHandler> e: shortFlagHandlers.entrySet()) {
                    if (e.getKey().equals(c[i])) {
                        e.getValue().handleCLIFlag();
                        found = true;
                    }
                }
                for (Map.Entry<Character, CLIParameterHandler> e: shortParameterHandlers.entrySet()) {
                    if (e.getKey().equals(c[i])) {
                        if (handler != null)
                            throw new CLIException("Expecting parameter for -" + handler.getKey());
                        else
                            handler = e;
                        found = true;
                    }
                }
                if (!found)
                    throw new CLIException("Unknown option -" + c[i]);
            }
            if (handler != null) {
                parser.next();
                try {
                    handler.getValue().handleCLIParameter(parser.readParameter(handler.getKey()));
                } catch (CLIParameterFormatException ex) {
                    throw new CLIException("Error reading option: -" + handler.getKey() + ": " + ex.getMessage(), ex);
                }
            }
        }
    }

    private class Parser {

        private final String[] args;
        private int i;

        public Parser(String[] args) {
            this.args = args;
            i = 0;
        }

        public boolean isEndOfArgs() {
            return i == args.length;
        }

        public String current() throws CLIException {
            if (!(i < args.length))
                throw new CLIException("Expecting argument, got end of line");
            return args[i];
        }

        public String readParameter(char option) throws CLIException {
            if (!current().equals("-") && current().startsWith("-"))
                throw new CLIException("Expecting parameter for -" + option);
            String res = current();
            next();
            return res;
        }

        public String readParameter(String option) throws CLIException {
            if (!current().equals("-") && current().startsWith("-"))
                throw new CLIException("Expecting parameter for --" + option);
            String res = current();
            next();
            return res;
        }

        public void next() {
            i++;
        }
    }
}

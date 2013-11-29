/*
 * Copyright (C) 2013 Victor Nazarov <asviraspossible@gmail.com>
 */

package com.github.sviperll;

import java.lang.management.ManagementFactory;

public class JVM {
    public static int getPID() {
        return Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
    }

    private JVM() {
    }
}

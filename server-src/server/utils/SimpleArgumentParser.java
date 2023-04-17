/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class SimpleArgumentParser {
    private final HashMap<String, String> assignedOptions = new HashMap();
    private final HashSet<String> flagOptions = new HashSet();
    private final HashSet<String> unknownOptions = new HashSet();

    public SimpleArgumentParser(String[] args, Set<String> allowedOptions) {
        for (String arg : args) {
            if ((arg = arg.trim()).isEmpty() || arg.contains("WurmServerLauncher")) continue;
            int assignmentIdx = arg.indexOf(61);
            if (assignmentIdx > 0) {
                String option = arg.substring(0, assignmentIdx).toLowerCase(Locale.ENGLISH);
                if (!allowedOptions.contains(option)) {
                    System.err.println("Unknown parameter: " + option);
                    continue;
                }
                if (assignmentIdx >= arg.length()) {
                    this.assignedOptions.put(option, "");
                    continue;
                }
                this.assignedOptions.put(option, arg.substring(assignmentIdx + 1));
                continue;
            }
            if (allowedOptions.contains(arg)) {
                this.flagOptions.add(arg.toLowerCase(Locale.ENGLISH));
                continue;
            }
            System.err.println("Unknown parameter: " + arg);
            this.unknownOptions.add(arg);
        }
    }

    public boolean hasOption(String option) {
        return this.flagOptions.contains(option) || this.assignedOptions.containsKey(option);
    }

    public boolean hasFlag(String option) {
        return this.flagOptions.contains(option);
    }

    public String getOptionValue(String option) {
        return this.assignedOptions.get(option);
    }

    public boolean hasUnknownOptions() {
        return !this.unknownOptions.isEmpty();
    }
}


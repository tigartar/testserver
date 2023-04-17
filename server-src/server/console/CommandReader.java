/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.console;

import com.wurmonline.server.Server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandReader
implements Runnable {
    private static final Logger logger = Logger.getLogger(CommandReader.class.getName());
    private final Server server;
    private final InputStream inputStream;
    private static final String SHUTDOWN = "shutdown";

    public CommandReader(Server server, InputStream inputStream) {
        this.server = server;
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        String nextLine;
        logger.info("Starting command reader for console input");
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(this.inputStream));
        do {
            try {
                nextLine = consoleReader.readLine();
                if (nextLine == null) break;
                if (nextLine.equals(SHUTDOWN)) {
                    this.server.shutDown();
                    break;
                }
                logger.warning("Unknown command: " + nextLine);
            }
            catch (IOException e) {
                logger.log(Level.SEVERE, "Can't read from console", e);
                nextLine = null;
            }
        } while (nextLine != null);
        logger.info("Console reader exiting.");
    }
}


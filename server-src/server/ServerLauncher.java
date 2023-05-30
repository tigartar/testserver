package com.wurmonline.server;

import com.wurmonline.server.console.CommandReader;
import com.wurmonline.server.items.CreationEntryCreator;
import com.wurmonline.server.utils.SimpleArgumentParser;
import com.wurmonline.server.webinterface.RegistryStarter;
import com.wurmonline.server.webinterface.WebInterfaceImpl;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.AlreadyBoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class ServerLauncher {
   private static final int MAX_BYTES_PER_LOGFILE = 10240000;
   private static final int NUM_LOG_FILES = 200;
   Server server;
   private static final Map<String, String> SPECIALIST_LOGGER_FILES;
   private static final Map<String, SimpleFormatter> SPECIALIST_LOGGER_FORMATS;
   private static final Map<String, Logger> SPECIALIST_LOGGERS = new HashMap<>(SPECIALIST_LOGGER_FILES.size());
   boolean started = false;
   private static final String ARG_PLAYER_SERVER = "ps";
   private static final Set<String> ALLOWED_ARGUMENTS;

   public ServerLauncher() {
      createLoggers();
   }

   public final Server getServer() {
      return this.server;
   }

   public final boolean wasStarted() {
      return this.started;
   }

   public void runServer() throws IOException {
      this.runServer(false, false);
   }

   public void runServer(boolean ps, boolean isOfflineServer) throws IOException {
      this.server = Server.getInstance();
      this.server.setIsPS(ps);
      this.server.steamHandler.setIsOfflienServer(isOfflineServer);

      try {
         this.server.startRunning();
         this.started = true;
      } catch (Exception var5) {
         System.out.println(var5.getMessage());
         var5.printStackTrace();
         this.server.shutDown("Problem running the server - " + var5.getMessage(), var5);
      }

      CreationEntryCreator.createCreationEntries();
      if (Constants.useIncomingRMI) {
         try {
            InetAddress byAddress = InetAddress.getByAddress(this.server.getInternalIp());
            RegistryStarter.startRegistry(new WebInterfaceImpl(Servers.localServer.REGISTRATION_PORT), byAddress, Servers.localServer.RMI_PORT);
            System.out.println("RMI listening on " + byAddress + ':' + Servers.localServer.RMI_PORT);
            System.out.println("RMI Registry listening on " + byAddress + ':' + Servers.localServer.REGISTRATION_PORT);
         } catch (AlreadyBoundException var4) {
            System.out.println("The port " + Servers.localServer.RMI_PORT + " is already bound./n Registry RMI communication won't work.");
         }
      } else {
         System.out.println("Incoming RMI is disabled");
      }
   }

   public static final void stopLoggers() {
      Logger logger = Logger.getLogger("com.wurmonline");
      if (logger != null) {
         removeLoggerHandlers(logger);
      }

      for(String loggerName : SPECIALIST_LOGGERS.keySet()) {
         logger = Logger.getLogger(loggerName);
         if (logger != null) {
            removeLoggerHandlers(logger);
         }
      }
   }

   public static final void createLoggers() {
      Logger logger = Logger.getLogger("com.wurmonline");
      String loggingProperty = System.getProperty("java.util.logging.config.file", null);
      if (loggingProperty == null) {
         System.out.println("java.util.logging.config.file system property is not set so hardcoding logging");
         logger.setUseParentHandlers(false);
         Handler[] h = logger.getHandlers();
         System.out.println("com.wurmonline logger handlers: " + Arrays.toString((Object[])h));

         for(int i = 0; i != h.length; ++i) {
            logger.removeHandler(h[i]);
         }

         try {
            String logsPath = createLogPath();
            FileHandler fh = new FileHandler(logsPath + "wurm.log", 10240000, 200, true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            if (Constants.devmode) {
               logger.addHandler(new ConsoleHandler());
            }
         } catch (IOException var5) {
            System.err.println("no redirection possible, stopping server");
            System.exit(1);
         }
      } else {
         System.out.println("java.util.logging.config.file system property is set to " + loggingProperty);
         System.out.println("com.wurmonline logger level: " + logger.getLevel());
         System.out.println("com.wurmonline logger UseParentHandlers: " + logger.getUseParentHandlers());
      }

      logger.log(
         Level.OFF,
         "\n----------------------------------------------------------------\nWurm Server logging started at "
            + new Date()
            + "\n----------------------------------------------------------------"
      );

      for(String loggerName : SPECIALIST_LOGGER_FILES.keySet()) {
         SPECIALIST_LOGGERS.put(loggerName, createLoggerFileHandlers(loggerName, SPECIALIST_LOGGER_FILES.get(loggerName)));
      }
   }

   private static void removeLoggerHandlers(Logger logger) {
      for(Handler handler : logger.getHandlers()) {
         if (handler != null) {
            try {
               handler.flush();
            } catch (Exception var7) {
            }

            try {
               handler.close();
            } catch (Exception var6) {
            }

            logger.removeHandler(handler);
         }
      }
   }

   private static Logger createLoggerFileHandlers(String loggerName, String logFileName) {
      Logger logger = Logger.getLogger(loggerName);
      logger.setUseParentHandlers(false);
      removeLoggerHandlers(logger);

      try {
         String logsPath = createLogPath();
         FileHandler fh = new FileHandler(logsPath + logFileName, 10240000, 200, true);
         fh.setFormatter(new SimpleFormatter());
         if (SPECIALIST_LOGGER_FORMATS.containsKey(loggerName)) {
            fh.setFormatter(SPECIALIST_LOGGER_FORMATS.get(loggerName));
         }

         logger.addHandler(fh);
         if (Constants.devmode) {
            logger.addHandler(new ConsoleHandler());
         }
      } catch (IOException var5) {
         System.err.println("no redirection possible, stopping server");
         System.exit(1);
      }

      return logger;
   }

   static String getServerStartBanner() {
      StringBuilder lBuilder = new StringBuilder(1024);
      lBuilder.append("\n========================================================================================================\n\n");
      lBuilder.append("888       888                                     .d8888b. \n");
      lBuilder.append("888   o   888                                     d88P  Y88b  \n");
      lBuilder.append("888  d8b  888                                     Y88b.  \n");
      lBuilder.append("888 d888b 888 888  888 888d888 88888b.d88b.        'Y888b.    .d88b.  888d888 888  888  .d88b.  888d888 \n");
      lBuilder.append("888d88888b888 888  888 888P   888 '888 '88b          'Y88b. d8P  Y8b 888P'   888  888 d8P  Y8b 888P'   \n");
      lBuilder.append("88888P Y88888 888  888 888     888  888  888            '888 88888888 888     Y88  88P 88888888 888    \n");
      lBuilder.append("8888P   Y8888 Y88b 888 888     888  888  888      Y88b  d88P Y8b.     888      Y8bd8P  Y8b.     888   \n");
      lBuilder.append("888P     Y888  'Y88888 888     888  888  888       'Y8888P'   'Y8888  888       Y88P    'Y8888  888 \n");
      lBuilder.append("\n========================================================================================================\n");
      return lBuilder.toString();
   }

   public static void main(String[] args) throws IOException {
      try {
         SimpleArgumentParser parser = new SimpleArgumentParser(args, ALLOWED_ARGUMENTS);
         if (parser.hasUnknownOptions()) {
            System.exit(1);
         }

         boolean isPs = parser.hasFlag("ps");
         Runtime lRuntime = Runtime.getRuntime();
         System.out.println(getServerStartBanner());
         System.out.println("Wurm Server application started at " + new Date());
         System.out
            .println(
               "Operating system: "
                  + System.getProperty("os.name")
                  + " (arch: "
                  + System.getProperty("os.arch")
                  + ", version: "
                  + System.getProperty("os.version")
                  + ")"
            );
         System.out.println("Java version: " + System.getProperty("java.version"));
         System.out.println("Java home: " + System.getProperty("java.home"));
         System.out.println("Java vendor: " + System.getProperty("java.vendor") + " (" + System.getProperty("java.vendor.url") + ")");
         System.out.println("Available CPUs: " + lRuntime.availableProcessors());
         System.out.println("Java Classpath: " + System.getProperty("java.class.path"));
         System.out
            .println(
               "Free memory: "
                  + lRuntime.freeMemory() / 1048576L
                  + " MB, Total memory: "
                  + lRuntime.totalMemory() / 1048576L
                  + " MB, Max memory: "
                  + lRuntime.maxMemory() / 1048576L
                  + " MB"
            );
         System.out.println("\n==================================================================\n");
         ServerLauncher lServerLauncher = new ServerLauncher();
         lServerLauncher.runServer(isPs, false);
         new Thread(new CommandReader(lServerLauncher.getServer(), System.in), "Console Command Reader").start();
      } catch (Exception var8) {
         throw var8;
      } finally {
         System.out.println("\n==================================================================\n");
         System.out.println("Wurm Server launcher finished at " + new Date());
         System.out.println("\n==================================================================\n");
      }
   }

   public static String createLogPath() {
      String logsPath = Constants.dbHost + "/Logs/";
      File newDirectory = new File(logsPath);
      if (!newDirectory.exists()) {
         newDirectory.mkdirs();
      }

      return logsPath;
   }

   static {
      HashMap<String, String> specialistLoggers = new HashMap<>();
      specialistLoggers.put("Cheaters", "cheaters.log");
      specialistLoggers.put("Money", "money.log");
      specialistLoggers.put("Chat", "chat.log");
      specialistLoggers.put("ca-help", "ca-help.log");
      specialistLoggers.put("IntraServer", "IntraServer.log");
      specialistLoggers.put("Reimbursements", "reimbursements.log");
      specialistLoggers.put("stacktraces", "stacktraces.log");
      specialistLoggers.put("deletions", "deletions.log");
      specialistLoggers.put("ItemDebug", "item-debug.log");
      specialistLoggers.put("affinities", "affinities.log");
      SPECIALIST_LOGGER_FILES = Collections.unmodifiableMap(specialistLoggers);
      SimpleFormatter chatFormat = new SimpleFormatter() {
         private static final String format = "[%1$tF %1$tT] %2$s %n";

         @Override
         public synchronized String format(LogRecord record) {
            return String.format("[%1$tF %1$tT] %2$s %n", new Date(record.getMillis()), record.getMessage());
         }
      };
      HashMap<String, SimpleFormatter> specialistFormatters = new HashMap<>();
      specialistFormatters.put("ca-help", chatFormat);
      specialistFormatters.put("Chat", chatFormat);
      SPECIALIST_LOGGER_FORMATS = Collections.unmodifiableMap(specialistFormatters);
      HashSet<String> allowedArguments = new HashSet<>();
      allowedArguments.add("ps");
      ALLOWED_ARGUMENTS = Collections.unmodifiableSet(allowedArguments);
   }
}

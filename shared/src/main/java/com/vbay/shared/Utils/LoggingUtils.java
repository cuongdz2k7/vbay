package com.vbay.shared.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class LoggingUtils {
    private static final Object LOCK = new Object();
    private static volatile boolean configured = false;

    private LoggingUtils() {
    }

    public static void configure(String applicationName) {
        synchronized (LOCK) {
            if (configured) {
                return;
            }

            try {
                //Create folder : "logs"
                Path logDirectory = Path.of("logs");
                Files.createDirectories(logDirectory);

                //Identify :"root Logger"
                Logger rootLogger = Logger.getLogger("");
                rootLogger.setLevel(Level.INFO);
                //Remove Java auto loggings 
                for (Handler handler : rootLogger.getHandlers()) {
                    rootLogger.removeHandler(handler);
                    handler.close();
                }

                Formatter formatter = new PlainTextFormatter();
                //Console (Level : INFOR)
                ConsoleHandler consoleHandler = new ConsoleHandler();
                consoleHandler.setLevel(Level.INFO);
                consoleHandler.setFormatter(formatter);
                rootLogger.addHandler(consoleHandler);
                //Files
                FileHandler fileHandler = new FileHandler(
                    logDirectory.resolve(applicationName + ".log").toString(),
                    true
                ); //--> Distribute to client.logs or server.logs
                fileHandler.setLevel(Level.ALL); //
                fileHandler.setFormatter(formatter);
                rootLogger.addHandler(fileHandler);

                configured = true;
                Logger.getLogger(LoggingUtils.class.getName())
                    .info(() -> "Logging initialized for " + applicationName + " at " + logDirectory.toAbsolutePath());
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to configure logging for " + applicationName, exception);
            }
        }
    }

    public static Logger getLogger(Class<?> type) {
        return Logger.getLogger(type.getName());
    }

    private static final class PlainTextFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder();
            builder.append(java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC))
                .append(" ")
                .append(record.getLevel().getName())
                .append(" [")
                .append(record.getLoggerName())
                .append("] ")
                .append(formatMessage(record))
                .append(System.lineSeparator());

            if (record.getThrown() != null) {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                record.getThrown().printStackTrace(printWriter);
                printWriter.flush();
                builder.append(stringWriter);
            }

            return builder.toString();
        }
    }
}

package net.zetaeta.remoteconsole.plugin;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class ConsoleLogFormatter extends Formatter {
    private SimpleDateFormat a = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    private java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\x1B\\[([0-9]{1,2}(;[0-9]{1,2})?)?[m|K]");

    public ConsoleLogFormatter() {
    }

    public String format(LogRecord logrecord) {
        StringBuilder stringbuilder = new StringBuilder();

        stringbuilder.append(this.a.format(Long.valueOf(logrecord.getMillis())));
        Level level = logrecord.getLevel();

        if (level == Level.FINEST) {
            stringbuilder.append(" [FINEST] ");
        } else if (level == Level.FINER) {
            stringbuilder.append(" [FINER] ");
        } else if (level == Level.FINE) {
            stringbuilder.append(" [FINE] ");
        } else if (level == Level.INFO) {
            stringbuilder.append(" [INFO] ");
        } else if (level == Level.WARNING) {
            stringbuilder.append(" [WARNING] ");
        } else if (level == Level.SEVERE) {
            stringbuilder.append(" [SEVERE] ");
        } else if (level == Level.SEVERE) {
            stringbuilder.append(" [" + level.getLocalizedName() + "] ");
        }

        stringbuilder.append(logrecord.getMessage());
        stringbuilder.append('\n');
        Throwable throwable = logrecord.getThrown();

        if (throwable != null) {
            StringWriter stringwriter = new StringWriter();

            throwable.printStackTrace(new PrintWriter(stringwriter));
            stringbuilder.append(stringwriter.toString());
        }

        // CraftBukkit start - handle stripping color
//        if (this.strip) {
//            return this.pattern.matcher(stringbuilder.toString()).replaceAll("");
//        } else {
        return stringbuilder.toString();
//        }
        // CraftBukkit end
    }
}

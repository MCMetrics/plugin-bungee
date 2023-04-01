package me.kicksquare.mcmbungee;

import io.sentry.Sentry;
import me.kicksquare.mcmbungee.util.LoggerUtil;

public class SentryExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        boolean isFromThisPlugin = false;
        for (StackTraceElement element : e.getStackTrace()) {
            //todo change this package name
            if (element.getClassName().contains("me.kicksquare.mcmbungee")) {
                isFromThisPlugin = true;
                break;
            }
        }
        if(isFromThisPlugin) {
            LoggerUtil.severe("Detected an MCMetrics exception. Uploading to sentry.");
            Sentry.captureException(e);
        }
    }
}
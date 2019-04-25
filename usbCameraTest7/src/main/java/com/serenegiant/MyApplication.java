package com.serenegiant;

import android.app.Application;
import android.os.Environment;

import com.serenegiant.usbcameratest7.R;
import com.serenegiant.usbcameratest7.xlog.LogConfiguration;
import com.serenegiant.usbcameratest7.xlog.LogLevel;
import com.serenegiant.usbcameratest7.xlog.XLog;
import com.serenegiant.usbcameratest7.xlog.flattener.ClassicFlattener;
import com.serenegiant.usbcameratest7.xlog.interceptor.BlacklistTagsFilterInterceptor;
import com.serenegiant.usbcameratest7.xlog.printer.AndroidPrinter;
import com.serenegiant.usbcameratest7.xlog.printer.Printer;
import com.serenegiant.usbcameratest7.xlog.printer.file.FilePrinter;
import com.serenegiant.usbcameratest7.xlog.printer.file.naming.DateFileNameGenerator;

import java.io.File;

public class MyApplication extends Application {

    public static Printer globalFilePrinter;

    @Override
    public void onCreate() {
        super.onCreate();

        initXlog();
    }

    /**
     * Initialize XLog.
     */
    private void initXlog() {
        LogConfiguration config = new LogConfiguration.Builder()
                .logLevel(true ? LogLevel.ALL             // Specify log level, logs below this level won't be printed, default: LogLevel.ALL
                        : LogLevel.NONE)
                .tag(getString(R.string.global_tag))
                .addInterceptor(new BlacklistTagsFilterInterceptor(
                        "blacklist1", "blacklist2", "blacklist3"))
                .build();

        Printer androidPrinter = new AndroidPrinter();             // Printer that print the log using android.util.Log
        Printer filePrinter = new FilePrinter                      // Printer that print the log to the file system
                .Builder(new File(Environment.getExternalStorageDirectory(), "xlogsample").getPath())       // Specify the path to save log file
                .fileNameGenerator(new DateFileNameGenerator())        // Default: ChangelessFileNameGenerator("log")
                .flattener(new ClassicFlattener())                     // Default: DefaultFlattener
                .build();

        XLog.init(config, androidPrinter, filePrinter);

        // For future usage: partial usage in MainActivity.
        globalFilePrinter = filePrinter;
    }
}

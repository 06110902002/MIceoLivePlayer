package come.live.decodelib.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Formatter;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;


/**
 * Author:      hengyang
 * CreateDate:  2020/10/19 10:30 AM
 * Version:     1.0
 * Description: Log工具，类似android.util.Log。 tag自动产生，格式:
 * customTagPrefix:className.methodName(Line:lineNumber),
 * customTagPrefix为空时只输出：className.methodName(Line:lineNumber)。
 */
public class LogUtils {

    public static String customTagPrefix = "-------";
    private static boolean isSaveLog = true;
    private static final String DEFAULT_MSG = " ";
    private static final String PATH_LOG_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/cloudgame/games/log";
    private static int CG_DEBUG = -1;

    private LogUtils() {
    }


    @SuppressLint("DefaultLocale")
    private static String generateTag(StackTraceElement caller) {
        String tag = "%s.%s(Line:%d)"; // 占位符
        String callerClazzName = caller.getClassName(); // 获取到类名
        callerClazzName = callerClazzName.substring(callerClazzName
                .lastIndexOf(".") + 1);
        tag = String.format(tag, callerClazzName, caller.getMethodName(),caller.getLineNumber());
        tag = TextUtils.isEmpty(customTagPrefix) ? tag : customTagPrefix + ":"
                + tag;
        return tag;
    }
    public static boolean isDebug() {
        if (CG_DEBUG == -1) {
            try {
                // TODO: change to 0 before released
                String type = getSystemProperty("debug.cloudgame.agent", "1");
                CG_DEBUG = Integer.parseInt(type);
            } catch (Throwable e) {
                CG_DEBUG = 0;
            }
        }
        return CG_DEBUG > 0;
    }

    public static String getSystemProperty(String key, String defaultValue) {
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            Method m = systemProperties.getMethod("get", String.class, String.class);
            String result = (String)m.invoke((Object)null, key, defaultValue);
            return result;
        } catch (Exception var5) {
            d("fail to getSystemProperty: " + key);
            return defaultValue;
        }
    }

    /**
     * 自定义的logger
     */
    public static CustomLogger customLogger;

    public interface CustomLogger {
        void d(String tag, String content);

        void d(String tag, String content, Throwable tr);

        void e(String tag, String content);

        void e(String tag, String content, Throwable tr);

        void i(String tag, String content);

        void i(String tag, String content, Throwable tr);

        void v(String tag, String content);

        void v(String tag, String content, Throwable tr);

        void w(String tag, String content);

        void w(String tag, String content, Throwable tr);

        void w(String tag, Throwable tr);

        void wtf(String tag, String content);

        void wtf(String tag, String content, Throwable tr);

        void wtf(String tag, Throwable tr);
    }

    public static void d(String content) {
        if (!isDebug()){
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);

        if (customLogger != null) {
            customLogger.d(tag, content);
        } else {
            Log.d(tag, content);
        }
        //if (isSaveLog) {
        //    startSaveLogTask(caller,content);
        //}
    }

    public static void dLong(String tag, String msg) {
        if (!isDebug()){
            return;
        }
        if (msg != null) {
            int urlLength = msg.length();

            int i;
            for(i = 0; i < urlLength - 2048; i += 2048) {
                String temp1 = msg.substring(i, i + 2048);
                if (isDebug()) {
                    Log.d(tag, tag + "~" + temp1);
                } else {
                    if (TextUtils.isEmpty(temp1)){
                        temp1 = DEFAULT_MSG;
                    }
                }
            }

            if (isDebug()) {
                Log.d(tag, tag + "~" + msg.substring(i));
            } else {
                String printMsg = msg.substring(i);
                if (TextUtils.isEmpty(printMsg)){
                    printMsg = DEFAULT_MSG;
                }
            }
        }
    }


    public static void d(String content, Throwable tr) {
        if (!isDebug()){
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);

        if (customLogger != null) {
            customLogger.d(tag, content, tr);
        } else {
            Log.d(tag, content, tr);
        }
        //if (isSaveLog) {
        //    startSaveLogTask(caller,content);
        //}
    }

    public static void e(String content) {
        if (!isDebug()){
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);

        if (customLogger != null) {
            customLogger.e(tag, content);
        } else {
            Log.e(tag, content);
        }
        //if (isSaveLog) {
        //    startSaveLogTask(caller,content);
        //}
    }

    public static void e(String content, Throwable tr) {
        if (!isDebug()){
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);

        if (customLogger != null) {
            customLogger.e(tag, content, tr);
        } else {
            Log.e(tag, content, tr);
        }
        //if (isSaveLog) {
        //    startSaveLogTask(caller,content);
        //}
    }

    public static void i(String content) {
        if (!isDebug()){
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);

        if (customLogger != null) {
            customLogger.i(tag, content);
        } else {
            Log.i(tag, content);
        }

    }

    public static void i(String content, Throwable tr) {
        if (!isDebug()){
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);

        if (customLogger != null) {
            customLogger.i(tag, content, tr);
        } else {
            Log.i(tag, content, tr);
        }

    }

    public static void v(String content) {
        if (!isDebug()){
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);

        if (customLogger != null) {
            customLogger.v(tag, content);
        } else {
            Log.v(tag, content);
        }
        //if (isSaveLog) {
        //    startSaveLogTask(caller,content);
        //}
    }

    public static void v(String content, Throwable tr) {
        if (!isDebug()){
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);

        if (customLogger != null) {
            customLogger.v(tag, content, tr);
        } else {
            Log.v(tag, content, tr);
        }
        //if (isSaveLog) {
        //    startSaveLogTask(caller,content);
        //}
    }

    private static StackTraceElement getCallerStackTraceElement() {
        if(Thread.currentThread().getStackTrace().length >= 5){
            return Thread.currentThread().getStackTrace()[4];
        }else{
            return new StackTraceElement("unkown","method","filename",0);
        }
    }
    /**
     * 根据文件路径 递归创建文件
     *
     * @param file
     */
    public static void createDipPath(String file) {
        String parentFile = file.substring(0, file.lastIndexOf("/"));
        File file1 = new File(file);
        File parent = new File(parentFile);
        if (!file1.exists()) {
            parent.mkdirs();
            try {
                file1.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ReusableFormatter {

        private Formatter formatter;
        private StringBuilder builder;

        public ReusableFormatter() {
            builder = new StringBuilder();
            formatter = new Formatter(builder);
        }

        public String format(String msg, Object... args) {
            formatter.format(msg, args);
            String s = builder.toString();
            builder.setLength(0);
            return s;
        }
    }

    private static final ThreadLocal<ReusableFormatter> threadLocalFormatter = new ThreadLocal<ReusableFormatter>() {
        protected ReusableFormatter initialValue() {
            return new ReusableFormatter();
        }
    };

    public static String format(String msg, Object... args) {
        ReusableFormatter formatter = threadLocalFormatter.get();
        return formatter.format(msg, args);
    }

    public static boolean isSDAva() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)
                || Environment.getExternalStorageDirectory().exists()) {
            return true;
        } else {
            return false;
        }
    }

    //private static SaveLogTask saveLogTask;
    //private static BlockingQueue<BaseDebugLogEntity> logQueue;
    //public static void startSaveLogTask(StackTraceElement stackTraceElement, String content){
    //    if(stackTraceElement == null || TextUtils.isEmpty(content)){
    //        return;
    //    }
    //
    //    BaseDebugLogEntity baseDebugLogEntity = new BaseDebugLogEntity();
    //    baseDebugLogEntity.setClassName(stackTraceElement.getClassName());
    //    baseDebugLogEntity.setMethodName(stackTraceElement.getMethodName());
    //    baseDebugLogEntity.setLineNumber(stackTraceElement.getLineNumber());
    //    baseDebugLogEntity.setContent(content);
    //    String time = DateUtils.getCurDate();
    //    baseDebugLogEntity.setTime(time);
    //    if(logQueue == null){
    //        logQueue = new LinkedBlockingQueue<>(20);
    //    }
    //    if(logQueue.size() >= 20){
    //        saveLogTask = new SaveLogTask();
    //        String logFileName = time + ".txt";
    //        saveLogTask.startSaveLog(logQueue,PATH_LOG_DIR +"/" + logFileName);
    //        AsyncExecutor.execute(saveLogTask);
    //
    //    }else{
    //        logQueue.add(baseDebugLogEntity);
    //    }
    //
    //
    //}

    /**
     * 删除过期日志
     */
    //public static void delExpireLog(){
    //    Calendar calendar = Calendar.getInstance();
    //    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    //    int minute = calendar.get(Calendar.MINUTE);
    //    int seconds = calendar.get(Calendar.SECOND);
    //    if(hour == 2 && minute == 5){
    //        if(seconds > 50){
    //            List<String> allFiles = FileUtils.getFilesByDir(PATH_LOG_DIR);
    //            if(allFiles != null && allFiles.size() > 0){
    //                String curTime = DateUtils.getNowTime("yyyy-MM-dd");
    //                for(int i = 0; i < allFiles.size(); i ++){
    //                    File file = new File(allFiles.get(i));
    //                    String fileModify = DateUtils.transformToData(file.lastModified(),"yyyy-MM-dd");
    //                    int days = DateUtils.daysBetween(fileModify,curTime);
    //                    if(days >= 7){
    //                        FileUtils.deleteFile(file);
    //                    }
    //                }
    //            }
    //        }
    //    }
    //}

}





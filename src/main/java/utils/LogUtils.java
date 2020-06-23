package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by b.bassac on 23/06/2017.
 */
public abstract class LogUtils {
    private static final Logger logger = LoggerFactory.getLogger("BDList");

    public static void info(String s){
        logger.info(s);
    }

    public static void warn(String s){
        logger.warn(s);
    }

    public static void debug(String s){
        logger.debug(s);
    }

    public static void error(Exception e){
        logger.error("Erreur : " + e.toString());
        e.printStackTrace();
        e.getCause().printStackTrace();
    }
}

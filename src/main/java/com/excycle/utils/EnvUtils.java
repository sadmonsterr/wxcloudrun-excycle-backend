package com.excycle.utils;

public class EnvUtils {


    /**
     * 判断是否为非本地开发环境，简单校验，过滤以MainClass启动的本地idea调试
     *
     * @return boolean
     */
    public static boolean isProd() {
        String javaCommand = System.getProperty("sun.java.command");
        return javaCommand == null || javaCommand.endsWith(".jar");
    }

}

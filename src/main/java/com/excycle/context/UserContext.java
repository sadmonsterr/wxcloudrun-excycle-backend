package com.excycle.context;

import lombok.Data;

@Data
public class UserContext {
    private String openId;

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    public static UserContext getCurrent() {
        UserContext context = CONTEXT.get();
        if (context == null) {
            context = new UserContext();
            CONTEXT.set(context);
        }
        return context;
    }

    public static void setCurrent(UserContext context) {
        CONTEXT.set(context);
    }

    public static String getCurrentOpenId() {
        UserContext context = CONTEXT.get();
        return context != null ? context.getOpenId() : null;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
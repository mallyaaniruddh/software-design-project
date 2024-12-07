package com.ul.vrs.interceptor;

public class LoggingInterceptor implements Interceptor {

    @Override
    public void beforeAction(String action, Object target) {
        if (action == null || target == null) {
            System.out.println("Logging BEFORE action: Invalid input. Action or Target is null.");
            return;
        }
        System.out.println("Logging BEFORE action: " + action + " on target: " + target.getClass().getSimpleName());
    }

    @Override
    public void afterAction(String action, Object target) {
        if (action == null || target == null) {
            System.out.println("Logging AFTER action: Invalid input. Action or Target is null.");
            return;
        }
        System.out.println("Logging AFTER action: " + action + " on target: " + target.getClass().getSimpleName());
    }
}
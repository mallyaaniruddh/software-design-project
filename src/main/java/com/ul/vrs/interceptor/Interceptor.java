package com.ul.vrs.interceptor;

public interface Interceptor {
    /**
     * Method to execute before the target action.
     *
     * @param action Name of the action being performed (e.g., "bookVehicle").
     * @param target The object on which the action is being performed.
     */
    void beforeAction(String action, Object target);

    /**
     * Method to execute after the target action.
     *
     * @param action Name of the action that was performed.
     * @param target The object on which the action was performed.
     */
    void afterAction(String action, Object target);
}

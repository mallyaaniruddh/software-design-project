package com.ul.vrs.interceptor;

import com.ul.vrs.entity.vehicle.Vehicle;
import com.ul.vrs.entity.vehicle.state.ReservedVehicleState;

public class GPSInterceptor implements Interceptor {

    @Override
    public void beforeAction(String action, Object target) {
        if ("applyGPS".equals(action) && target instanceof Vehicle vehicle) {
        	if (!(vehicle.getState() instanceof ReservedVehicleState))  {
                throw new IllegalStateException("GPS can only be applied to RESERVED vehicles.");
            }
            System.out.println("Preparing to add GPS to vehicle ID: " + vehicle.getID());
        }
    }

    @Override
    public void afterAction(String action, Object target) {
        if ("applyGPS".equals(action) && target instanceof Vehicle vehicle) {
            System.out.println("GPS successfully added to vehicle ID: " + vehicle.getID());
        }
    }
}
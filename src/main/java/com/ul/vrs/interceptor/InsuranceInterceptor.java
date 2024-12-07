package com.ul.vrs.interceptor;

import com.ul.vrs.entity.vehicle.Vehicle;
import com.ul.vrs.entity.vehicle.state.ReservedVehicleState;

public class InsuranceInterceptor implements Interceptor {

    @Override
    public void beforeAction(String action, Object target) {
        if ("applyInsurance".equals(action) && target instanceof Vehicle vehicle) {
        	if (!(vehicle.getState() instanceof ReservedVehicleState))  {
                throw new IllegalStateException("Insurance can only be applied to RESERVED vehicles.");
            }
            System.out.println("Preparing to apply insurance to vehicle ID: " + vehicle.getID());
        }
    }

    @Override
    public void afterAction(String action, Object target) {
        if ("applyInsurance".equals(action) && target instanceof Vehicle vehicle) {
            System.out.println("Insurance successfully applied to vehicle ID: " + vehicle.getID());
        }
    }
}
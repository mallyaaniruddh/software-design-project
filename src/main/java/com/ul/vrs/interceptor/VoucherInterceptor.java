package com.ul.vrs.interceptor;

import com.ul.vrs.entity.vehicle.Vehicle;
import com.ul.vrs.entity.vehicle.state.ReservedVehicleState;

public class VoucherInterceptor implements Interceptor {

    @Override
    public void beforeAction(String action, Object target) {
        if ("applyVoucher".equals(action) && target instanceof Vehicle vehicle) {
        	if (!(vehicle.getState() instanceof ReservedVehicleState)) {
                throw new IllegalStateException("Voucher can only be applied to RESERVED vehicles.");
            }
            System.out.println("Preparing to apply voucher to vehicle ID: " + vehicle.getID());
        }
    }

    @Override
    public void afterAction(String action, Object target) {
        if ("applyVoucher".equals(action) && target instanceof Vehicle vehicle) {
            System.out.println("Voucher successfully applied to vehicle ID: " + vehicle.getID());
        }
    }
}
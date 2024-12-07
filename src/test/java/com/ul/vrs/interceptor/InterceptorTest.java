package com.ul.vrs.interceptor;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.ul.vrs.entity.Color;
import com.ul.vrs.entity.account.Customer;
import com.ul.vrs.entity.booking.Booking;
import com.ul.vrs.entity.booking.decorator.*;
import com.ul.vrs.entity.vehicle.Car;
import com.ul.vrs.entity.vehicle.Vehicle;
import com.ul.vrs.entity.vehicle.fuel.PetrolFuel;
import com.ul.vrs.entity.vehicle.state.ReservedVehicleState;

public class InterceptorTest {

    @Test
    public void testGPSInterceptor() {
        // Arrange
        GPSInterceptor gpsInterceptor = new GPSInterceptor();
        Customer customer = new Customer("Mark Bough", "password");
        Vehicle vehicle = new Car(1L, "Test Car", "Toyota", 2020, 20000, Color.BLUE, new PetrolFuel(), 4, 300);
        vehicle.updateState(new ReservedVehicleState());
        Booking booking = new Booking(customer, vehicle, 0);

        customer.addInterceptor(gpsInterceptor);

        // Act
        Booking gpsBooking = customer.customizeBooking(booking, Customization.GPS);

        // Assert
        assertNotNull(gpsBooking, "GPS customization should not result in null.");
        assertTrue(gpsBooking instanceof GPSBookingDecorator, "Booking should be decorated with GPS.");
    }

    @Test
    public void testVoucherInterceptor() {
        // Arrange
        VoucherInterceptor voucherInterceptor = new VoucherInterceptor();
        Customer customer = new Customer("Macht Bough", "securepassword");
        Vehicle vehicle = new Car(2L, "Luxury Car", "BMW", 2022, 50000, Color.BLACK, new PetrolFuel(), 4, 450);
        vehicle.updateState(new ReservedVehicleState());
        Booking booking = new Booking(customer, vehicle, 0);

        customer.addInterceptor(voucherInterceptor);

        // Act
        Booking voucherBooking = customer.customizeBooking(booking, Customization.VOUCHER);

        // Assert
        assertNotNull(voucherBooking, "Voucher customization should not result in null.");
        assertTrue(voucherBooking instanceof VoucherBookingDecorator, "Booking should be decorated with Voucher.");
    }

    @Test
    public void testInsuranceInterceptor() {
        // Arrange
        InsuranceInterceptor insuranceInterceptor = new InsuranceInterceptor();
        Customer customer = new Customer("Mandy Bough", "safepassword");
        Vehicle vehicle = new Car(3L, "SUV", "Ford", 2021, 30000, Color.RED, new PetrolFuel(), 5, 400);
        vehicle.updateState(new ReservedVehicleState());
        Booking booking = new Booking(customer, vehicle, 0);

        customer.addInterceptor(insuranceInterceptor);

        // Act
        Booking insuranceBooking = customer.customizeBooking(booking, Customization.INSURANCE);

        // Assert
        assertNotNull(insuranceBooking, "Insurance customization should not result in null.");
        assertTrue(insuranceBooking instanceof InsuranceBookingDecorator, "Booking should be decorated with Insurance.");
    }
}
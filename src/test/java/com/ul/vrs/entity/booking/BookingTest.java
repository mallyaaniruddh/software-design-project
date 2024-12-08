package com.ul.vrs.entity.booking;

import com.ul.vrs.entity.account.Customer;
import com.ul.vrs.entity.vehicle.Vehicle;
import com.ul.vrs.entity.Color;
import com.ul.vrs.entity.booking.decorator.*;
import com.ul.vrs.entity.vehicle.fuel.PetrolFuel;
import com.ul.vrs.entity.vehicle.state.AvailableVehicleState;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BookingTest {
    private Customer testCustomer;
    private Vehicle testVehicle;
    private Booking testBooking;
    private int testNumberOfRentingDays;

    @BeforeAll
    public void setup() {
        // Initialize common test objects
        testCustomer = new Customer("John Doe", "john.doe@example.com");

        testVehicle = new Vehicle(1L, "Camry", "Toyota", 2020, 25_000, Color.BLACK, new PetrolFuel(), new AvailableVehicleState()) {
            @Override
            public double getRentingCost(int numberOfRentingDays) {
                return 500.0 * numberOfRentingDays;
            }
        };

        testNumberOfRentingDays = 1;

        testBooking = new Booking(testCustomer, testVehicle, testNumberOfRentingDays);
    }

    @BeforeEach
    public void resetBooking() {
        // Reset testBooking for each test to ensure isolation
        testBooking = new Booking(testCustomer, testVehicle, testNumberOfRentingDays);
    }

    @Test
    public void testValidBookingInitialization() {
        assertNotNull(testBooking.getBookingId(), "Booking ID should not be null");
        assertEquals(testCustomer, testBooking.getAccount(), "Customer should be correctly initialized");
        assertEquals(testVehicle, testBooking.getVehicle(), "Vehicle should be correctly initialized");
        assertFalse(testBooking.getIsAuthenticated(), "is_authenticated should be false by default");
        assertEquals(500, testBooking.getPrice(), "Default price should be 500");
    }

    @Test
    public void testSetIsAuthenticated() {
        assertFalse(testBooking.getIsAuthenticated(), "Payment should not be authenticated initially");
        testBooking.setIsAuthenticated(true);
        assertTrue(testBooking.getIsAuthenticated(), "Payment should be authenticated after setting it to true");
    }

    @Test
    public void testBookingIdConsistency() {
        UUID bookingId = testBooking.getBookingId();
        assertNotNull(bookingId, "Booking ID should not be null");
        assertEquals(bookingId, testBooking.getBookingId(), "Booking ID should remain consistent");
    }

    @Test
    public void testBookingDecoratorCustomerDelegation() {
        BookingDecorator decoratedBooking = new GPSBookingDecorator(testBooking);
        assertEquals(testBooking.getAccount(), decoratedBooking.getAccount(), "Decorator should delegate getCustomer to the original booking");
    }

    @Test
    public void testBookingDecoratorVehicleDelegation() {
        BookingDecorator decoratedBooking = new GPSBookingDecorator(testBooking);
        assertEquals(testBooking.getVehicle(), decoratedBooking.getVehicle(), "Decorator should delegate getVehicle to the original booking");
    }

    @Test
    public void testBookingDecoratorAuthenticatePayment() {
        BookingDecorator decoratedBooking = new GPSBookingDecorator(testBooking);
        assertFalse(decoratedBooking.getIsAuthenticated(), "Decorator should initially report unauthenticated payment");
        decoratedBooking.setIsAuthenticated(true);
        assertTrue(decoratedBooking.getIsAuthenticated(), "Decorator should delegate payment authentication to the original booking");
    }
}
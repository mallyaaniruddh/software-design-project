package com.ul.vrs.entity.account;

import com.ul.vrs.entity.Color;
import com.ul.vrs.entity.booking.Booking;
import com.ul.vrs.entity.booking.decorator.Customization;
import com.ul.vrs.entity.booking.decorator.GPSBookingDecorator;
import com.ul.vrs.entity.booking.decorator.InsuranceBookingDecorator;
import com.ul.vrs.entity.booking.decorator.VoucherBookingDecorator;
import com.ul.vrs.entity.booking.payment.PaymentRequest;
import com.ul.vrs.entity.vehicle.Car;
import com.ul.vrs.entity.vehicle.Vehicle;
import com.ul.vrs.entity.vehicle.fuel.DieselFuel;
import com.ul.vrs.entity.vehicle.fuel.PetrolFuel;
import com.ul.vrs.entity.vehicle.state.AvailableVehicleState;
import com.ul.vrs.entity.vehicle.state.ReservedVehicleState;
import com.ul.vrs.interceptor.Interceptor;
import com.ul.vrs.service.RentalSystemService;
import com.ul.vrs.service.VehicleManagerService;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomerTest {

	@Test
	public void testSearchAvailableVehicles() {
	    // Arrange
	    Customer customer = new Customer("Mark Bough", "password");
	    VehicleManagerService vehicleManagerService = mock(VehicleManagerService.class);
	    customer.setVehicleManagerService(vehicleManagerService);

	    // Case 1: Vehicles available
	    List<Vehicle> vehicles = List.of(
	        new Car(1L, "Car A", "Toyota", 2020, 20000, Color.BLUE, new PetrolFuel(), 4, 300),
	        new Car(2L, "Car B", "Honda", 2018, 18000, Color.RED, new PetrolFuel(), 4, 350)
	    );
	    when(vehicleManagerService.getAllVehicles()).thenReturn(vehicles);

	    // Act
	    List<Vehicle> availableVehicles = customer.searchAvailableVehicles();

	    // Assert
	    assertNotNull(availableVehicles, "Available vehicles list should not be null.");
	    assertTrue(availableVehicles.size() > 0, "Available vehicles list should not be empty.");

	    // Case 2: No vehicles available
	    when(vehicleManagerService.getAllVehicles()).thenReturn(new ArrayList<>());

	    // Act
	    availableVehicles = customer.searchAvailableVehicles();

	    // Assert
	    assertNotNull(availableVehicles, "Available vehicles list should not be null even if empty.");
	    assertTrue(availableVehicles.isEmpty(), "Available vehicles list should be empty when no vehicles are available.");
	}


	@Test
	public void testBookVehicleSuccessfully() {
	    // Arrange
	    Customer customer = new Customer("Martin Bough", "password123");
	    Vehicle vehicle = new Car(1L, "Model X", "Tesla", 2021, 50000, Color.BLACK, new PetrolFuel(), 4, 300);
	    vehicle.updateState(new AvailableVehicleState());

	    RentalSystemService mockRentalSystemService = mock(RentalSystemService.class);
	    customer.setRentalSystemService(mockRentalSystemService);
	    Interceptor mockInterceptor = mock(Interceptor.class);
	    customer.addInterceptor(mockInterceptor);

	    // Case 1: Booking a valid vehicle
	    Booking booking = customer.bookVehicle(vehicle, 5);
	    assertNotNull(booking, "Booking should not be null.");
	    assertTrue(vehicle.getState() instanceof ReservedVehicleState, "Vehicle should be in RESERVED state after booking.");
	    verify(mockInterceptor).beforeAction(eq("bookVehicle"), eq(vehicle));
	    verify(mockInterceptor).afterAction(eq("bookVehicle"), eq(booking));
	    verify(mockRentalSystemService).makeBooking(eq("Martin Bough"), eq(vehicle), eq(5));

	    // Case 2: Booking a vehicle not in AvailableVehicleState
	    vehicle.updateState(new ReservedVehicleState());
	    booking = customer.bookVehicle(vehicle, 5);
	    assertNull(booking, "Booking should be null when vehicle is not available.");
	    assertTrue(vehicle.getState() instanceof ReservedVehicleState, "Vehicle state should remain RESERVED.");
	}

	
	@Test
	public void testMakeBookingPayment() {
	    // Arrange
	    Customer customer = new Customer("Martha Bough", "intlypassword");
	    RentalSystemService mockRentalSystemService = mock(RentalSystemService.class);
	    customer.setRentalSystemService(mockRentalSystemService);
	    Interceptor mockInterceptor = mock(Interceptor.class);
	    customer.addInterceptor(mockInterceptor);

	    UUID bookingId = UUID.randomUUID();
	    PaymentRequest paymentRequest = mock(PaymentRequest.class);

	    customer.makeBookingPayment(bookingId, paymentRequest);
	    verify(mockInterceptor).beforeAction(eq("makeBookingPayment"), eq(paymentRequest));
	    verify(mockInterceptor).afterAction(eq("makeBookingPayment"), eq(paymentRequest));
	    verify(mockRentalSystemService).makeBookingPayment(eq(bookingId), eq(paymentRequest));
	}


	@Test
	public void testCancelBooking() {
	    // Arrange
	    Customer customer = new Customer("Mark Bough", "password");
	    Vehicle vehicle = new Car(1L, "Test Car", "Toyota", 2020, 20000, Color.BLUE, new PetrolFuel(), 4, 300);
	    vehicle.updateState(new ReservedVehicleState());

	    Interceptor mockInterceptor = mock(Interceptor.class);
	    customer.addInterceptor(mockInterceptor);

	    RentalSystemService mockRentalSystemService = mock(RentalSystemService.class);
	    customer.setRentalSystemService(mockRentalSystemService);

	    // Cancellation
	    UUID bookingId = UUID.randomUUID();
	    customer.cancelBooking(vehicle, bookingId);
	    assertTrue(vehicle.getState() instanceof AvailableVehicleState, "Vehicle should be in AVAILABLE state after cancellation.");
	    verify(mockInterceptor).beforeAction(eq("cancelBooking"), eq(vehicle));
	    verify(mockInterceptor).afterAction(eq("cancelBooking"), eq(vehicle));
	}

    @Test
    public void testReturnVehicle() {
        // Arrange
        Customer customer = new Customer("Mark Bough", "password");
        Vehicle vehicle = new Car(1L, "Test Car", "Toyota", 2020, 20000, Color.BLUE, new PetrolFuel(), 4, 300);
        vehicle.updateState(new ReservedVehicleState());

        //Mock interceptor
        Interceptor mockInterceptor = mock(Interceptor.class);
        customer.addInterceptor(mockInterceptor);
        
        //Mock Rental System Service
        RentalSystemService mockRentalSystemService = mock(RentalSystemService.class);
        customer.setRentalSystemService(mockRentalSystemService);

        // Act
        customer.returnVehicle(vehicle, UUID.randomUUID());

        // Assert
        assertTrue(vehicle.getState() instanceof AvailableVehicleState, "Vehicle should be in AVAILABLE state after returning.");
        verify(mockInterceptor).beforeAction(eq("returnVehicle"), eq(vehicle));
        verify(mockInterceptor).afterAction(eq("returnVehicle"), eq(vehicle));
    }

    @Test
    public void testCustomizeBookingWithGPS() {
        // Arrange
        Customer customer = new Customer("Mark Bough", "password");
        Vehicle vehicle = new Car(1L, "Test Car", "Toyota", 2020, 20000, Color.BLUE, new PetrolFuel(), 4, 300);
        int numberOfRentingDays = 5;
        vehicle.updateState(new AvailableVehicleState());
        
        //Mock interceptor
        Interceptor mockInterceptor = mock(Interceptor.class);
        customer.addInterceptor(mockInterceptor);
        
        //Mock Rental System Service
        RentalSystemService mockRentalSystemService = mock(RentalSystemService.class);
        customer.setRentalSystemService(mockRentalSystemService);

        // Create a booking first
        Booking booking = customer.bookVehicle(vehicle, numberOfRentingDays); // Ensure this does not return null

        // Check if booking was created successfully
        assertNotNull(booking, "Booking should not be null.");
        
        //Change vehicle state to reserved after booking is completed
        vehicle.updateState(new ReservedVehicleState());

        // Act
        Booking customizedBooking = customer.customizeBooking(booking, Customization.GPS);

        // Assert
        assertNotNull(customizedBooking, "Customized booking should not be null.");
        assertTrue(customizedBooking instanceof GPSBookingDecorator, "Booking should be decorated with GPS customization.");
        verify(mockInterceptor).beforeAction(eq("applyGPS"), eq(booking));
        verify(mockInterceptor).afterAction(eq("applyGPS"), eq(customizedBooking));
    }
    
    @Test
    public void testCustomizeBookingWithInsurance() {
        // Arrange
        Customer customer = new Customer("Macht Bough", "securepassword");
        Vehicle vehicle = new Car(2L, "Luxury Car", "BMW", 2021, 50000, Color.BLACK, new DieselFuel(), 4, 400);
        int numberOfRentingDays = 5;
        vehicle.updateState(new AvailableVehicleState());
        
        //Mock interceptor
        Interceptor mockInterceptor = mock(Interceptor.class);
        customer.addInterceptor(mockInterceptor);
        
        //Mock Rental System Service
        RentalSystemService mockRentalSystemService = mock(RentalSystemService.class);
        customer.setRentalSystemService(mockRentalSystemService);

        // Create a booking first
        Booking booking = customer.bookVehicle(vehicle, numberOfRentingDays); // Ensure this does not return null

        // Check if booking was created successfully
        assertNotNull(booking, "Booking should not be null.");
        
        //Change vehicle state to reserved after booking is completed
        vehicle.updateState(new ReservedVehicleState());

        // Act
        Booking customizedBooking = customer.customizeBooking(booking, Customization.INSURANCE);

        // Assert
        assertNotNull(customizedBooking, "Customized booking should not be null.");
        assertTrue(customizedBooking instanceof InsuranceBookingDecorator, "Booking should be decorated with Insurance customization.");
        verify(mockInterceptor).beforeAction(eq("applyInsurance"), eq(booking));
        verify(mockInterceptor).afterAction(eq("applyInsurance"), eq(customizedBooking));
    }

    @Test
    public void testCustomizeBookingWithVoucher() {
        // Arrange
        Customer customer = new Customer("Martha Bough", "safePassword");
        Vehicle vehicle = new Car(3L, "Economy Car", "Honda", 2019, 15000, Color.WHITE, new PetrolFuel(), 4, 250);
        int numberOfRentingDays = 5;
        vehicle.updateState(new AvailableVehicleState());
        
        //Mock interceptor
        Interceptor mockInterceptor = mock(Interceptor.class);
        customer.addInterceptor(mockInterceptor);
        
        //Mock Rental System Service
        RentalSystemService mockRentalSystemService = mock(RentalSystemService.class);
        customer.setRentalSystemService(mockRentalSystemService);

        // Create a booking first
        Booking booking = customer.bookVehicle(vehicle, numberOfRentingDays); // Ensure this does not return null

        // Check if booking was created successfully
        assertNotNull(booking, "Booking should not be null.");
        
        //Change vehicle state to reserved after booking is completed
        vehicle.updateState(new ReservedVehicleState());

        // Act
        Booking customizedBooking = customer.customizeBooking(booking, Customization.VOUCHER);

        // Assert
        assertNotNull(customizedBooking, "Customized booking should not be null.");
        assertTrue(customizedBooking instanceof VoucherBookingDecorator, "Booking should be decorated with Voucher customization.");
        verify(mockInterceptor).beforeAction(eq("applyVoucher"), eq(booking));
        verify(mockInterceptor).afterAction(eq("applyVoucher"), eq(customizedBooking));
    }
    
    @Test
    public void testReportIssue() {
        // Arrange
        Customer customer = new Customer("John Doe", "securepassword");
        RentalSystemService mockRentalSystemService = mock(RentalSystemService.class);
        customer.setRentalSystemService(mockRentalSystemService);
        Vehicle vehicle = new Car(1L, "Sedan", "Toyota", 2020, 20000, Color.BLACK, new PetrolFuel(), 4, 300);
        Booking issueDescription = mock(Booking.class);
        UUID vehicleId = UUID.randomUUID();

        // Act
        customer.reportIssue(vehicle, vehicleId, issueDescription);

        // Assert
        verify(mockRentalSystemService).reportIssue(eq(vehicleId), eq(issueDescription));
    }

    @Test
    public void testSubmitFeedback() {
        // Arrange
        Customer customer = new Customer("Jane Doe", "securepassword");
        RentalSystemService mockRentalSystemService = mock(RentalSystemService.class);
        customer.setRentalSystemService(mockRentalSystemService);
        UUID vehicleId = UUID.randomUUID();
        Booking feedback = mock(Booking.class);

        // Act
        customer.submitFeedback(vehicleId, feedback);

        // Assert
        verify(mockRentalSystemService).submitFeedback(eq(vehicleId), eq(feedback));
    }


}
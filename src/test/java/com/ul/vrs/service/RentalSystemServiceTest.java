package com.ul.vrs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.ul.vrs.entity.Color;
import com.ul.vrs.entity.account.Customer;
import com.ul.vrs.entity.booking.Booking;
import com.ul.vrs.entity.booking.decorator.Customization;
import com.ul.vrs.entity.booking.payment.ApplePayPayment;
import com.ul.vrs.entity.booking.payment.PaymentMethod;
import com.ul.vrs.entity.booking.payment.PaymentRequest;
import com.ul.vrs.entity.vehicle.Car;
import com.ul.vrs.entity.vehicle.Vehicle;
import com.ul.vrs.entity.vehicle.fuel.Fuel;
import com.ul.vrs.entity.vehicle.state.*;
import com.ul.vrs.repository.*;

public class RentalSystemServiceTest {
    // Service being tested
    @InjectMocks
    private RentalSystemService rentalSystemService;

    // Mock repositories
    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private BookingRepository bookingRepository;

    // Mock VehicleManagerService
    @Mock
    private VehicleManagerService vehicleManagerService;

    // Mock Account repository
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountManagerService accountManagerService;

    // Mock data for vehicles
    private List<Vehicle> mockVehicles;

    // Mock customer
    private Customer mockCustomer;

    @BeforeEach
    public void setup() {
        // Initialize mocks and set up mock customer
        MockitoAnnotations.openMocks(this);
        mockCustomer = new Customer("test_user", "test_password");
        when(accountRepository.findById(mockCustomer.getUsername())).thenReturn(Optional.of(mockCustomer));
        initMockVehicles(); // Initialize mock vehicles
    }

    private void initMockVehicles() {
        // Create mock implementations for Color and Fuel
        Color color = Color.BLACK; // Example color
        Fuel fuel = mock(Fuel.class); // Mocking the Fuel interface
        when(fuel.getCost()).thenReturn(1.5); // Mock fuel cost

        // Initialize mock vehicles with Car-specific parameters
        mockVehicles = new ArrayList<>();
        mockVehicles.add(new Car(1L, "TestCar1", "BrandA", 2020, 100.0, color, fuel, 4, 250.0f)); // 4 doors, small trunk
        mockVehicles.add(new Car(2L, "TestCar2", "BrandB", 2021, 200.0, color, fuel, 5, 350.0f)); // 5 doors, large trunk

        for (Vehicle vehicle : mockVehicles) {
            when(vehicleRepository.findById(vehicle.getID())).thenReturn(Optional.of(vehicle));
            when(vehicleRepository.save(vehicle)).thenReturn(vehicle);
        }
    }

    @Test
    public void testMakeBooking_Success() {
        // Test for successfully making a booking
        Vehicle vehicle = mockVehicles.get(0); // Select the first vehicle
        when(vehicleRepository.findById(vehicle.getID())).thenReturn(Optional.of(vehicle));

        Booking booking = new Booking(mockCustomer, vehicle, 1);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        UUID bookingId = rentalSystemService.makeBooking(mockCustomer.getUsername(), vehicle, 1); // Create a booking

        assertNotNull(bookingId, "Booking ID should not be null"); // Booking ID must not be null
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        Optional<Booking> retrievedBooking = rentalSystemService.getBookingById(bookingId);
        assertTrue(retrievedBooking.isPresent(), "Booking should exist"); // Booking should exist
        assertEquals(vehicle, retrievedBooking.get().getVehicle(), "The vehicle in the booking should match the one provided");
        assertEquals(new ReservedVehicleState(), vehicle.getState(), "Vehicle should be RESERVED after booking");
    }

    @Test
    public void testMakeBooking_NullVehicle() {
        // Test for attempting to book a null vehicle
        UUID bookingId = rentalSystemService.makeBooking(mockCustomer.getUsername(), null, 0);
        assertNull(bookingId, "Booking ID should be null when vehicle is null"); // Booking ID must be null
    }

    @Test
    public void testCustomizeBooking_Success() {
        // Test for successfully customizing a booking
        Vehicle vehicle = mockVehicles.get(0);
        when(vehicleRepository.findById(vehicle.getID())).thenReturn(Optional.of(vehicle));
        Booking booking = new Booking(mockCustomer, vehicle, 1);
        when(bookingRepository.findById(booking.getBookingId())).thenReturn(Optional.of(booking));

        Customization mockCustomization = Customization.GPS;
        Optional<Booking> customizedBooking = rentalSystemService.customizeBooking(booking.getBookingId(), mockCustomization);

        assertNotNull(customizedBooking, "Customized booking should not be null");
        assertEquals(booking.getBookingId(), customizedBooking.get().getBookingId(), "Booking ID should remain the same");
    }

    @Test
    public void testCustomizeBooking_InvalidBooking() {
        // Test for customizing a non-existent booking
        UUID invalidBookingId = UUID.randomUUID();
        Customization mockCustomization = Customization.INSURANCE;

        Optional<Booking> customizedBooking = rentalSystemService.customizeBooking(invalidBookingId, mockCustomization);

        assertTrue(customizedBooking.isEmpty(), "Customization should fail for non-existent booking");
    }

    @Test
    public void testAuthenticateBookingPayment_Success() {
        // Test for successful payment authentication
        Vehicle vehicle = mockVehicles.get(0);
        Booking booking = new Booking(mockCustomer, vehicle, 1);
        when(bookingRepository.findById(booking.getBookingId())).thenReturn(Optional.of(booking));

        PaymentRequest mockPaymentRequest = new PaymentRequest(PaymentMethod.APPLEPAY, new ApplePayPayment("xyz", "123"));
        rentalSystemService.makeBookingPayment(booking.getBookingId(), mockPaymentRequest);

        when(bookingRepository.findById(booking.getBookingId())).thenReturn(Optional.of(booking));

        Optional<Booking> retrievedBooking = rentalSystemService.getBookingById(booking.getBookingId());
        assertTrue(retrievedBooking.isPresent(), "Booking should exist");
        assertTrue(retrievedBooking.get().getIsAuthenticated(), "Booking payment should be authenticated");
    }

    @Test
    public void testReturnVehicle_Success() {
        // Test for returning a vehicle
        Vehicle vehicle = mockVehicles.get(0);
        Booking booking = new Booking(mockCustomer, vehicle, 1);

        // Mock the repository behavior
        when(bookingRepository.findById(booking.getBookingId())).thenReturn(Optional.of(booking));

        // Invoke the method under test
        rentalSystemService.returnVehicle(booking.getBookingId());

        // Verify the booking was deleted
        verify(bookingRepository, times(1)).delete(booking);
        assertEquals(new InMaintenanceVehicleState(), vehicle.getState(), "Vehicle state should be IN_MAINTENANCE");
    }


    @Test
    public void testCancelBooking_Success() {
        // Test for canceling a booking
        Vehicle vehicle = mockVehicles.get(0);
        Booking booking = new Booking(mockCustomer, vehicle, 1);

        // Mock the repository behavior
        when(bookingRepository.findById(booking.getBookingId())).thenReturn(Optional.of(booking));

        // Invoke the method under test
        rentalSystemService.cancelBooking(booking.getBookingId());

        // Verify the booking was deleted
        verify(bookingRepository, times(1)).delete(booking);
        assertEquals(new AvailableVehicleState(), vehicle.getState(), "Vehicle state should be AVAILABLE after cancellation");
    }


    @Test
    public void testGetAllBookings() {
        // Test for retrieving all bookings
        List<Booking> bookingList = new ArrayList<>();
        for (Vehicle vehicle : mockVehicles) {
            Booking booking = new Booking(mockCustomer, vehicle, 1);
            bookingList.add(booking);
        }
        when(bookingRepository.findAll()).thenReturn(bookingList);

        List<Booking> allBookings = rentalSystemService.getAllBookings();

        assertEquals(bookingList.size(), allBookings.size(), "Should return all bookings");
    }

    @Test
    public void testCarRentingCost() {
        // Test for calculating the renting cost of a car
        Car car = (Car) mockVehicles.get(1); // A car with 5 doors and 350 trunk capacity
        int rentingDays = 5;

        // Calculate the expected cost based on the Car implementation
        double fuelCost = car.getFuelType().getCost() * rentingDays;
        double baseCost = car.getBaseCost() * rentingDays;
        double featureCost = 0.0;

        // Additional cost for doors and trunk capacity
        if (car.getNumberOfDoors() > 4) {
            featureCost += 50 * rentingDays;
        }
        if (car.getTrunkCapacity() > 300) {
            featureCost += 30 * rentingDays;
        }

        double expectedCost = fuelCost + baseCost + featureCost;

        assertEquals(expectedCost, car.getRentingCost(rentingDays), "The renting cost calculation should match the expected value");
    }
    
    @Test
    public void testReportIssuesAndSubmitFeedback() {
        // Arrange
        UUID invalidVehicleId = null;
        Booking invalidInput = null;
        UUID validVehicleId1 = UUID.randomUUID();
        UUID validVehicleId2 = UUID.randomUUID();
        UUID validVehicleId3 = UUID.randomUUID();
        Booking validInput = mock(Booking.class); // Mock the Booking object if it has behavior or state.

        // Act & Assert: Reporting Issues
        rentalSystemService.reportIssue(validVehicleId1, validInput);
        rentalSystemService.reportIssue(validVehicleId2, validInput);
        Map<UUID, Booking> issues = rentalSystemService.getAllReportedIssues();

        rentalSystemService.reportIssue(invalidVehicleId, invalidInput);
        Map<UUID, Booking> updatedIssues = rentalSystemService.getAllReportedIssues();

        // Validate Issues
        assertEquals(2, issues.size(), "Issues map should contain entries for valid inputs.");
        assertEquals(validInput, issues.get(validVehicleId1), "Reported issue should match the input.");
        assertEquals(validInput, issues.get(validVehicleId2), "Reported issue should match the input.");
        assertEquals(2, updatedIssues.size(), "Issues map should remain unchanged for invalid inputs.");

        // Act & Assert: Submitting Feedback
        rentalSystemService.submitFeedback(validVehicleId1, validInput);
        rentalSystemService.submitFeedback(validVehicleId2, validInput);
        rentalSystemService.submitFeedback(validVehicleId3, validInput);
        Map<UUID, Booking> feedbacks = rentalSystemService.getAllFeedback();

        rentalSystemService.submitFeedback(invalidVehicleId, invalidInput);
        Map<UUID, Booking> updatedFeedbacks = rentalSystemService.getAllFeedback();

        // Validate Feedbacks
        assertEquals(3, feedbacks.size(), "Feedbacks map should contain entries for valid inputs.");
        assertEquals(validInput, feedbacks.get(validVehicleId1), "Submitted feedback should match the input.");
        assertEquals(validInput, feedbacks.get(validVehicleId2), "Submitted feedback should match the input.");
        assertEquals(validInput, feedbacks.get(validVehicleId3), "Submitted feedback should match the input.");
        assertEquals(3, updatedFeedbacks.size(), "Feedbacks map should remain unchanged for invalid inputs.");
    }

}

package com.ul.vrs.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ul.vrs.entity.account.Account;
import com.ul.vrs.entity.booking.Booking;
import com.ul.vrs.entity.booking.decorator.Customization;
import com.ul.vrs.entity.booking.decorator.factory.BookingDecoratorFactoryMethod;
import com.ul.vrs.entity.booking.payment.PaymentRequest;
import com.ul.vrs.entity.booking.payment.strategy.PaymentStrategy;
import com.ul.vrs.entity.vehicle.Vehicle;
import com.ul.vrs.entity.vehicle.state.AvailableVehicleState;
import com.ul.vrs.entity.vehicle.state.InMaintenanceVehicleState;
import com.ul.vrs.entity.vehicle.state.ReservedVehicleState;

import com.ul.vrs.repository.BookingRepository;
import com.ul.vrs.repository.AccountRepository;

@Service
public class RentalSystemService {
	
	private final Map<UUID, Booking> issues = new HashMap<>();
    private final Map<UUID, Booking> feedbacks = new HashMap<>();
    
    @Autowired
    BookingRepository bookingRepository;

    @Autowired 
    AccountRepository accountRepository;

    @Autowired
    private VehicleManagerService vehicleManagerService;


    public Optional<Booking> getBookingById(UUID bookingId) {
        return bookingRepository.findById(bookingId);
    }

    public UUID makeBooking(String username, Vehicle vehicle, int numberOfRentingDays) {
        if (vehicle != null) {
            Account account = accountRepository.findById(username).get();
            Booking booking = new Booking(account, vehicle, numberOfRentingDays);
            UUID bookingId = booking.getBookingId();

            bookingRepository.save(booking);
            vehicle.updateState(new ReservedVehicleState());
            vehicleManagerService.updateVehicle(vehicle.getID(), vehicle);

            return bookingId;
        }

        return null;
    }

    public Optional<Booking> customizeBooking(UUID bookingId, Customization customizationType) {
        Optional<Booking> b = bookingRepository.findById(bookingId);

        if(b.isPresent()) {
            Booking booking = BookingDecoratorFactoryMethod.createBookingDecorator(b.get(), customizationType);
            bookingRepository.delete(b.get());
            bookingRepository.save(booking);
            b = Optional.ofNullable(booking);
        }

        return b;
    }

    public void makeBookingPayment(UUID bookingId, PaymentRequest paymentRequest) {
        Optional<Booking> b = bookingRepository.findById(bookingId);
        PaymentStrategy strategy = paymentRequest.getPaymentStrategy();
        if(b.isPresent()) {
            Booking booking = b.get();
            booking.setIsAuthenticated(strategy.pay(booking.getPrice()));
            bookingRepository.save(booking);
        }
    }

    // TODO: We gotta later use Mechanic to check it out to then update its state
    public void returnVehicle(UUID bookingId) {
        Optional<Booking> b = bookingRepository.findById(bookingId);

        if(b.isPresent()) {
            Booking booking = b.get();
            Vehicle v = booking.getVehicle();
            v.updateState(new InMaintenanceVehicleState());
            vehicleManagerService.updateVehicle(v.getID(), v);
            bookingRepository.delete(booking);
        }
    }

    public void cancelBooking(UUID bookingId) {
        Optional<Booking> b = bookingRepository.findById(bookingId);
        if(b.isPresent()) {
            Booking booking = b.get();
            Vehicle v = booking.getVehicle();
            v.updateState(new AvailableVehicleState());
            vehicleManagerService.updateVehicle(v.getID(), v);
            bookingRepository.delete(booking);
        }
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
    
    // Reports an issue for a specific vehicle.
    public void reportIssue(UUID vehicleId, Booking issueDescription) {
        if (isValidInput(vehicleId, issueDescription)) {
            issues.put(vehicleId, issueDescription);
            System.out.println("Issue reported for Vehicle ID: " + vehicleId + " | Issue: " + issueDescription);
        } else {
            System.err.println("Invalid vehicle ID or issue description.");
        }
    }

    // Submits feedback for a specific vehicle.
    public void submitFeedback(UUID vehicleId, Booking feedback) {
        if (isValidInput(vehicleId, feedback)) {
            feedbacks.put(vehicleId, feedback);
            System.out.println("Feedback submitted for Vehicle ID: " + vehicleId + " | Feedback: " + feedback);
        } else {
            System.err.println("Invalid vehicle ID or feedback text.");
        }
    }

    // Retrieves all reported issues.
    public Map<UUID, Booking> getAllReportedIssues() {
        return new HashMap<>(issues);
    }

    // Retrieves all feedback submitted.
    public Map<UUID, Booking> getAllFeedback() {
        return new HashMap<>(feedbacks);
    }

    // Helper method to validate inputs.
    private boolean isValidInput(UUID vehicleId, Booking input) {
        return vehicleId != null && input != null;
    }
}
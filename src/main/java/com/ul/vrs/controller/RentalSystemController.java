package com.ul.vrs.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ul.vrs.entity.booking.Booking;
import com.ul.vrs.entity.booking.decorator.Customization;
import com.ul.vrs.entity.booking.payment.PaymentRequest;
import com.ul.vrs.entity.vehicle.Vehicle;
import com.ul.vrs.security.JwtTokenUtil;
import com.ul.vrs.service.RentalSystemService;
import com.ul.vrs.service.VehicleManagerService;
import com.ul.vrs.controller.command.CommandInvoker;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/renting")
public class RentalSystemController {
    @Autowired
    private RentalSystemService rentalSystemService;

    @Autowired
    private VehicleManagerService vehicleManager;

    @Autowired
    private CommandInvoker invoker;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    // Get all vehicles - http://localhost:8080/api/vehicles
    @GetMapping("/list_bookings")
    public List<Booking> getBookings() {
        return rentalSystemService.getAllBookings();
    }

    // Make booking - http://localhost:8080/api/renting/make_booking/{id}
    @PostMapping("/make_booking/{id}")
    public ResponseEntity<?> makeBooking(@PathVariable long id, @RequestBody(required = false) Map<String, Integer> payload, @RequestHeader("Authorization") String token) {
        if (payload == null || !payload.containsKey("numberOfRentingDays")) {
            return ResponseEntity.badRequest().body("Invalid request: 'numberOfRentingDays' is required.");
        }

        int numberOfRentingDays = payload.get("numberOfRentingDays");

        String username = jwtTokenUtil.extractUsername(token);

        Optional<Vehicle> vehicleToBook = vehicleManager.getVehicleById(id);

        if (!vehicleToBook.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Vehicle vehicle = vehicleToBook.get();
        UUID bookingId = rentalSystemService.makeBooking(username, vehicle, numberOfRentingDays);

        if (bookingId != null) {
            return ResponseEntity.ok(bookingId);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Customise booking - http://localhost:8080/api/renting/customize_booking/{id}
    @PutMapping("/customize_booking/{id}")
    public ResponseEntity<Booking> customizeBooking(@PathVariable UUID id, @RequestBody Customization decorator) {
        Optional<Booking> booking = rentalSystemService.customizeBooking(id, decorator);

        if(booking.isPresent()) {
            return ResponseEntity.ok(booking.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Authenticate payment for booking - http://localhost:8080/api/renting/make_payment/{id}
    @PutMapping("/make_payment/{id}")
    public ResponseEntity<Booking> makeBookingPayment(@PathVariable UUID id, @RequestBody PaymentRequest payment) {
        rentalSystemService.makeBookingPayment(id, payment);
        Optional<Booking> booking = rentalSystemService.getBookingById(id);

        if (booking.isPresent()) {
            return ResponseEntity.ok(booking.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Return vehicle - http://localhost:8080/api/renting/return_vehicle/{id}
    @PutMapping("/return_vehicle/{id}")
    public ResponseEntity<String> returnVehicle(@PathVariable UUID id) {

        rentalSystemService.returnVehicle(id);

        return ResponseEntity.ok("Vehicle of Booking (ID=" + id + ") has been returned.");
    }

    // Cancel booking - http://localhost:8080/api/renting/cancel_booking/{id}
    @DeleteMapping("/cancel_booking/{id}")
    public ResponseEntity<String> cancelBooking(@PathVariable UUID id) {


        rentalSystemService.cancelBooking(id);

        return ResponseEntity.ok("Booking (ID=" + id + ") has been canceled.");
    }

    // Return vehicle and open gate - http://localhost:8080/api/renting/return_vehicle_and_open_gate/{id}
    @PutMapping("/return_vehicle_and_open_gate/{id}")
    public ResponseEntity<String> returnVehicleAndOpenGate(@PathVariable UUID id) {

            // Dynamically set the returnCar command with the current bookingId
            invoker.setBookingID(id);

            // Execute commands
            invoker.executeCommand("openGate");
            invoker.executeCommand("returnCar");

            return ResponseEntity.ok("Vehicle of Booking (ID=" + id + ") has been returned and gate opened.");
    }
}
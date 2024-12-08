package com.ul.vrs.entity.booking;

import com.ul.vrs.entity.vehicle.Car;
import com.ul.vrs.entity.vehicle.Vehicle;

import jakarta.persistence.*;

import com.ul.vrs.entity.account.*;
import com.ul.vrs.entity.booking.decorator.Customization;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "booking_type")
@DiscriminatorValue("BASE")
public class Booking {
    @ManyToOne
    @JoinColumn(name = "account_id")
    private final Account account;

    @OneToOne
    @JoinColumn(name = "vehicle_id")
    private final Vehicle vehicle;
    
    @Id
    private final UUID bookingId;
    private final double price;
    private final int numberOfRentingDays;
    private boolean isAuthenticated;

    @ElementCollection
    @CollectionTable(name = "booking_decorators", joinColumns = @JoinColumn(name = "booking_id"))
    protected List<Customization> decorators;

    protected Booking(UUID bookingId, Account account, Vehicle vehicle, int numberOfRentingDays) {
        this.account = account;
        this.vehicle = vehicle;
        this.bookingId = bookingId;
        this.isAuthenticated = false;
        this.numberOfRentingDays = numberOfRentingDays;
        this.price = this.vehicle.getRentingCost(numberOfRentingDays);
        this.decorators = new ArrayList<>();
    }

    protected Booking(UUID bookingId, Account account, Vehicle vehicle, int numberOfRentingDays, boolean isAuthenticated, List<Customization> decorators, double price) {
        this.account = account;
        this.vehicle = vehicle;
        this.bookingId = bookingId;
        this.isAuthenticated = isAuthenticated;
        this.numberOfRentingDays = numberOfRentingDays;
        this.price = price;
        this.decorators = decorators;
    }

    public Booking(Account account, Vehicle vehicle, int numberOfRentingDays) {
        this(UUID.randomUUID(), account, vehicle, numberOfRentingDays);
    }

    public Booking() {
        this.account = new Customer();
        this.vehicle = new Car();
        this.bookingId = UUID.randomUUID();
        this.isAuthenticated = false;
        this.numberOfRentingDays = 1;
        this.price = this.vehicle.getRentingCost(numberOfRentingDays);
        this.decorators = new ArrayList<>();
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setIsAuthenticated(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
    }

    public int getNumberOfRentingDays() {
        return this.numberOfRentingDays;
    }

    public double getPrice() {
        return this.price;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Account getAccount() {
        return account;
    }

    public boolean getIsAuthenticated() {
        return isAuthenticated;
    }

    public List<Customization> getDecorators() {
        return this.decorators;
    }
}
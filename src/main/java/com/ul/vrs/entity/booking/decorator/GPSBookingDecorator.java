package com.ul.vrs.entity.booking.decorator;

import com.ul.vrs.entity.booking.Booking;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("GPS")
public class GPSBookingDecorator extends BookingDecorator {
    public GPSBookingDecorator(Booking booking) {
        super(booking);
        this.decorators.add(Customization.GPS);
    }

    public GPSBookingDecorator() {
        super();
        this.decorators.add(Customization.GPS);
    }

    public double getPrice() {
        return super.getPrice() + 10;
    }
}
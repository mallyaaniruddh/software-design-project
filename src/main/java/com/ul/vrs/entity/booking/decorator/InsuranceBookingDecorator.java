package com.ul.vrs.entity.booking.decorator;

import com.ul.vrs.entity.booking.Booking;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("INSURANCE")
public class InsuranceBookingDecorator extends BookingDecorator{
    public InsuranceBookingDecorator(Booking booking) {
        super(booking);
        this.decorators.add(Customization.INSURANCE);
    }

    public InsuranceBookingDecorator() {
        super();
        this.decorators.add(Customization.INSURANCE);
    }

    public double getPrice() {
        return super.getPrice() + super.getNumberOfRentingDays() * 100 ;
    }
}

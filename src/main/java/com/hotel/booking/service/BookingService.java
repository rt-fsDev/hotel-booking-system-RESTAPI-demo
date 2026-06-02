package com.hotel.booking.service;

import com.hotel.booking.exception.BookingNotFoundException;
import com.hotel.booking.exception.GuestCapacityException;
import com.hotel.booking.exception.RoomFullyBookedException;
import com.hotel.booking.model.Booking;
import com.hotel.booking.model.RoomType;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BookingService {
    private final Map<Integer, Booking> bookings = new ConcurrentHashMap<>();
    private final Map<RoomType, Integer> availableRooms = new EnumMap<>(RoomType.class);

    public BookingService() {
        // Initialize inventory: 10 Single, 7 Double, 3 Suite
        availableRooms.put(RoomType.SINGLE, 10);
        availableRooms.put(RoomType.DOUBLE, 7);
        availableRooms.put(RoomType.SUITE, 3);
    }

    public List<Booking> getAllBookings() {
        return new ArrayList<>(bookings.values());
    }

    public Map<RoomType, Integer> getAllAvailableRooms() {
        return new EnumMap<>(availableRooms);
    }

    public Booking createBooking(String guestName, RoomType roomType, int numberOfGuests) {
        // Check capacity
        if (numberOfGuests > roomType.capacity) {
            throw new GuestCapacityException(roomType.svName, roomType.capacity, numberOfGuests);
        }

        // Check availability
        int available = availableRooms.getOrDefault(roomType, 0);
        if (available <= 0) {
            throw new RoomFullyBookedException(roomType.svName);
        }

        // Create booking
        Booking booking = new Booking(guestName, roomType, numberOfGuests);
        bookings.put(booking.getId(), booking);
        availableRooms.put(roomType, available - 1);

        return booking;
    }

    public void deleteBooking(int id) {
        Booking removed = bookings.remove(id);
        if (removed == null) {
            throw new BookingNotFoundException(id);
        }
        // Return room to inventory
        availableRooms.put(removed.getRoomType(), availableRooms.get(removed.getRoomType()) + 1);
    }
}
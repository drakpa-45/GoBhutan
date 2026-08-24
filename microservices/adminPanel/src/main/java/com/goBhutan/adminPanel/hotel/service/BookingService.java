package com.goBhutan.adminPanel.hotel.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.goBhutan.adminPanel.hotel.dto.BookingRequestDTO;
import com.goBhutan.adminPanel.hotel.dto.BookingSummaryDTO;
import com.goBhutan.adminPanel.hotel.dto.BookingWithHotelDTO;
import com.goBhutan.adminPanel.hotel.entity.Guest;
import com.goBhutan.adminPanel.hotel.entity.Hotel;
import com.goBhutan.adminPanel.hotel.entity.Room;
import com.goBhutan.adminPanel.hotel.repository.BookingSummary;
import com.goBhutan.adminPanel.hotel.repository.HotelRepository;
import com.goBhutan.adminPanel.hotel.repository.RoomRepository;
import com.goBhutan.adminPanel.paymentInt.dto.ServicePaymentRequest;
import com.goBhutan.adminPanel.paymentInt.dto.WalletPaymentResult;
import com.goBhutan.adminPanel.paymentInt.service.PaymentIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.goBhutan.adminPanel.hotel.entity.Booking;
import com.goBhutan.adminPanel.hotel.repository.BookingRepository;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class BookingService {

    @Autowired
    private BookingRepository bookingRepo;
    @Autowired
    private HotelRepository hotelRepo;
    @Autowired
    private RoomRepository roomRepo;

    @Autowired
    private PaymentIntegrationService paymentService;

    public List<BookingSummary> getBookingSummariesByHotel(Long hotelId) {
        return bookingRepo.findBookingSummariesByHotelId(hotelId);
    }


    public List<BookingWithHotelDTO> getBookingsByAdminUserId() {
        // 🔹 Extract Keycloak userId from token
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId;

        if (principal instanceof Jwt jwt) {
            userId = jwt.getSubject();
        } else if (principal instanceof String str) {
            userId = str;
        } else {
            throw new RuntimeException("Unsupported principal type: " + principal.getClass());
        }

        return bookingRepo.findAllBookingsWithHotelByAdminUserId(userId);
    }

    public Booking getBooking(Long id) {
        return bookingRepo.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
    }
    @Transactional
    public BookingSummaryDTO createBooking(BookingRequestDTO dto) {
        BookingSummaryDTO summaryDTO =  new BookingSummaryDTO();
        // 1. Fetch hotel
        Hotel hotel = hotelRepo.findById(dto.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        // 2. Lock the room row (pessimistic lock)
        Room room = roomRepo.findByIdForUpdate(dto.getRoomId());
        if (room == null) {
            throw new RuntimeException("Room not found");
        }

        // 3. Check if already booked for given dates
        boolean isAlreadyBooked = bookingRepo.existsByRoomAndDateRange(
                room, dto.getCheckInDate(), dto.getCheckOutDate()
        );

        if (isAlreadyBooked) {
            throw new RuntimeException("Room already booked for selected dates");
        }

        // 4. Create booking entity
        Booking booking = new Booking();
        booking.setHotel(hotel);
        booking.setRoom(room);
        booking.setCheckInDate(dto.getCheckInDate());
        booking.setCheckOutDate(dto.getCheckOutDate());
        booking.setGuestCount(dto.getGuestCount());
        booking.setTotalAmount(dto.getTotalAmount());
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        // 5. Generate booking reference
        String hotelName = hotel.getName();
        String[] words = hotelName.split(" ");
        String initials = words.length >= 2
                ? (words[0].substring(0,1) + words[1].substring(0,1)).toUpperCase()
                : hotelName.substring(0, Math.min(2, hotelName.length())).toUpperCase();

        String roomRef = "F" + room.getRoomNumber();

        String reference = initials + "-" + roomRef + "-" +
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        booking.setBookingReference(reference);

        // 6. Map guests from DTO
        if (dto.getGuests() != null && !dto.getGuests().isEmpty()) {
            List<Guest> guestEntities = dto.getGuests().stream().map(g -> {
                Guest guest = new Guest();
                guest.setCid(g.getCid());
                guest.setName(g.getName());
                guest.setAge(g.getAge());
                guest.setGender(g.getGender());
                guest.setCountryOfOrigin(g.getCountryOfOrigin());
                guest.setPhoneNumber(g.getPhoneNumber());
                guest.setEmail(g.getEmail());
                guest.setCreatedDate(LocalDateTime.now());
                guest.setUpdatedDate(LocalDateTime.now());
                guest.setBooking(booking);
                return guest;
            }).collect(Collectors.toList());

            booking.setGuests(guestEntities);
        }

        // 7. Save booking (cascade saves guests)
         bookingRepo.save(booking);
        summaryDTO.setBookingReference(reference);
        summaryDTO.setStatus(String.valueOf(Booking.BookingStatus.PENDING));
        summaryDTO.setBookingId(room.getId());

        return summaryDTO;
    }

    @Transactional
    public void confirmBooking(Long id, String bookingReference) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = jwt.getSubject();

        // Print ALL claims to see what's available
        jwt.getClaims().forEach((key, value) ->
                System.out.println("Key: " + key + " => Value: " + value)
        );

        // Get realm roles
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        List<String> roles = (List<String>) realmAccess.get("roles");

        System.out.println(roles);
// [default-roles-go-bhutan, offline_access, uma_authorization]

        Booking booking = bookingRepo.getBookingStatus(id, bookingReference);

        if (booking.isExpired()) {
            booking.setStatus(Booking.BookingStatus.EXPIRED);
            bookingRepo.save(booking);
            throw new RuntimeException("Booking has expired. Please create a new booking.");
        }

        if (!"PENDING".equalsIgnoreCase(String.valueOf(booking.getStatus()))) {
            throw new RuntimeException("Cannot confirm booking " + bookingReference
                    + " with status: " + booking.getStatus());
        }

        boolean isPublicUser = roles.stream()
                .anyMatch(role -> role.equals("PUBLIC_USER"));
        if (isPublicUser) {
            // booking is a single object
            if (booking.getWalletPaymentRef() != null && !booking.getWalletPaymentRef().isBlank()) {
                throw new RuntimeException("Wallet payment already processed for this booking");
            }
            BigDecimal totalAmount = booking.getTotalAmount();

            ServicePaymentRequest paymentRequest = new ServicePaymentRequest();
            paymentRequest.setAmount(totalAmount);
            paymentRequest.setCurrency("BTN");
            paymentRequest.setServiceName("HOTEL");
            paymentRequest.setReferenceType("HOTEL_ROOM_BOOKING");
            paymentRequest.setReferenceId(bookingReference);
            paymentRequest.setDescription("Hotel room booking payment");

            WalletPaymentResult walletPayment = paymentService.payWithWallet(paymentRequest, userId);

            paymentService.creditServiceSettlement(
                    walletPayment.getPaymentRef(),
                    totalAmount,
                    "HOTEL",
                    "HOTEL_ROOM_BOOKING",
                    bookingReference,
                    "Hotel room booking settlement",
                    booking.getHotel().getAdminUserId()
            );

            // persist the payment reference
            booking.setWalletPaymentRef(walletPayment.getPaymentRef());
        }
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        // Ensure room exists
        Room room = booking.getRoom();
        if (room == null) {
            throw new RuntimeException("Booking has no associated room");
        }
        room.setStatus(Room.RoomStatus.OCCUPIED);
        roomRepo.save(room);

        bookingRepo.save(booking);
    }

    public void checkIn(Long bookingId) {
        // 1️⃣ Fetch booking
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        // 2️⃣ Ensure booking is CONFIRMED
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new RuntimeException("Booking must be CONFIRMED before check-in");
        }

        // 3️⃣ Ensure room exists
        Room room = booking.getRoom();
        if (room == null) {
            throw new RuntimeException("Booking has no associated room");
        }

        // 4️⃣ Update booking status & check-in date
        booking.setStatus(Booking.BookingStatus.CHECKED_IN);
        if (booking.getCheckInDate() == null) {
            booking.setCheckInDate(LocalDate.now());
        }

        // Optional: set check-in timestamp
        booking.setCreatedAt(LocalDateTime.now());

        // 5️⃣ Update room status
     //   room.setStatus(Room.RoomStatus.OCCUPIED);

        // 6️⃣ Validate guests (optional, if any rules exist)
        if (booking.getGuests() != null) {
            booking.getGuests().forEach(guest -> {
                if (guest.getName() == null || guest.getName().isBlank()) {
                    throw new RuntimeException("Guest name cannot be empty");
                }
            });
        }

        // 7️⃣ Save room and booking in the same transaction
       // roomRepo.save(room);
        bookingRepo.save(booking);
    }


    public void checkOut(Long id) {
        Booking booking = getBooking(id);
        if (booking.getStatus() != Booking.BookingStatus.CHECKED_IN) {
            throw new RuntimeException("Booking must be CHECKED_IN before check-out");
        }
        booking.setStatus(Booking.BookingStatus.CHECKED_OUT);
        Room room = booking.getRoom();
        room.setStatus(Room.RoomStatus.AVAILABLE);
        roomRepo.save(room);
        bookingRepo.save(booking);
    }

    public void cancelBooking(Long id) {
        Booking booking = getBooking(id);
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        Room room = booking.getRoom();
        room.setStatus(Room.RoomStatus.AVAILABLE);
        roomRepo.save(room);
        bookingRepo.save(booking);
    }

    @Transactional
    public Long getTotalBookingCountByHotel() {
        // 🔹 Extract Keycloak userId from token
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId;

        if (principal instanceof Jwt jwt) {
            userId = jwt.getSubject(); // Keycloak "sub" claim
        } else if (principal instanceof String str) {
            userId = str; // fallback if principal is String
        } else {
            throw new RuntimeException("Unsupported principal type: " + principal.getClass());
        }
        List<String> statuses = Arrays.asList("CONFIRMED", "CHECKED_IN", "PENDING");
        return bookingRepo.countByUserIdAndStatuses(userId, statuses);
    }


    public ServicePaymentRequest buildDirectGatewayPaymentRequest(
            String bookingRef, String userId,
            BigDecimal amount, String currency, String description) {

        Booking booking = bookingRepo.findByBookingReference(bookingRef)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingRef));

        if (!booking.getGuests().get(0).getEmail().equals(userId)) {
            // userId is Keycloak UUID — validate ownership via a userId field if you have one
            // skip or adjust based on your entity structure
        }

        ServicePaymentRequest request = new ServicePaymentRequest();
        request.setAmount(amount != null ? amount : booking.getTotalAmount());
        request.setCurrency(currency != null ? currency : "BTN");
        request.setServiceName("HOTEL");
        request.setReferenceType("HOTEL_BOOKING");
        request.setReferenceId(bookingRef);
        request.setDescription(description != null ? description : "Hotel room booking payment");
        return request;
    }

    public void extendDirectGatewayPaymentLock(String bookingRef, String userId, LocalDateTime expiresAt) {
        Booking booking = bookingRepo.findByBookingReference(bookingRef)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingRef));
        booking.setExpiresAt(expiresAt);
        bookingRepo.save(booking);
    }

    public String ensureDirectGatewayPaymentCanContinue(String paymentRef, String userId) {
        // paymentRef here is the gateway paymentRef, look up booking by walletPaymentRef
        Booking booking = bookingRepo.findByWalletPaymentRef(paymentRef)
                .orElseThrow(() -> new RuntimeException("Booking not found for paymentRef: " + paymentRef));

        if (booking.isExpired()) {
            throw new RuntimeException("Booking has expired: " + booking.getBookingReference());
        }
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Booking is not in PENDING state: " + booking.getStatus());
        }
        return booking.getBookingReference();
    }

    public String ensureDirectGatewayPaymentCanDebit(String paymentRef, String userId) {
        // same guard as canContinue — debit is the final OTP step
        return ensureDirectGatewayPaymentCanContinue(paymentRef, userId);
    }

    @Transactional
    public Booking confirmDirectGatewayPaymentBooking(String paymentRef, String userId) {
        Booking booking = bookingRepo.findByWalletPaymentRef(paymentRef)
                .orElseThrow(() -> new RuntimeException("Booking not found for paymentRef: " + paymentRef));

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentMethod("DIRECT_GATEWAY");

        Room room = booking.getRoom();
        if (room == null) {
            throw new RuntimeException("Booking has no associated room");
        }
        room.setStatus(Room.RoomStatus.OCCUPIED);
        roomRepo.save(room);
        bookingRepo.save(booking);

        return booking;
    }
}

package com.goBhutan.adminPanel.hotel.repository;

import com.goBhutan.adminPanel.hotel.dto.HotelSearchRequestDTO;
import com.goBhutan.adminPanel.hotel.dto.HotelSearchResultDTO;
import com.goBhutan.adminPanel.hotel.entity.Amenity;
import com.goBhutan.adminPanel.hotel.entity.Hotel;
import com.goBhutan.adminPanel.hotel.entity.HotelImage;
import com.goBhutan.adminPanel.hotel.entity.Room;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HotelSearchRepositoryImpl implements HotelSearchRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<HotelSearchResultDTO> searchHotels(HotelSearchRequestDTO req, Pageable pageable) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        // ---- Count query ----
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Hotel> countRoot = countQuery.from(Hotel.class);
        countQuery.select(cb.countDistinct(countRoot));
        countQuery.where(buildPredicates(req, cb, countRoot, countQuery).toArray(new Predicate[0]));
        Long total = em.createQuery(countQuery).getSingleResult();

        if (total == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // ---- Step 1: fetch IDs + sort column (Tuple fixes MySQL DISTINCT + ORDER BY) ----
        CriteriaQuery<Tuple> idQuery = cb.createTupleQuery();
        Root<Hotel> idRoot = idQuery.from(Hotel.class);
        idQuery.where(buildPredicates(req, cb, idRoot, idQuery).toArray(new Predicate[0]));
        applySort(req, cb, idQuery, idRoot);

        List<Long> hotelIds = em.createQuery(idQuery)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList()
                .stream()
                .map(t -> t.get(0, Long.class))
                .distinct()
                .collect(Collectors.toList());

        if (hotelIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, total);
        }

        // ---- Step 2: load hotels with amenities ----
        List<Hotel> hotelsWithAmenities = em.createQuery("""
                SELECT DISTINCT h FROM Hotel h
                LEFT JOIN FETCH h.amenities
                WHERE h.id IN :ids
                """, Hotel.class)
                .setParameter("ids", hotelIds)
                .getResultList();

        // ---- Step 3: load hotels with images ----
        List<Hotel> hotelsWithImages = em.createQuery("""
                SELECT DISTINCT h FROM Hotel h
                LEFT JOIN FETCH h.images
                WHERE h.id IN :ids
                """, Hotel.class)
                .setParameter("ids", hotelIds)
                .getResultList();

        // ---- Step 4: load all rooms (filter in memory — avoids LEFT JOIN → INNER JOIN issue) ----
        List<Hotel> hotelsWithRooms = em.createQuery("""
                SELECT DISTINCT h FROM Hotel h
                LEFT JOIN FETCH h.rooms
                WHERE h.id IN :ids
                """, Hotel.class)
                .setParameter("ids", hotelIds)
                .getResultList();

        // ---- Step 5: build lookup maps ----
        Map<Long, Hotel> amenitiesById = hotelsWithAmenities.stream()
                .collect(Collectors.toMap(Hotel::getId, h -> h));

        Map<Long, List<HotelImage>> imagesById = hotelsWithImages.stream()
                .collect(Collectors.toMap(
                        Hotel::getId, Hotel::getImages, (a, b) -> a));

        Map<Long, List<Room>> roomsById = hotelsWithRooms.stream()
                .collect(Collectors.toMap(
                        Hotel::getId, Hotel::getRooms, (a, b) -> a));

        // ---- Step 6: preserve sort order from hotelIds ----
        List<HotelSearchResultDTO> content = hotelIds.stream()
                .map(id -> toDTO(
                        amenitiesById.get(id),
                        imagesById.getOrDefault(id, Collections.emptyList()),
                        roomsById.getOrDefault(id, Collections.emptyList())
                ))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, total);
    }

    // -------------------------------------------------------------------------
    // Predicates
    // -------------------------------------------------------------------------
    private List<Predicate> buildPredicates(HotelSearchRequestDTO req,
                                            CriteriaBuilder cb,
                                            Root<Hotel> root,
                                            CriteriaQuery<?> query) {
        List<Predicate> predicates = new ArrayList<>();

        // Always exclude inactive
        predicates.add(cb.equal(root.get("isActive"), true));

        // Keyword — Hotel.name
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            predicates.add(cb.like(
                    cb.lower(root.get("name")),
                    "%" + req.getKeyword().toLowerCase() + "%"
            ));
        }

        // Location
        if (req.getCity() != null && !req.getCity().isBlank()) {
            predicates.add(cb.like(
                    cb.lower(root.get("city")),
                    "%" + req.getCity().toLowerCase() + "%"
            ));
        }
        if (req.getState() != null && !req.getState().isBlank()) {
            predicates.add(cb.like(
                    cb.lower(root.get("state")),
                    "%" + req.getState().toLowerCase() + "%"
            ));
        }
        if (req.getCountry() != null && !req.getCountry().isBlank()) {
            predicates.add(cb.like(
                    cb.lower(root.get("country")),
                    "%" + req.getCountry().toLowerCase() + "%"
            ));
        }

        // Star rating
        if (req.getMinStars() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("starRating"), req.getMinStars()));
        }
        if (req.getMaxStars() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("starRating"), req.getMaxStars()));
        }

        // ---- Amenity filter via EXISTS subquery (fixes double JOIN issue) ----
        boolean hasAmenityNames = req.getAmenityNames() != null && !req.getAmenityNames().isEmpty();
        boolean hasAmenityCategories = req.getAmenityCategories() != null
                && !req.getAmenityCategories().isEmpty();

        if (hasAmenityNames || hasAmenityCategories) {
            Subquery<Long> amenitySub = query.subquery(Long.class);
            Root<Amenity> amenityRoot = amenitySub.from(Amenity.class);
            amenitySub.select(cb.count(amenityRoot));

            List<Predicate> amenityPredicates = new ArrayList<>();
            // Must belong to this hotel
            amenityPredicates.add(cb.equal(amenityRoot.get("hotel"), root));

            if (hasAmenityNames) {
                amenityPredicates.add(
                        cb.lower(amenityRoot.get("name")).in(
                                req.getAmenityNames().stream()
                                        .map(String::toLowerCase)
                                        .collect(Collectors.toList())
                        )
                );
            }
            if (hasAmenityCategories) {
                amenityPredicates.add(
                        amenityRoot.get("category").in(req.getAmenityCategories())
                );
            }

            amenitySub.where(amenityPredicates.toArray(new Predicate[0]));
            // Hotel must have at least one matching amenity
            predicates.add(cb.greaterThan(amenitySub, 0L));
        }

        // Price filter — subquery on Room.basePrice
        if (req.getMinPrice() != null || req.getMaxPrice() != null) {
            Subquery<java.math.BigDecimal> priceSub = query.subquery(java.math.BigDecimal.class);
            Root<Room> roomRoot = priceSub.from(Room.class);
            priceSub.select(cb.least(roomRoot.<java.math.BigDecimal>get("basePrice")))
                    .where(
                            cb.equal(roomRoot.get("hotel"), root),
                            cb.equal(roomRoot.get("isActive"), true),
                            cb.equal(roomRoot.get("status"), Room.RoomStatus.AVAILABLE)
                    );
            if (req.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(priceSub, req.getMinPrice()));
            }
            if (req.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(priceSub, req.getMaxPrice()));
            }
        }

        // Guest filter
        if (req.getGuests() != null) {
            Subquery<Long> guestSub = query.subquery(Long.class);
            Root<Room> roomRoot = guestSub.from(Room.class);
            guestSub.select(cb.count(roomRoot))
                    .where(
                            cb.equal(roomRoot.get("hotel"), root),
                            cb.equal(roomRoot.get("isActive"), true),
                            cb.equal(roomRoot.get("status"), Room.RoomStatus.AVAILABLE),
                            cb.greaterThanOrEqualTo(roomRoot.get("maxOccupancy"), req.getGuests())
                    );
            predicates.add(cb.greaterThan(guestSub, 0L));
        }

        // Availability filter
        if (req.getCheckIn() != null && req.getCheckOut() != null) {
            Subquery<Long> availSub = query.subquery(Long.class);
            Root<Room> roomRoot = availSub.from(Room.class);
            availSub.select(cb.count(roomRoot))
                    .where(
                            cb.equal(roomRoot.get("hotel"), root),
                            cb.equal(roomRoot.get("isActive"), true),
                            cb.or(
                                    cb.isNull(roomRoot.get("currentCheckInDate")),
                                    cb.lessThanOrEqualTo(
                                            roomRoot.get("currentCheckOutDate"), req.getCheckIn()),
                                    cb.greaterThanOrEqualTo(
                                            roomRoot.get("currentCheckInDate"), req.getCheckOut())
                            )
                    );
            predicates.add(cb.greaterThan(availSub, 0L));
        }

        return predicates;
    }

    // -------------------------------------------------------------------------
    // Sorting — multiselect ensures ORDER BY column is in SELECT (MySQL fix)
    // -------------------------------------------------------------------------
    private void applySort(HotelSearchRequestDTO req, CriteriaBuilder cb,
                           CriteriaQuery<Tuple> query, Root<Hotel> root) {

        if (req.getSortBy() == null) {
            query.multiselect(
                    root.get("id").alias("id"),
                    root.get("createdAt").alias("sortCol")
            );
            query.orderBy(cb.desc(root.get("createdAt")));
            return;
        }

        boolean asc = "ASC".equalsIgnoreCase(req.getSortDirection());

        switch (req.getSortBy()) {

            case STAR_RATING -> {
                query.multiselect(
                        root.get("id").alias("id"),
                        root.get("starRating").alias("sortCol")
                );
                query.orderBy(asc
                        ? cb.asc(root.get("starRating"))
                        : cb.desc(root.get("starRating")));
            }

            case PRICE_LOW_HIGH, PRICE_HIGH_LOW -> {
                Subquery<java.math.BigDecimal> priceSub = query.subquery(java.math.BigDecimal.class);
                Root<Room> roomRoot = priceSub.from(Room.class);
                priceSub.select(cb.least(roomRoot.<java.math.BigDecimal>get("basePrice")))
                        .where(
                                cb.equal(roomRoot.get("hotel"), root),
                                cb.equal(roomRoot.get("isActive"), true),
                                cb.equal(roomRoot.get("status"), Room.RoomStatus.AVAILABLE)
                        );
                query.multiselect(
                        root.get("id").alias("id"),
                        priceSub.alias("sortCol")
                );
                query.orderBy(req.getSortBy() == HotelSearchRequestDTO.SortOption.PRICE_LOW_HIGH
                        ? cb.asc(priceSub)
                        : cb.desc(priceSub));
            }

            default -> {
                // NEWEST
                query.multiselect(
                        root.get("id").alias("id"),
                        root.get("createdAt").alias("sortCol")
                );
                query.orderBy(asc
                        ? cb.asc(root.get("createdAt"))
                        : cb.desc(root.get("createdAt")));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Entity → DTO
    // -------------------------------------------------------------------------
    private HotelSearchResultDTO toDTO(Hotel hotel,
                                       List<HotelImage> images,
                                       List<Room> rooms) {

        // Primary hotel-level image only (exclude room images)
        String primaryImageUrl = images.stream()
                .filter(img -> img.getRoom() == null)
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .map(HotelImage::getUrl)
                .findFirst()
                .orElseGet(() -> images.stream()
                        .filter(img -> img.getRoom() == null)
                        .map(HotelImage::getUrl)
                        .findFirst()
                        .orElse(null));

        // Lowest basePrice from active + available rooms only
        java.math.BigDecimal startingPrice = rooms.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsActive()))
                .filter(r -> r.getStatus() == Room.RoomStatus.AVAILABLE)
                .map(Room::getBasePrice)
                .filter(Objects::nonNull)
                .min(java.math.BigDecimal::compareTo)
                .orElse(null);

        // Amenities
        List<HotelSearchResultDTO.AmenityDTO> amenityDTOs = hotel.getAmenities().stream()
                .map(a -> HotelSearchResultDTO.AmenityDTO.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .iconClass(a.getIconClass())
                        .category(a.getCategory())
                        .build())
                .collect(Collectors.toList());

        return HotelSearchResultDTO.builder()
                .hotelId(hotel.getId())
                .name(hotel.getName())
                .description(hotel.getDescription())
                .address(hotel.getAddress())
                .city(hotel.getCity())
                .state(hotel.getState())
                .country(hotel.getCountry())
                .postalCode(hotel.getPostalCode())
                .phoneNumber(hotel.getPhoneNumber())
                .email(hotel.getEmail())
                .website(hotel.getWebsite())
                .starRating(hotel.getStarRating())
                .primaryImageUrl(primaryImageUrl)
                .startingFromPrice(startingPrice)
                .amenities(amenityDTOs)
                .build();
    }
}
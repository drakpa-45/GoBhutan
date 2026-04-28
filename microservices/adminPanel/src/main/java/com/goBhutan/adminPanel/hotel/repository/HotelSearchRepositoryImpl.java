package com.goBhutan.adminPanel.hotel.repository;

import com.goBhutan.adminPanel.hotel.dto.HotelSearchRequestDTO;
import com.goBhutan.adminPanel.hotel.dto.HotelSearchResultDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HotelSearchRepositoryImpl implements HotelSearchRepositoryCustom {

    @Autowired
    private EntityManager em;

    @Override
    public Page<HotelSearchResultDTO> searchHotels(HotelSearchRequestDTO req, Pageable pageable) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        // ---- Count query ----
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<?> countRoot = countQuery.from(com.goBhutan.adminPanel.hotel.entity.Hotel.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(buildPredicates(req, cb, countRoot).toArray(new Predicate[0]));
        Long total = em.createQuery(countQuery).getSingleResult();

        // ---- Data query ----
        CriteriaQuery<HotelSearchResultDTO> dataQuery = cb.createQuery(HotelSearchResultDTO.class);
        Root<?> root = dataQuery.from(com.goBhutan.adminPanel.hotel.entity.Hotel.class);

        // Subquery: total bookings per hotel (popularity)
        Subquery<Long> bookingCount = dataQuery.subquery(Long.class);
        Root<?> bookingRoot = bookingCount.from(com.goBhutan.adminPanel.hotel.entity.Booking.class);
        bookingCount.select(cb.count(bookingRoot))
                .where(cb.equal(bookingRoot.get("hotel").get("id"), root.get("id")));

        dataQuery.multiselect(
                root.get("id").alias("hotelId"),
                root.get("name"),
                root.get("city"),
                root.get("district"),
                root.get("thumbnailUrl"),
                root.get("starRating"),
                root.get("averageRating"),
                root.get("reviewCount"),
                root.get("pricePerNight"),
                root.get("currency"),
                bookingCount.alias("totalBookings")
        );

        dataQuery.where(buildPredicates(req, cb, root).toArray(new Predicate[0]));

        TypedQuery<HotelSearchResultDTO> query = em.createQuery(dataQuery);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<HotelSearchResultDTO> content = query.getResultList();
        return new PageImpl<>(content, pageable, total);
    }

    private List<Predicate> buildPredicates(HotelSearchRequestDTO req,
                                            CriteriaBuilder cb, Root<?> root) {
        List<Predicate> predicates = new ArrayList<>();

        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("name")),
                    "%" + req.getKeyword().toLowerCase() + "%"));
        }
        if (req.getCity() != null && !req.getCity().isBlank()) {
            predicates.add(cb.equal(cb.lower(root.get("city")),
                    req.getCity().toLowerCase()));
        }
        if (req.getDistrict() != null) {
            predicates.add(cb.equal(cb.lower(root.get("district")),
                    req.getDistrict().toLowerCase()));
        }
        if (req.getMinRating() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), req.getMinRating()));
        }
        if (req.getMinStars() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("starRating"), req.getMinStars()));
        }
        if (req.getMaxStars() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("starRating"), req.getMaxStars()));
        }
        if (req.getMinPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("pricePerNight"), req.getMinPrice()));
        }
        if (req.getMaxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("pricePerNight"), req.getMaxPrice()));
        }

        return predicates;
    }
}
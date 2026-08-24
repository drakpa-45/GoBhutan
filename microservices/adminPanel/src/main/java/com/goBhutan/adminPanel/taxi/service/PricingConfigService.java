package com.goBhutan.adminPanel.taxi.service;

import com.goBhutan.adminPanel.taxi.dto.request.PricingConfigRequest;
import com.goBhutan.adminPanel.taxi.dto.response.PricingConfigResponse;
import com.goBhutan.adminPanel.taxi.entity.PricingConfig;
import com.goBhutan.adminPanel.taxi.enums.TripCategory;
import com.goBhutan.adminPanel.taxi.enums.TripMode;
import com.goBhutan.adminPanel.taxi.repository.PricingConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PricingConfigService {

    private final PricingConfigRepository repo;

    // ── Get all (admin dashboard lists all 4 rows) ────────────────────────────

    public List<PricingConfigResponse> getAll() {
        return repo.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Get one by category + mode ────────────────────────────────────────────

    public PricingConfigResponse getOne(TripCategory category, TripMode mode) {
        return toResponse(findOrThrow(category, mode));
    }

    // ── Get by ID ─────────────────────────────────────────────────────────────

    public PricingConfigResponse getById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pricing config not found with id: " + id)));
    }

    // ── Create (if row doesn't exist yet) ─────────────────────────────────────

    @Transactional
    public PricingConfigResponse create(PricingConfigRequest req) {
        if (repo.findByTripCategoryAndTripMode(req.getTripCategory(), req.getTripMode()).isPresent()) {
            throw new IllegalStateException(
                    "Pricing config already exists for " + req.getTripCategory()
                    + " + " + req.getTripMode() + ". Use PUT to update.");
        }
        PricingConfig entity = toEntity(new PricingConfig(), req);
        return toResponse(repo.save(entity));
    }

    // ── Full update by ID ─────────────────────────────────────────────────────

    @Transactional
    public PricingConfigResponse updateById(Long id, PricingConfigRequest req) {
        PricingConfig entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pricing config not found with id: " + id));
        return toResponse(repo.save(toEntity(entity, req)));
    }

    // ── Full update by category + mode (most natural for admin) ──────────────

    @Transactional
    public PricingConfigResponse updateByType(TripCategory category, TripMode mode,
                                               PricingConfigRequest req) {
        PricingConfig entity = findOrThrow(category, mode);
        return toResponse(repo.save(toEntity(entity, req)));
    }

    // ── Partial update by ID (PATCH — only non-null fields overwritten) ───────

    @Transactional
    public PricingConfigResponse patchById(Long id, PricingConfigRequest req) {
        PricingConfig entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pricing config not found with id: " + id));
        applyPatch(entity, req);
        return toResponse(repo.save(entity));
    }

    // ── Partial update by category + mode ────────────────────────────────────

    @Transactional
    public PricingConfigResponse patchByType(TripCategory category, TripMode mode,
                                              PricingConfigRequest req) {
        PricingConfig entity = findOrThrow(category, mode);
        applyPatch(entity, req);
        return toResponse(repo.save(entity));
    }

    // ── Seed all 4 default rows (run once on first deploy) ───────────────────

    @Transactional
    public List<PricingConfigResponse> seedDefaults() {
        record DefaultRow(TripCategory cat, TripMode mode,
                          double base, double rate, double night,
                          double premium, double deposit, double comm, double surge) {}

        List<DefaultRow> defaults = Arrays.asList(
            new DefaultRow(TripCategory.INTRA_DZONGKHAG, TripMode.PULL,
                           40, 12, 30, 0,  0,  15, 2.0),
            new DefaultRow(TripCategory.INTRA_DZONGKHAG, TripMode.RESERVED,
                           40, 12, 30, 20, 20, 15, 2.0),
            new DefaultRow(TripCategory.INTER_DZONGKHAG, TripMode.PULL,
                           0,  5,  0,  0,  0,  12, 2.0),
            new DefaultRow(TripCategory.INTER_DZONGKHAG, TripMode.RESERVED,
                           0,  5,  0,  15, 30, 12, 2.0)
        );

        return defaults.stream()
                .filter(d -> repo.findByTripCategoryAndTripMode(d.cat(), d.mode()).isEmpty())
                .map(d -> {
                    PricingConfig c = new PricingConfig();
                    c.setTripCategory(d.cat());
                    c.setTripMode(d.mode());
                    c.setBaseFare(bd(d.base()));
                    c.setRatePerKm(bd(d.rate()));
                    c.setNightSurcharge(bd(d.night()));
                    c.setReservedPremiumPct(bd(d.premium()));
                    c.setDepositPct(bd(d.deposit()));
                    c.setCommissionPct(bd(d.comm()));
                    c.setMaxSurgeMultiplier(bd(d.surge()));
                    return toResponse(repo.save(c));
                })
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private PricingConfig findOrThrow(TripCategory category, TripMode mode) {
        return repo.findByTripCategoryAndTripMode(category, mode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pricing config not found for " + category + " + " + mode));
    }

    /** Overwrites all fields from request onto entity */
    private PricingConfig toEntity(PricingConfig entity, PricingConfigRequest req) {
        entity.setTripCategory(req.getTripCategory());
        entity.setTripMode(req.getTripMode());
        entity.setBaseFare(req.getBaseFare());
        entity.setRatePerKm(req.getRatePerKm());
        entity.setNightSurcharge(req.getNightSurcharge());
        entity.setReservedPremiumPct(req.getReservedPremiumPct());
        entity.setDepositPct(req.getDepositPct());
        entity.setCommissionPct(req.getCommissionPct());
        entity.setMaxSurgeMultiplier(req.getMaxSurgeMultiplier());
        return entity;
    }

    /** Only overwrites non-null fields (PATCH behaviour) */
    private void applyPatch(PricingConfig entity, PricingConfigRequest req) {
        if (req.getBaseFare()           != null) entity.setBaseFare(req.getBaseFare());
        if (req.getRatePerKm()          != null) entity.setRatePerKm(req.getRatePerKm());
        if (req.getNightSurcharge()     != null) entity.setNightSurcharge(req.getNightSurcharge());
        if (req.getReservedPremiumPct() != null) entity.setReservedPremiumPct(req.getReservedPremiumPct());
        if (req.getDepositPct()         != null) entity.setDepositPct(req.getDepositPct());
        if (req.getCommissionPct()      != null) entity.setCommissionPct(req.getCommissionPct());
        if (req.getMaxSurgeMultiplier() != null) entity.setMaxSurgeMultiplier(req.getMaxSurgeMultiplier());
        // tripCategory and tripMode intentionally not patchable — would break uniqueness
    }

    private PricingConfigResponse toResponse(PricingConfig e) {
        String label = formatLabel(e.getTripCategory(), e.getTripMode());
        return PricingConfigResponse.builder()
                .id(e.getId())
                .tripCategory(e.getTripCategory())
                .tripMode(e.getTripMode())
                .baseFare(e.getBaseFare())
                .ratePerKm(e.getRatePerKm())
                .nightSurcharge(e.getNightSurcharge())
                .reservedPremiumPct(e.getReservedPremiumPct())
                .depositPct(e.getDepositPct())
                .commissionPct(e.getCommissionPct())
                .maxSurgeMultiplier(e.getMaxSurgeMultiplier())
                .updatedAt(e.getUpdatedAt())
                .label(label)
                .build();
    }

    private String formatLabel(TripCategory cat, TripMode mode) {
        String c = cat == TripCategory.INTRA_DZONGKHAG ? "Intra-dzongkhag" : "Inter-dzongkhag";
        String m = mode == TripMode.PULL ? "Pull" : "Reserved";
        return c + " — " + m;
    }

    private java.math.BigDecimal bd(double v) {
        return java.math.BigDecimal.valueOf(v);
    }
}

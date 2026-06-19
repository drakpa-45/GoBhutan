package com.goBhutan.adminPanel.taxi.controller;

import com.goBhutan.adminPanel.taxi.dto.request.PricingConfigRequest;
import com.goBhutan.adminPanel.taxi.dto.response.PricingConfigResponse;
import com.goBhutan.adminPanel.taxi.enums.TripCategory;
import com.goBhutan.adminPanel.taxi.enums.TripMode;
import com.goBhutan.adminPanel.taxi.service.PricingConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin endpoints for managing Yaya taxi fare pricing.
 *
 * Base URL: /api/admin/taxi/pricing
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  Endpoint                                   │ Purpose                   │
 * ├─────────────────────────────────────────────┼───────────────────────────┤
 * │  GET    /                                   │ List all 4 configs        │
 * │  GET    /{id}                               │ Get by DB id              │
 * │  GET    /type?category=&mode=               │ Get by trip type          │
 * │  POST   /                                   │ Create new config         │
 * │  POST   /seed                               │ Seed 4 default rows       │
 * │  PUT    /{id}                               │ Full update by id         │
 * │  PUT    /type?category=&mode=               │ Full update by type       │
 * │  PATCH  /{id}                               │ Partial update by id      │
 * │  PATCH  /type?category=&mode=               │ Partial update by type    │
 * └─────────────────────────────────────────────┴───────────────────────────┘
 */
@RestController
@RequestMapping("/api/admin/taxi/pricing")
@RequiredArgsConstructor
public class PricingConfigController {

    private final PricingConfigService service;

    // ── READ ──────────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/taxi/pricing
     * Returns all 4 pricing configs (one per category+mode combination).
     * Admin dashboard loads this to populate the pricing table.
     */
    @GetMapping
    public ResponseEntity<List<PricingConfigResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    /**
     * GET /api/admin/taxi/pricing/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PricingConfigResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * GET /api/admin/taxi/pricing/type?category=INTRA_DZONGKHAG&mode=PULL
     * More intuitive than using the DB id — admin can query by trip type.
     */
    @GetMapping("/type")
    public ResponseEntity<PricingConfigResponse> getByType(
            @RequestParam TripCategory category,
            @RequestParam TripMode mode) {
        return ResponseEntity.ok(service.getOne(category, mode));
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * POST /api/admin/taxi/pricing
     * Creates a new pricing config row.
     * Throws 400 if a row for that category+mode already exists.
     *
     * Body example (Intra Pull):
     * {
     *   "tripCategory": "INTRA_DZONGKHAG",
     *   "tripMode": "PULL",
     *   "baseFare": 40,
     *   "ratePerKm": 12,
     *   "nightSurcharge": 30,
     *   "commissionPct": 15,
     *   "maxSurgeMultiplier": 2.0
     * }
     */
    @PostMapping
    public ResponseEntity<PricingConfigResponse> create(
            @Valid @RequestBody PricingConfigRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    /**
     * POST /api/admin/taxi/pricing/seed
     * Inserts the 4 default pricing rows (skips any that already exist).
     * Safe to call multiple times — idempotent.
     * Useful on first deployment instead of running the SQL seed manually.
     */
    @PostMapping("/seed")
    public ResponseEntity<List<PricingConfigResponse>> seed() {
        return ResponseEntity.ok(service.seedDefaults());
    }

    // ── FULL UPDATE (PUT) ─────────────────────────────────────────────────────

    /**
     * PUT /api/admin/taxi/pricing/{id}
     * Replaces all fields. All pricing fields must be provided.
     *
     * Body example — update commission on Intra Pull:
     * {
     *   "tripCategory": "INTRA_DZONGKHAG",
     *   "tripMode": "PULL",
     *   "baseFare": 40,
     *   "ratePerKm": 12,
     *   "nightSurcharge": 30,
     *   "commissionPct": 18,       ← changed
     *   "maxSurgeMultiplier": 2.0
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<PricingConfigResponse> updateById(
            @PathVariable Long id,
            @Valid @RequestBody PricingConfigRequest request) {
        return ResponseEntity.ok(service.updateById(id, request));
    }

    /**
     * PUT /api/admin/taxi/pricing/type?category=INTRA_DZONGKHAG&mode=PULL
     * Same as above but addressed by trip type — admin doesn't need to know the DB id.
     */
    @PutMapping("/type")
    public ResponseEntity<PricingConfigResponse> updateByType(
            @RequestParam TripCategory category,
            @RequestParam TripMode mode,
            @Valid @RequestBody PricingConfigRequest request) {
        return ResponseEntity.ok(service.updateByType(category, mode, request));
    }

    // ── PARTIAL UPDATE (PATCH) ────────────────────────────────────────────────

    /**
     * PATCH /api/admin/taxi/pricing/{id}
     * Updates only the fields provided in the body — null fields are ignored.
     * Most useful for admin panel: change just commissionPct without sending everything.
     *
     * Body example — just raise the night surcharge:
     * {
     *   "nightSurcharge": 50
     * }
     *
     * Note: tripCategory and tripMode cannot be changed via PATCH
     * (they are the natural key — changing them would create a different row).
     */
    @PatchMapping("/{id}")
    public ResponseEntity<PricingConfigResponse> patchById(
            @PathVariable Long id,
            @RequestBody PricingConfigRequest request) {   // no @Valid — partial is fine
        return ResponseEntity.ok(service.patchById(id, request));
    }

    /**
     * PATCH /api/admin/taxi/pricing/type?category=INTRA_DZONGKHAG&mode=RESERVED
     * Partial update addressed by trip type.
     *
     * Body example — just change the reserved premium:
     * {
     *   "reservedPremiumPct": 25
     * }
     */
    @PatchMapping("/type")
    public ResponseEntity<PricingConfigResponse> patchByType(
            @RequestParam TripCategory category,
            @RequestParam TripMode mode,
            @RequestBody PricingConfigRequest request) {
        return ResponseEntity.ok(service.patchByType(category, mode, request));
    }
}

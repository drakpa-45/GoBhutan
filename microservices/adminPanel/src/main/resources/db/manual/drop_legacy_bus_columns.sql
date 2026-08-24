-- Manual cleanup for legacy bus schema columns.
-- Run this after deploying code that no longer maps these columns.
--
-- Impact:
-- - tbl_bs_bus_routes.estimated_duration is replaced by estimated_duration_minutes.
-- - tbl_bs_bus_routes.custom_fare is replaced by base_fare + app_charges.
-- - tbl_bs_schedules.price is replaced by base_fare + app_charges + final_fare.
--
-- Optional data preservation before dropping:
-- UPDATE tbl_bs_bus_routes
-- SET estimated_duration_minutes = estimated_duration
-- WHERE estimated_duration_minutes IS NULL
--   AND estimated_duration IS NOT NULL;
--
-- UPDATE tbl_bs_bus_routes
-- SET app_charges = GREATEST(custom_fare - base_fare, 0)
-- WHERE custom_fare IS NOT NULL
--   AND base_fare IS NOT NULL
--   AND (app_charges IS NULL OR app_charges = 0);
--
-- UPDATE tbl_bs_schedules
-- SET final_fare = price
-- WHERE final_fare IS NULL
--   AND price IS NOT NULL;

SET @drop_estimated_duration = (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE tbl_bs_bus_routes DROP COLUMN estimated_duration',
        'SELECT ''tbl_bs_bus_routes.estimated_duration already absent'''
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'tbl_bs_bus_routes'
      AND column_name = 'estimated_duration'
);
PREPARE stmt FROM @drop_estimated_duration;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_custom_fare = (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE tbl_bs_bus_routes DROP COLUMN custom_fare',
        'SELECT ''tbl_bs_bus_routes.custom_fare already absent'''
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'tbl_bs_bus_routes'
      AND column_name = 'custom_fare'
);
PREPARE stmt FROM @drop_custom_fare;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_schedule_price = (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE tbl_bs_schedules DROP COLUMN price',
        'SELECT ''tbl_bs_schedules.price already absent'''
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'tbl_bs_schedules'
      AND column_name = 'price'
);
PREPARE stmt FROM @drop_schedule_price;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

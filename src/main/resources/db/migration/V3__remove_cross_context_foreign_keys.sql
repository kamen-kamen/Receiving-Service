-- V3: Remove Foreign Key constraints between different Bounded Contexts to increase autonomy.

-- 1. Decouple Goods Receipt (Fact) from Inbound Delivery (Plan)
ALTER TABLE goods_receipts
    DROP CONSTRAINT IF EXISTS fk_goods_receipts_inbound_delivery;

-- 2. Decouple Worker Receiving Session from other BCs
ALTER TABLE worker_receiving_sessions
    DROP CONSTRAINT IF EXISTS fk_wrs_inbound_delivery,
    DROP CONSTRAINT IF EXISTS fk_wrs_receipt,
    DROP CONSTRAINT IF EXISTS fk_wrs_worker;

-- 3. Break the cycle between Worker Receiving Session and Received Handling Unit
ALTER TABLE worker_receiving_sessions
    DROP CONSTRAINT IF EXISTS fk_wrs_current_unit;

ALTER TABLE received_handling_units
    DROP CONSTRAINT IF EXISTS fk_rhu_worker_receiving_session;

-- 4. Denormalize warehouse_id into goods_receipts to make it self-sufficient
ALTER TABLE goods_receipts
    ADD COLUMN warehouse_id VARCHAR(255);

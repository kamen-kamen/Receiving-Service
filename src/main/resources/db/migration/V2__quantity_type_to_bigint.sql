-- Изменение типа данных в таблице contents
ALTER TABLE contents
    ALTER COLUMN quantity TYPE BIGINT USING quantity::BIGINT;

-- Изменение типа данных в таблице received_contents
ALTER TABLE received_contents
    ALTER COLUMN quantity TYPE BIGINT USING quantity::BIGINT;
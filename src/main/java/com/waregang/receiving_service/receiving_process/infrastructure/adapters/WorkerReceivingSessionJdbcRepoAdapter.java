package com.waregang.receiving_service.receiving_process.infrastructure.adapters;

import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivingMode;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSession;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSessionStatus;
import com.waregang.receiving_service.receiving_process.domain.ports.WorkerReceivingSessionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.*;

@RequiredArgsConstructor

@Repository
@ConditionalOnProperty(
        name = "app.adapters.worker-receiving-session-repository-adapter",
        havingValue = "jdbc")
public class WorkerReceivingSessionJdbcRepoAdapter implements WorkerReceivingSessionRepositoryPort {
    private final JdbcTemplate jdbcTemplate;
    private final ApplicationEventPublisher eventPublisher;

    private static final RowMapper<WorkerReceivingSession> ROW_MAPPER = (rs, rowNum) -> {
        String currentUnitIdStr = rs.getString("current_unit_id");
        UUID currentUnitId = currentUnitIdStr != null ? UUID.fromString(currentUnitIdStr) : null;

        return WorkerReceivingSession.toDomain(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("worker_id")),
                UUID.fromString(rs.getString("receipt_id")),
                UUID.fromString(rs.getString("inbound_delivery_id")),
                WorkerReceivingSessionStatus.valueOf(rs.getString("worker_receiving_session_status")),
                ReceivingMode.valueOf(rs.getString("receiving_mode")),
                rs.getString("current_unit_lpn_path"),
                currentUnitId
        );
    };

    @Override
    public boolean existsByReceiptIdAndStatus(UUID receiptId, WorkerReceivingSessionStatus status) {
        Boolean exists = jdbcTemplate.queryForObject(
                """
                        SELECT EXISTS(
                            SELECT 1 FROM worker_receiving_sessions
                            WHERE receipt_id = ? AND worker_receiving_session_status = ?
                        )""",
                Boolean.class, receiptId, status.name()
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsByWorkerIdAndStatus(UUID workerId, WorkerReceivingSessionStatus status) {
        Boolean exists = jdbcTemplate.queryForObject(
                """
                        SELECT EXISTS(
                            SELECT 1 FROM worker_receiving_sessions
                            WHERE worker_id = ? AND worker_receiving_session_status = ?
                        )""",
                Boolean.class, workerId, status.name()
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public WorkerReceivingSession save(WorkerReceivingSession session) {
        jdbcTemplate.update(
                """
                        INSERT INTO worker_receiving_sessions
                        (id, worker_id, receipt_id, inbound_delivery_id, worker_receiving_session_status,
                         receiving_mode, current_unit_lpn_path, current_unit_id)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                session.getId(),
                session.getWorkerId(),
                session.getReceiptId(),
                session.getInboundDeliveryId(),
                session.getStatus().name(),
                session.getReceivingMode().name(),
                session.getCurrentUnitLpnPath(),
                session.getCurrentUnitId()
        );

        session.pullDomainEvents().forEach(eventPublisher::publishEvent);
        return session;
    }

    @Override
    public WorkerReceivingSession update(WorkerReceivingSession session) {
        int affectedRows = jdbcTemplate.update(
                """
                        UPDATE worker_receiving_sessions
                        SET worker_receiving_session_status = ?, current_unit_lpn_path = ?, current_unit_id = ?
                        WHERE id = ?
                        """,
                session.getStatus().name(),
                session.getCurrentUnitLpnPath(),
                session.getCurrentUnitId(),
                session.getId()
        );

        if (affectedRows == 0) {
            throw AppException.of(ReceivingErrorCode.WORKER_SESSION_NOT_FOUND)
                    .with("session_id", session.getId());
        }

        session.pullDomainEvents().forEach(eventPublisher::publishEvent);
        return session;
    }

    @Override
    public Optional<WorkerReceivingSession> findByWorkerIdAndStatus(UUID workerId, WorkerReceivingSessionStatus status) {
        List<WorkerReceivingSession> results = jdbcTemplate.query(
                """
                        SELECT * FROM worker_receiving_sessions
                        WHERE worker_id = ? AND worker_receiving_session_status = ?
                        """,
                ROW_MAPPER, workerId, status.name()
        );
        return results.stream().findFirst();
    }

    @Override
    public Set<WorkerReceivingSession> findAll() {
        return new HashSet<>(jdbcTemplate.query("SELECT * FROM worker_receiving_sessions", ROW_MAPPER));
    }
    //TODO: Pagination
}
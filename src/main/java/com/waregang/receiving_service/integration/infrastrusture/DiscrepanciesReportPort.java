package com.waregang.receiving_service.integration.infrastrusture;

import com.waregang.receiving_service.integration.infrastrusture.dto.DiscrepanciesReport;

public interface DiscrepanciesReportPort {
    void sendReport(DiscrepanciesReport report);
}

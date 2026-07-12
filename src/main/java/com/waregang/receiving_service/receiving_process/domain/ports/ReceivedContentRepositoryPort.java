package com.waregang.receiving_service.receiving_process.domain.ports;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivedContent;

import java.util.List;

public interface ReceivedContentRepositoryPort {
    ReceivedContent save(ReceivedContent content);
    List<ReceivedContent> findAll();
}
package com.waregang.receiving_service.common.validation;


import com.waregang.receiving_service.inbound_delivery.api.dto.CreateContentRequest;
import com.waregang.receiving_service.inbound_delivery.api.dto.CreateDeliveryRequest;
import com.waregang.receiving_service.inbound_delivery.api.dto.CreateUnitRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DeliveryAsnValidator implements ConstraintValidator<ValidAsn, CreateDeliveryRequest> {

    @Override
    public boolean isValid(CreateDeliveryRequest request, ConstraintValidatorContext context) {
        if (request == null || request.unitRequests() == null || request.contents() == null) {
            return true;
        }

        Set<String> unitLpns = request.unitRequests().stream()
                .map(CreateUnitRequest::lpn)
                .collect(Collectors.toSet());

        boolean isValid = true;

        // Check 1: All parentLpns must exist in the request
        for (CreateContentRequest content : request.contents()) {
            if (!unitLpns.contains(content.parentLpn())) {
                isValid = false;
                addParentLpnMissingViolation(context);
            }
        }

        for (CreateUnitRequest unit : request.unitRequests()) {
            if (unit.parentLpn() != null && !unitLpns.contains(unit.parentLpn())) {
                isValid = false;
                addParentLpnMissingViolation(context);
            }
        }

        // Check 2: All leaf units must have content
        Set<String> parentLpns = request.unitRequests().stream()
                .map(CreateUnitRequest::parentLpn)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> contentContainerLpns = request.contents().stream()
                .map(CreateContentRequest::parentLpn)
                .collect(Collectors.toSet());

        for (CreateUnitRequest unit : request.unitRequests()) {
            boolean isLeaf = !parentLpns.contains(unit.lpn());
            boolean hasContent = contentContainerLpns.contains(unit.lpn());

            if (isLeaf && !hasContent) {
                isValid = false;
                addEmptyLeafViolation(context);
            }
        }

        return isValid;
    }

    private void addParentLpnMissingViolation(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        String message = "A non-existent LPN is referenced.";
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

    private void addEmptyLeafViolation(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        String message = "A leaf unit must not be empty.";
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}

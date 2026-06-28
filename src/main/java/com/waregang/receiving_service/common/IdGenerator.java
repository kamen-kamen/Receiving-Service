package com.waregang.receiving_service.common;


import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class IdGenerator {
    private final static NoArgGenerator v7Generator = Generators.timeBasedEpochGenerator();

    //private IdGenerator() {}

    public static UUID generate() {
        return v7Generator.generate();
    }
}

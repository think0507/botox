package com.botox.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TemperatureUpdateRequest {
    private int temperatureChange;
}

package com.caretta.proje.uyelik.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record KoltukSatinAlmaRequest(
        @Min(value = 1, message = "Koltuk sayisi en az 1 olmali")
        @Max(value = 100, message = "Tek seferde en fazla 100 koltuk satin alinabilir")
        int koltukSayisi
) {
}

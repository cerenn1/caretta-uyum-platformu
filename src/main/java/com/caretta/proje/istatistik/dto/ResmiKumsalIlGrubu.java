package com.caretta.proje.istatistik.dto;

import java.util.List;

/**
 * "Etkimiz / Kapsam Alanimiz" bolumunde bir ilin altinda gosterilecek resmi
 * kumsal adlari - {@link ResmiKumsal#TUMU} listesinin il bazinda gruplanmis hali.
 */
public record ResmiKumsalIlGrubu(String il, List<String> kumsallar) {
}

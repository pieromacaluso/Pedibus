package it.polito.ai.mmap.esercitazione3.resources;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.ResourceSupport;

/**
 * POST /reservations/{nome_linea}/{data}
 * un oggetto JSON contenente:
 * il nome dell’alunno da trasportare
 * l’identificatore della fermata a cui sale/scende
 * il verso di percorrenza (andata/ritorno)
 *
 */

@Data
@Builder
public class PrenotazioneResource
{
    private String cfChild;
    private Integer idFermata;
    private Boolean verso;
}

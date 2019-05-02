package it.polito.ai.mmap.esercitazione3.resources;

import it.polito.ai.mmap.esercitazione3.objectDTO.PrenotazioneDTO;
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

@EqualsAndHashCode(callSuper = true)
@Data
public class PrenotazioneResource extends ResourceSupport
{
    String nomeAlunno;
    Integer idFermata;
    Boolean verso;

    public PrenotazioneResource() {
        super();
    }

    public PrenotazioneResource(PrenotazioneDTO dto) {
        nomeAlunno = dto.getNomeAlunno();
        idFermata = dto.getIdFermata();
        verso = dto.getVerso();
    }
}

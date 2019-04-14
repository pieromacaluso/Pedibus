package it.polito.ai.mmap.esercitazione2.entity;

import lombok.Data;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

//TODO capire se Serializable serve

@Data
public class CompositeKeyPrenotazione implements Serializable {
    private String nomeAlunno;
    private LocalDateTime data;
    private boolean verso;

    public CompositeKeyPrenotazione(String nomeAlunno, LocalDateTime data, boolean verso)
    {
        this.nomeAlunno = nomeAlunno;
        this.data = data;
        this.verso = verso;
    }

    public CompositeKeyPrenotazione(String idPrenotazione)
    {
        String[] key = idPrenotazione.split("_");
        nomeAlunno = key[0];
        data = LocalDateTime.parse(key[1]);
        verso = Boolean.parseBoolean(key[2]);

    }
    @Override
    public String toString()
    {
        return nomeAlunno + "_" + data.toString() + "_" + verso;
    }
}
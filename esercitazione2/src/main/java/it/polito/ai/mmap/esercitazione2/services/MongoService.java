package it.polito.ai.mmap.esercitazione2.services;

import it.polito.ai.mmap.esercitazione2.entity.CompositeKeyPrenotazione;
import it.polito.ai.mmap.esercitazione2.entity.FermataEntity;
import it.polito.ai.mmap.esercitazione2.entity.LineaEntity;
import it.polito.ai.mmap.esercitazione2.entity.PrenotazioneEntity;
import it.polito.ai.mmap.esercitazione2.objectDTO.FermataDTO;
import it.polito.ai.mmap.esercitazione2.objectDTO.LineaDTO;
import it.polito.ai.mmap.esercitazione2.objectDTO.PrenotazioneDTO;
import it.polito.ai.mmap.esercitazione2.repository.FermataRepository;
import it.polito.ai.mmap.esercitazione2.repository.LineaRepository;
import it.polito.ai.mmap.esercitazione2.repository.PrenotazioneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MongoService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LineaRepository lineaRepository;

    @Autowired
    private FermataRepository fermataRepository;

    @Autowired
    private PrenotazioneRepository prenotazioneRepository;


    /**
     * Salva una linea sul DB
     *
     * @param lineaDTO
     */
    public void addLinea(LineaDTO lineaDTO) {
        LineaEntity lineaEntity = new LineaEntity(lineaDTO);
        lineaRepository.save(lineaEntity);

    }

    /**
     * Salva fermate dul DB
     *
     * @param listaFermate
     */

    public void addFermate(List<FermataDTO> listaFermate) {

        fermataRepository.saveAll(listaFermate
                .stream()
                .map(FermataEntity::new)
                .collect(Collectors.toList()));
    }


    public List<LineaEntity> getAllLines() {
        return lineaRepository.findAll();
    }

    public LineaEntity getLine(String lineName) {
        return lineaRepository.findByNome(lineName);       //ToDo check unica linea con tale nome, forse farlo in fase di caricamento db
    }

    /**
     * Legge da db tutte le fermate presenti nella lista idFermata
     *
     * @param idFermate
     * @return ordinato per orario
     */
    public List<FermataEntity> getFermate(List<Integer> idFermate) {
        List<FermataEntity> fermate = (List<FermataEntity>) fermataRepository.findAllById(idFermate);
        fermate.sort(Comparator.comparing(FermataEntity::getOrario));                                  //ordinate per orario e non per id
        return fermate;
    }

    /**
     * Aggiunge una prenotazione al db. Metodo utilizzato anche per update
     * in quanto se _id presente mongoDb sostituisce l'intero documento.
     * TODO deve restituire un id_prenotazione
     *
     * @param prenotazioneDTO
     */
    public CompositeKeyPrenotazione addPrenotazione(PrenotazioneDTO prenotazioneDTO) {
        PrenotazioneEntity prenotazioneEntity = new PrenotazioneEntity(prenotazioneDTO);
        return prenotazioneRepository.save(prenotazioneEntity).getId();
    }

    /**
     * TODO implementazione stupida che fa find/delete/save, sarebbe da raggruppare in unica operazione definita per la prenotazioneRepository
     * @param prenotazioneDTO contiene i nuovi dati
     * @param compositeKeyPrenotazione ci permette di identificare la prenotazione da modificare
     */
    public void updatePrenotazione(PrenotazioneDTO prenotazioneDTO, CompositeKeyPrenotazione compositeKeyPrenotazione)
    {
        PrenotazioneEntity prenotazioneEntity = prenotazioneRepository.findById(compositeKeyPrenotazione);
        //TODO al posto della delete si può provare a usare prenotazioneEntity.setQUALCOSA e poi save, non so se si può fare perchè andiamo anche ad aggiornare la key
        prenotazioneRepository.delete(prenotazioneEntity);
        addPrenotazione(prenotazioneDTO);
    }

    /**
     * Elimina prenotazione selezionata
     *
     * @param prenotazioneDTO
     */
    public void deletePrenotazione(PrenotazioneDTO prenotazioneDTO) {
        PrenotazioneEntity prenotazioneEntity = new PrenotazioneEntity(prenotazioneDTO);
        prenotazioneRepository.delete(prenotazioneEntity);
    }


    public void findPrenotazione(PrenotazioneDTO prenotazioneDTO) {
        PrenotazioneEntity prenotazioneEntity = new PrenotazioneEntity(prenotazioneDTO);
    }
}

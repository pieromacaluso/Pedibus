package it.polito.ai.mmap.esercitazione2.services;

import it.polito.ai.mmap.esercitazione2.entity.FermataEntity;
import it.polito.ai.mmap.esercitazione2.entity.LineaEntity;
import it.polito.ai.mmap.esercitazione2.entity.PrenotazioneEntity;
import it.polito.ai.mmap.esercitazione2.objectDTO.FermataDTO;
import it.polito.ai.mmap.esercitazione2.objectDTO.LineaDTO;
import it.polito.ai.mmap.esercitazione2.objectDTO.PrenotazioneDTO;
import it.polito.ai.mmap.esercitazione2.repository.FermataRepository;
import it.polito.ai.mmap.esercitazione2.repository.LineaRepository;
import it.polito.ai.mmap.esercitazione2.repository.PrenotazioneRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
     * Salva una linea sul DB
     *
     * @param lineaDTO
     */
    public void addLinea(LineaDTO lineaDTO) {
        LineaEntity lineaEntity = new LineaEntity(lineaDTO);
        lineaRepository.save(lineaEntity);

    }

    /**
     * Metodo che restituisce una LineaDTO a partire dal suo nome
     *
     * @param lineName
     * @return LineaDTO
     */
    public LineaDTO getLineByName(String lineName) {
        return new LineaDTO(lineaRepository.findByNome(lineName), this); //ToDo check unica linea con tale nome, forse farlo in fase di caricamento db

    }

    public LineaDTO getLineById(Integer idLinea) {
        return new LineaDTO(lineaRepository.findById(idLinea).get(), this);
    }


    public List<LineaEntity> getAllLines() {
        return lineaRepository.findAll();
    }

    public List<String> getAllLinesNames() {
        return lineaRepository.findAll().stream().map(f -> f.getNome()).collect(Collectors.toList());
    }


    /**
     * Aggiunge una nuova prenotazione al db. Controlla se esiste già una prenotazione per lo stesso utente nello
     * stesso giorno con lo stesso verso, in tal caso l'inserimento non viene eseguito
     *
     * @param prenotazioneDTO
     */
    public String addPrenotazione(PrenotazioneDTO prenotazioneDTO) {
        PrenotazioneEntity prenotazioneEntity = new PrenotazioneEntity(prenotazioneDTO);
        PrenotazioneEntity checkPren = prenotazioneRepository.findByNomeAlunnoAndDataAndVerso(prenotazioneDTO.getNomeAlunno(), prenotazioneDTO.getData(), prenotazioneDTO.getVerso());
        String id;
        if (checkPren == null)
            id = prenotazioneRepository.save(prenotazioneEntity).getId().toString();
        else
            id = "Prenotazione non valida";
        return id;
        //return prenotazioneRepository.save(prenotazioneEntity).getId().toString();
    }


    /**
     * Metodo utilizzato per aggiornare una vecchia prenotazione tramite il suo reservationId con uno dei
     * nuovi campi passati dall'utente
     *
     * @param prenotazioneDTO contiene i nuovi dati
     */
    public void updatePrenotazione(PrenotazioneDTO prenotazioneDTO, ObjectId reservationId) {
        PrenotazioneEntity prenotazioneEntity = prenotazioneRepository.findById(reservationId);
        prenotazioneEntity.update(prenotazioneDTO);
        prenotazioneRepository.save(prenotazioneEntity);
    }


    public PrenotazioneEntity getPrenotazione(ObjectId reservationId) {
        return prenotazioneRepository.findById(reservationId);
    }

    /**
     * Elimina la prenotazione indicata dall'objectId controllando che i dettagli siano consistenti
     * @param nomeLinea
     * @param data
     * @param reservationId
     */
    public void deletePrenotazione(String nomeLinea, Date data, ObjectId reservationId ) {
        PrenotazioneEntity checkPren = prenotazioneRepository.findById(reservationId);
        if (checkPren.getData().equals(data) && getLineById(checkPren.getIdLinea()).getNome().equals(nomeLinea))
        {
            prenotazioneRepository.delete(checkPren);
        }
        else
        {
            throw new IllegalArgumentException();
        }

    }


    public void findPrenotazione(PrenotazioneDTO prenotazioneDTO) {
        PrenotazioneEntity prenotazioneEntity = new PrenotazioneEntity(prenotazioneDTO);
    }

    public List<String> findAlunniFermata(Date data, Integer id, boolean verso) {
        List<PrenotazioneEntity> prenotazioni = prenotazioneRepository.findAllByDataAndIdFermataAndVerso(data, id, verso);
        return prenotazioni.stream().map(p -> p.getNomeAlunno()).collect(Collectors.toList());

    }

    public boolean LineaUpdated(LineaDTO lineaDTO) {
        LineaEntity linea = new LineaEntity(lineaDTO);
        String ultimaModifica = lineaRepository.findByNome(linea.getNome()).getUltimaModifica();
        if (ultimaModifica != null) // se il campo ultimaModifica non e' presente forse torna null
            return linea.getUltimaModifica().compareToIgnoreCase(ultimaModifica) >= 0;
        else return false;
    }
}

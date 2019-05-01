package it.polito.ai.mmap.esercitazione2.services;

import it.polito.ai.mmap.esercitazione2.entity.FermataEntity;
import it.polito.ai.mmap.esercitazione2.entity.LineaEntity;
import it.polito.ai.mmap.esercitazione2.entity.PrenotazioneEntity;
import it.polito.ai.mmap.esercitazione2.exception.FermataNotFoundException;
import it.polito.ai.mmap.esercitazione2.exception.LineaNotFoundException;
import it.polito.ai.mmap.esercitazione2.exception.PrenotazioneNotFoundException;
import it.polito.ai.mmap.esercitazione2.exception.PrenotazioneNotValidException;
import it.polito.ai.mmap.esercitazione2.objectDTO.FermataDTO;
import it.polito.ai.mmap.esercitazione2.objectDTO.LineaDTO;
import it.polito.ai.mmap.esercitazione2.objectDTO.PrenotazioneDTO;
import it.polito.ai.mmap.esercitazione2.repository.FermataRepository;
import it.polito.ai.mmap.esercitazione2.repository.LineaRepository;
import it.polito.ai.mmap.esercitazione2.repository.PrenotazioneRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MongoService {

    private final LineaRepository lineaRepository;
    private final FermataRepository fermataRepository;
    private final PrenotazioneRepository prenotazioneRepository;

    public MongoService(LineaRepository lineaRepository, FermataRepository fermataRepository, PrenotazioneRepository prenotazioneRepository) {
        this.lineaRepository = lineaRepository;
        this.fermataRepository = fermataRepository;
        this.prenotazioneRepository = prenotazioneRepository;
    }

    /**
     * Salva lista Fermate sul DB
     *
     * @param listaFermate lista fermate
     */
    void addFermate(List<FermataDTO> listaFermate) {

        fermataRepository.saveAll(listaFermate.stream().map(FermataEntity::new).collect(Collectors.toList()));
    }

    /**
     * Eliminazione di tutte le fermate dal DB
     */
    public void dropAllFermate() {
        fermataRepository.deleteAll();
    }

    /**
     * Eliminazione di tutte le linee dal DB
     */
    public void dropAllLinee() {
        lineaRepository.deleteAll();
    }

    /**
     * Eliminazione di tutte le prenotazioni dal DB
     */
    public void dropAllPrenotazioni() {
        prenotazioneRepository.deleteAll();
    }

    /**
     * Fermata da ID fermata
     *
     * @param idFermata ID fermata
     * @return FermataDTO
     */
    private FermataDTO getFermataById(Integer idFermata) {
        Optional<FermataEntity> check = fermataRepository.findById(idFermata);
        if (check.isPresent()) {
            return new FermataDTO(check.get());
        } else {
            throw new FermataNotFoundException("Fermata con ID " + idFermata + "non trovata!");
        }
    }

    /**
     * Salva una linea sul DB
     *
     * @param lineaDTO Linea DTO
     */
    void addLinea(LineaDTO lineaDTO) {
        LineaEntity lineaEntity = new LineaEntity(lineaDTO);
        lineaRepository.save(lineaEntity);

    }

    /**
     * Restituisce una LineaDTO a partire dal suo nome
     *
     * @param lineName nome linea
     * @return LineaDTO
     */
    public LineaDTO getLineByName(String lineName) {
        Optional<LineaEntity> linea = lineaRepository.findByNome(lineName);
        if (linea.isPresent()) {
            return new LineaDTO(linea.get(), fermataRepository);
        } else {
            throw new LineaNotFoundException("Nessuna linea trovata con nome " + lineName);
        }
    }

    /**
     * Restituisce una LineaDTO a partire dal suo ID
     *
     * @param idLinea id Linea
     * @return LineaDTO
     */
    private LineaDTO getLineById(Integer idLinea) {
        Optional<LineaEntity> linea = lineaRepository.findById(idLinea);
        if (linea.isPresent()) {
            return new LineaDTO(linea.get(), fermataRepository);
        } else {
            throw new LineaNotFoundException("Nessuna linea trovata con ID " + idLinea);
        }
    }

    /**
     * Restituisce tutte le linee in DB
     *
     * @return Lista LineaEntity
     */
    public List<LineaEntity> getAllLines() {
        return lineaRepository.findAll();
    }

    /**
     * Restituisce tutti i nomi delle linee presenti in DB
     *
     * @return Lista di nomi linee
     */
    public List<String> getAllLinesNames() {
        return lineaRepository.findAll().stream().map(LineaEntity::getNome).collect(Collectors.toList());
    }


    /**
     * Aggiunge una nuova prenotazione al db. Controlla:
     * - se per quella Linea esiste una fermata con quel id
     * - se esiste già una prenotazione per lo stesso utente nello stesso giorno con lo stesso verso, in tal caso l'inserimento non viene eseguito
     *
     * @param prenotazioneDTO PrenotazioneDTO
     */
    public String addPrenotazione(PrenotazioneDTO prenotazioneDTO) {
        if (isValidPrenotation(prenotazioneDTO) && !isDuplicate(prenotazioneDTO)) {
            PrenotazioneEntity prenotazioneEntity = new PrenotazioneEntity(prenotazioneDTO);
            return prenotazioneRepository.save(prenotazioneEntity).getId().toString();
        } else
            throw new PrenotazioneNotValidException("Prenotazione già presente o non valida");
    }

    /**
     * Metodo utilizzato per aggiornare una vecchia prenotazione tramite il suo reservationId con uno dei
     * nuovi campi passati dall'utente
     *
     * @param prenotazioneDTO contiene i nuovi dati
     */
    public void updatePrenotazione(PrenotazioneDTO prenotazioneDTO, ObjectId reservationId) {
        Optional<PrenotazioneEntity> checkPrenotazione = prenotazioneRepository.findById(reservationId);
        if (isValidPrenotation(prenotazioneDTO) && checkPrenotazione.isPresent()) {
            PrenotazioneEntity prenotazioneEntity = checkPrenotazione.get();
            prenotazioneEntity.update(prenotazioneDTO);
            prenotazioneRepository.save(prenotazioneEntity);
        } else {
            throw new IllegalArgumentException("Aggiornamento prenotazione non valida");
        }
    }

    /**
     * Controlla che i dettagli della prenotazione siano consistenti.
     *
     * @param prenotazioneDTO: Oggetto PrenotazioneDTO
     * @return True se la prenotazione è valida, altrimenti False
     */
    private Boolean isValidPrenotation(PrenotazioneDTO prenotazioneDTO) {
        FermataDTO fermataDTO = this.getFermataById(prenotazioneDTO.getIdFermata());
        return (this.getLineById(prenotazioneDTO.getIdLinea()).getAndata().contains(fermataDTO) && prenotazioneDTO.getVerso()) ||
                (this.getLineById(prenotazioneDTO.getIdLinea()).getRitorno().contains(fermataDTO) && !prenotazioneDTO.getVerso());
    }

    /**
     * Controlla che la prenotazione non esista già
     *
     * @param prenotazioneDTO: Oggetto PrenotazioneDTO
     * @return True se la prenotazione è duplicata, altrimenti False
     */
    private Boolean isDuplicate(PrenotazioneDTO prenotazioneDTO) {
        Optional<PrenotazioneEntity> check = prenotazioneRepository
                .findByNomeAlunnoAndDataAndVerso(
                        prenotazioneDTO.getNomeAlunno(),
                        prenotazioneDTO.getData(),
                        prenotazioneDTO.getVerso());
        return check.isPresent();
    }

    /**
     * PrenotazioneEntity da ReservationID
     *
     * @param reservationId ReservationID
     * @return PrenotazioneEntity
     */
    public PrenotazioneEntity getPrenotazione(ObjectId reservationId) {
        Optional<PrenotazioneEntity> prenotazione = prenotazioneRepository.findById(reservationId);
        if (prenotazione.isPresent()) {
            return prenotazione.get();
        } else {
            throw new PrenotazioneNotFoundException("Prenotazione " + reservationId + "non trovata");
        }

    }

    /**
     * Elimina la prenotazione indicata dall'objectId controllando che i dettagli siano consistenti
     *
     * @param nomeLinea:     Nome della Linea
     * @param data:          Data
     * @param reservationId: Id Prenotazione
     */
    public void deletePrenotazione(String nomeLinea, Date data, ObjectId reservationId) {
        PrenotazioneEntity prenotazione = this.getPrenotazione(reservationId);
        if (prenotazione.getData().equals(data) && getLineById(prenotazione.getIdLinea()).getNome().equals(nomeLinea)) {
            prenotazioneRepository.delete(prenotazione);
        } else {
            throw new IllegalArgumentException("Errore in cancellazione");
        }
    }

    /**
     * Lista Alunni da Data, IdFermata e Verso
     *
     * @param data  Data
     * @param id    IdFermata
     * @param verso verso
     * @return Lista di nomi alunni
     */
    public List<String> findAlunniFermata(Date data, Integer id, boolean verso) {
        List<PrenotazioneEntity> prenotazioni = prenotazioneRepository.findAllByDataAndIdFermataAndVerso(data, id, verso);
        return prenotazioni.stream().map(PrenotazioneEntity::getNomeAlunno).collect(Collectors.toList());

    }
}

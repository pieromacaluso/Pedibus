package it.polito.ai.mmap.esercitazione3.services;

import it.polito.ai.mmap.esercitazione3.entity.PrenotazioneEntity;
import it.polito.ai.mmap.esercitazione3.exception.PrenotazioneNotFoundException;
import it.polito.ai.mmap.esercitazione3.exception.PrenotazioneNotValidException;
import it.polito.ai.mmap.esercitazione3.objectDTO.FermataDTO;
import it.polito.ai.mmap.esercitazione3.objectDTO.PrenotazioneDTO;
import it.polito.ai.mmap.esercitazione3.repository.PrenotazioneRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    //todo sarebbe bello avere tutto ReservationX o PrenotazioneX
    
    @Autowired
    PrenotazioneRepository prenotazioneRepository;
    @Autowired
    LineeService lineeService;
   

    /**
     * Aggiunge una nuova prenotazione al db. Controlla:
     * - se per quella Linea esiste una fermata con quel id
     * - se esiste già una prenotazione per lo stesso utente nello stesso giorno con lo stesso verso, in tal caso l'inserimento non viene eseguito
     *
     * @param prenotazioneDTO PrenotazioneDTO
     */
    public String addPrenotazione(PrenotazioneDTO prenotazioneDTO) {
        if (isValidPrenotation(prenotazioneDTO) && !isDuplicate(prenotazioneDTO)) {
            //TODO
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
        FermataDTO fermataDTO = lineeService.getFermataById(prenotazioneDTO.getIdFermata());
        return (lineeService.getLineByName(prenotazioneDTO.getNomeLinea()).getAndata().contains(fermataDTO) && prenotazioneDTO.getVerso()) ||
                (lineeService.getLineByName(prenotazioneDTO.getNomeLinea()).getRitorno().contains(fermataDTO) && !prenotazioneDTO.getVerso());
    }

    /**
     * Controlla che la prenotazione non esista già
     *
     * @param prenotazioneDTO: Oggetto PrenotazioneDTO
     * @return True se la prenotazione è duplicata, altrimenti False
     */
    private Boolean isDuplicate(PrenotazioneDTO prenotazioneDTO) {
        Optional<PrenotazioneEntity> check = prenotazioneRepository
                .findByIdChildAndDataAndVerso(
                        prenotazioneDTO.getIdChild(),
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
    public PrenotazioneEntity getReservation(ObjectId reservationId) {
        Optional<PrenotazioneEntity> prenotazione = prenotazioneRepository.findById(reservationId);
        if (prenotazione.isPresent()) {
            return prenotazione.get();
        } else {
            throw new PrenotazioneNotFoundException("Prenotazione " + reservationId + " non trovata");
        }

    }

    /**
     * Prenotazione Entity da verso,data,idAlunno
     * @param verso
     * @param data
     * @param idChild
     * @return
     * @throws Exception
     */
    public PrenotazioneEntity getReservation(String verso,String data, ObjectId idChild) throws Exception{
        Optional<PrenotazioneEntity> check;
        Date date=MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);

        if(verso.equals("andata")){
            check=prenotazioneRepository.findByIdChildAndDataAndVerso(idChild,date,true);
        }else if(verso.equals("ritorno")){
            check=prenotazioneRepository.findByIdChildAndDataAndVerso(idChild,date,false);
        }else{
            throw new Exception("errore verso");    //todo verificare
        }

        if(check.isPresent()) {
            return check.get();
        }else{
            throw new PrenotazioneNotFoundException();
        }

    }

    /**
     * PrenotazioneDTO da ReservationId per controller
     * @param reservationId
     * @return
     */
    public PrenotazioneDTO getPrenotazioneDTO(ObjectId reservationId){
        return new PrenotazioneDTO(getReservation(reservationId));
    }

    /**
     * Elimina la prenotazione indicata dall'objectId controllando che i dettagli siano consistenti
     *
     * @param nomeLinea:     Nome della Linea
     * @param data:          Data
     * @param reservationId: Id Prenotazione
     */
    public void deletePrenotazione(String nomeLinea, Date data, ObjectId reservationId) {
        PrenotazioneEntity prenotazione = getReservation(reservationId);
        if (prenotazione.getData().equals(data) && lineeService.getLineByName(prenotazione.getNomeLinea()).getNome().equals(nomeLinea)) {
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
        return prenotazioni.stream().map(PrenotazioneEntity::getIdChild).map(ObjectId::toString).collect(Collectors.toList());
        //todo sostituire il toString dell'objectId con un metodo che ritona il nome dell'alunno
    }

    /**
     * Admin lina indica che ha preso l'alunno alla fermata
     * @param verso
     * @param data
     * @param child
     * @throws Exception
     */
    public void setHandled(String verso,String data, String child) throws Exception {
            ObjectId idChild=new ObjectId(child);
            PrenotazioneEntity prenotazioneEntity = getReservation(verso,data,idChild);
            prenotazioneEntity.setPresoInCarico(true);
            prenotazioneRepository.save(prenotazioneEntity);

    }

    /**
     * Admin lina indica che ha lasciato l'alunno a scuola
     * @param verso
     * @param data
     * @param child
     * @throws Exception
     */
    public void setArrived(String verso,String data, String child) throws Exception {
        ObjectId idChild=new ObjectId(child);
        PrenotazioneEntity prenotazioneEntity = getReservation(verso, data, idChild);
        prenotazioneEntity.setArrivatoScuola(true);
        prenotazioneRepository.save(prenotazioneEntity);
    }
}

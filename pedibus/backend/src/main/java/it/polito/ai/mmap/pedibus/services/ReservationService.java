package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import it.polito.ai.mmap.pedibus.entity.ReservationEntity;
import it.polito.ai.mmap.pedibus.entity.RoleEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.exception.ReservationNotFoundException;
import it.polito.ai.mmap.pedibus.exception.ReservationNotValidException;
import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.objectDTO.FermataDTO;
import it.polito.ai.mmap.pedibus.objectDTO.LineaDTO;
import it.polito.ai.mmap.pedibus.objectDTO.ReservationDTO;
import it.polito.ai.mmap.pedibus.repository.ChildRepository;
import it.polito.ai.mmap.pedibus.repository.ReservationRepository;
import it.polito.ai.mmap.pedibus.repository.RoleRepository;
import it.polito.ai.mmap.pedibus.resources.ReservationChildResource;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    ChildRepository childRepository;
    @Autowired
    LineeService lineeService;

    @Autowired
    UserService userService;


    /**
     * Aggiunge una nuova reservation al db
     *
     * @param reservationDTO ReservationDTO
     */
    public String addReservation(ReservationDTO reservationDTO) {
        if (isValidReservation(reservationDTO) && !isDuplicate(reservationDTO)) {
            ReservationEntity reservationEntity = new ReservationEntity(reservationDTO);
            return reservationRepository.save(reservationEntity).getId().toString();
        } else
            throw new ReservationNotValidException("Reservation già presente o non valida");
    }

    /**
     * Metodo utilizzato per aggiornare una vecchia reservation tramite il suo reservationId con uno dei
     * nuovi campi passati dall'utente
     *
     * @param reservationDTO contiene i nuovi dati
     */
    public void updateReservation(ReservationDTO reservationDTO, ObjectId reservationId) {
        Optional<ReservationEntity> checkReservation = reservationRepository.findById(reservationId);
        if (isValidReservation(reservationDTO) && checkReservation.isPresent()) {
            ReservationEntity reservationEntity = checkReservation.get();
            reservationEntity.update(reservationDTO);
            reservationRepository.save(reservationEntity);
        } else {
            throw new IllegalArgumentException("Aggiornamento reservation non valida");
        }
    }

    /**
     * Controlla che i dettagli della reservation siano consistenti:
     * - si sta cercando di effettuare l'operazione per una reservation futura
     * - La fermata è nel verso indicato
     * - Si sta cercando di prenotare per uno dei proprio children o system-admin o amministratore della linea
     * - se per quella Linea esiste una fermata con quel id
     * - se esiste già una reservation per lo stesso utente nello stesso giorno con lo stesso verso, in tal caso l'inserimento non viene eseguito
     * <p>
     * * @param reservationDTO: Oggetto ReservationDTO
     *
     * @return True se la reservation è valida, altrimenti False
     */
    private Boolean isValidReservation(ReservationDTO reservationDTO) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        FermataDTO fermataDTO = lineeService.getFermataById(reservationDTO.getIdFermata());
        LineaDTO lineaDTO = lineeService.getLineById(reservationDTO.getIdLinea());


        return (checkTime(reservationDTO.getData(), fermataDTO) &&
                ((lineaDTO.getAndata().contains(fermataDTO) && reservationDTO.getVerso()) || (lineaDTO.getRitorno().contains(fermataDTO) && !reservationDTO.getVerso()))) &&
                (principal.getChildrenList().contains(reservationDTO.getCfChild()) || principal.getRoleList().stream().map(RoleEntity::getRole).collect(Collectors.toList()).contains("ROLE_SYSTEM-ADMIN") || lineaDTO.getAdminList().contains(principal.getUsername()));
    }

    /**
     * Controlla che la reservation non esista già
     *
     * @param reservationDTO: Oggetto ReservationDTO
     * @return True se la reservation è duplicata, altrimenti False
     */
    private Boolean isDuplicate(ReservationDTO reservationDTO) {
        Optional<ReservationEntity> check = reservationRepository
                .findByCfChildAndDataAndVerso(
                        reservationDTO.getCfChild(),
                        reservationDTO.getData(),
                        reservationDTO.getVerso());
        return check.isPresent();
    }

    /**
     * Restituisce la reservation controllando che nomeLinea e Data corrispondano a quelli del reservation_id
     *
     * @param nomeLinea
     * @param data
     * @param reservationId
     * @return
     */
    public ReservationDTO getReservationCheck(String nomeLinea, Date data, ObjectId reservationId) {
        Optional<ReservationEntity> checkReservation = reservationRepository.findById(reservationId);
        if (checkReservation.isPresent()) {
            ReservationEntity reservationEntity = checkReservation.get();
            if (lineeService.getLineById(nomeLinea).getId().equals(reservationEntity.getIdLinea()) && data.equals(reservationEntity.getData()))
                return new ReservationDTO(reservationEntity);
            else
                throw new ReservationNotFoundException("Reservation " + reservationId + " non trovata");
        } else {
            throw new ReservationNotFoundException("Reservation " + reservationId + " non trovata");
        }

    }

    /**
     * Reservation Entity da verso,data,idAlunno
     *
     * @param verso
     * @param data
     * @param cfChild
     * @return
     * @throws Exception
     */
    public ReservationEntity getChildReservation(Boolean verso, Date data, String cfChild) throws Exception {
        Optional<ReservationEntity> check = reservationRepository.findByCfChildAndDataAndVerso(cfChild, data, verso);
        if (check.isPresent()) {
            return check.get();
        } else {
            throw new ReservationNotFoundException();
        }
    }

    public List<ChildDTO> getChildrenNotReserved(Date data, boolean verso){
        List<String> bambiniDataVerso=getAllChildrenForReservationDataVerso(data,verso);
        List<String> bambini=userService.getAllChildrenId();

        return userService.getAllChildrenById(bambini.stream().filter(bambino->!bambiniDataVerso.contains(bambino)).collect(Collectors.toList()));
    }

    /**
     * Elimina la reservation indicata dall'objectId controllando:
     * - si sta cercando di effettuare l'operazione per una reservation futura
     * - Dettagli siano consistenti
     * - Si sta cercando di cancellare uno dei proprio children o system-admin o amministratore della linea
     *
     * @param nomeLinea:     Nome della Linea
     * @param data:          Data
     * @param reservationId: Id Reservation
     */
    public void deleteReservation(String nomeLinea, Date data, ObjectId reservationId) {
        Optional<ReservationEntity> checkReservation = reservationRepository.findById(reservationId);
        if (checkReservation.isPresent()) {
            ReservationEntity reservation = checkReservation.get();
            UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            LineaDTO lineaDTO = lineeService.getLineById(reservation.getIdLinea());
            FermataDTO fermataDTO = lineeService.getFermataById(reservation.getIdFermata());
            if (checkTime(reservation.getData(), fermataDTO) && reservation.getData().equals(data) && lineeService.getLineById(reservation.getIdLinea()).getId().equals(nomeLinea) && (principal.getChildrenList().contains(reservation.getCfChild()) || principal.getRoleList().stream().map(RoleEntity::getRole).collect(Collectors.toList()).contains("ROLE_SYSTEM-ADMIN") || lineaDTO.getAdminList().contains(principal.getUsername()))) {
                reservationRepository.delete(reservation);
            } else {
                throw new IllegalArgumentException("Errore in cancellazione reservation");
            }
        } else {
            throw new IllegalArgumentException("Errore in cancellazione reservation");
        }

    }

    private boolean checkTime(Date data, FermataDTO fermataDTO) {
        //se la reservation è per oggi allora controlla che sia prima dell'arrivo alla fermata o che il ruolo sia ADMIN o SYSTEM_ADMIN
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Boolean isAdmin = principal.getRoleList().contains(roleRepository.findByRole("ROLE_ADMIN"));
        if (!principal.getRoleList().contains(roleRepository.findByRole("ROLE_SYSTEM-ADMIN"))) {
            if (data.before(MongoZonedDateTime.getStartOfTomorrow())) {
                //reservation per oggi o nel passato
                if (isAdmin) {
                    return data.after(MongoZonedDateTime.getStartOfToday());
                } else {
                    return data.before(fermataDTO.getDateOrario());
                }
            } else {
                //reservation per domani o nel futuro
                return !isAdmin;
            }
        }
        return true;
    }

    /**
     * Lista Alunni da Data, IdFermata e Verso
     *
     * @param data  Data
     * @param id    IdFermata
     * @param verso verso
     * @return Lista di nomi alunni
     */
    public List<ReservationChildResource> findAlunniFermata(Date data, Integer id, boolean verso) {
        HashMap<String, ReservationEntity> reservations = (HashMap<String, ReservationEntity>) reservationRepository
                .findAllByDataAndIdFermataAndVerso(data, id, verso).stream()
                .collect(
                        Collectors.toMap(ReservationEntity::getCfChild, p -> p));
        Set<String> cfList = reservations.keySet();
        HashMap<String, ChildEntity> children = (HashMap<String, ChildEntity>)
                ((List<ChildEntity>) childRepository.findAllById(cfList)).stream().collect(
                        Collectors.toMap(ChildEntity::getCodiceFiscale, c -> c));
        List<ReservationChildResource> result = new ArrayList<>();
        for (String cf : cfList) {
            ReservationEntity p = reservations.get(cf);
            ChildEntity c = children.get(cf);
            result.add(new ReservationChildResource(p, c));
        }
        return result;
    }

    /**
     * Admin lina indica che ha preso l'alunno alla fermata
     *
     * @param verso
     * @param data
     * @param cfChild
     * @param nomeLinea
     * @throws Exception
     */
    public Integer manageHandled(Boolean verso, Date data, String cfChild, Boolean isSet, String nomeLinea) throws Exception {
        if (canModify(nomeLinea, data)) {
            ReservationEntity reservationEntity = getChildReservation(verso, data, cfChild);
            ReservationDTO pre = new ReservationDTO(reservationEntity);
            pre.setPresoInCarico(isSet);
            updateReservation(pre, reservationEntity.getId());
            return reservationEntity.getIdFermata();
        }
        return -1;
    }

    public boolean canModify(String nomeLinea, Date date) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal.getRoleList().contains(roleRepository.findByRole("ROLE_SYSTEM-ADMIN")))
            return true;
        else {
            if (lineeService.getLineById(nomeLinea).getAdminList().contains(principal.getUsername()) && MongoZonedDateTime.isToday(date)) {
                return true;
            } else return false;

        }
    }

    /**
     * Admin lina indica che ha lasciato l'alunno a scuola/fermata ritorno
     *
     * @param verso
     * @param data
     * @param cfChild
     * @param nomeLinea
     * @throws Exception
     */
    public Boolean manageArrived(Boolean verso, Date data, String cfChild, Boolean isSet, String nomeLinea) throws Exception {
        if (canModify(nomeLinea, data)) {
            ReservationEntity reservationEntity = getChildReservation(verso, data, cfChild);
            reservationEntity.setArrivatoScuola(isSet);
            reservationRepository.save(reservationEntity);
            return true;
        }
        return false;
    }

    /**
     * ritorna tutti i bambini prenotati per una determinata giornata in una detrminata linea
     */
    public List<String> getAllChildrenForReservationDataVerso(Date data, boolean verso) {
        List<ReservationEntity> reservationsTotaliLineaDataVerso = reservationRepository.findByDataAndVerso(data, verso);
        return reservationsTotaliLineaDataVerso.stream().map(ReservationEntity::getCfChild).collect(Collectors.toList());
    }
}

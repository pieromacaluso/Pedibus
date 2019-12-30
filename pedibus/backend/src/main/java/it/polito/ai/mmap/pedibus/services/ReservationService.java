package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.entity.*;
import it.polito.ai.mmap.pedibus.exception.ReservationNotFoundException;
import it.polito.ai.mmap.pedibus.exception.ReservationNotValidException;
import it.polito.ai.mmap.pedibus.objectDTO.*;
import it.polito.ai.mmap.pedibus.repository.*;
import it.polito.ai.mmap.pedibus.resources.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    GestioneCorseService gestioneCorseService;

    @Autowired
    LineeService lineeService;

    @Autowired
    UserService userService;

    @Autowired
    ChildService childService;

    @Autowired
    NotificheService notificheService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;



    @Value("${notifiche.type.Base}")
    String NotBASE;
    @Value("${notifiche.type.Disponibilita}")
    String NotDISPONIBILITA;

    public ReservationEntity getReservationEntityById(ObjectId idReservation) {
        Optional<ReservationEntity> checkReservation = reservationRepository.findById(idReservation);
        if (checkReservation.isPresent()) {
            return checkReservation.get();
        } else {
            throw new ReservationNotFoundException("Prenotazione non esistente");
        }
    }

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
     * Metodo utilizzato per aggiornare una vecchia reservation tramite il suo
     * reservationId con uno dei nuovi campi passati dall'utente
     *
     * @param reservationDTO contiene i nuovi dati
     */
    public void updateReservation(ReservationDTO reservationDTO, ObjectId reservationId) {
        ReservationEntity reservationEntity = getReservationEntityById(reservationId);
        if (isValidReservation(reservationDTO)) {
            reservationEntity.update(reservationDTO);
            reservationRepository.save(reservationEntity);
            reservationDTO.setId(reservationEntity.getId().toString());
        } else {
            throw new IllegalArgumentException("Aggiornamento reservation non valida");
        }
    }

    /**
     * Controlla che i dettagli della reservation siano consistenti: - si sta
     * cercando di effettuare l'operazione per una reservation futura - La fermata è
     * nel verso indicato - Si sta cercando di prenotare per uno dei proprio
     * children o system-admin o amministratore della linea - se per quella Linea
     * esiste una fermata con quel id - se esiste già una reservation per lo stesso
     * utente nello stesso giorno con lo stesso verso, in tal caso l'inserimento non
     * viene eseguito
     * <p>
     * * @param reservationDTO: Oggetto ReservationDTO
     *
     * @return True se la reservation è valida, altrimenti False
     */
    private Boolean isValidReservation(ReservationDTO reservationDTO) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        FermataEntity fermataEntity = lineeService.getFermataEntityById(reservationDTO.getIdFermata());
        LineaEntity lineaEntity = lineeService.getLineaEntityById(reservationDTO.getIdLinea());

        return (checkTime(reservationDTO.getData(), fermataEntity)
                && ((lineaEntity.getAndata().contains(fermataEntity.getId()) && reservationDTO.getVerso()) || (lineaEntity.getRitorno().contains(fermataEntity.getId()) && !reservationDTO.getVerso()))
                && (principal.getChildrenList().contains(reservationDTO.getCfChild()) || lineeService.isAdminLine(reservationDTO.getIdLinea()) || userService.isSysAdmin()));
    }

    /**
     * Controlla che la reservation non esista già
     *
     * @param reservationDTO: Oggetto ReservationDTO
     * @return True se la reservation è duplicata, altrimenti False
     */
    private Boolean isDuplicate(ReservationDTO reservationDTO) {
        Optional<ReservationEntity> check = reservationRepository.findByCfChildAndDataAndVerso(
                reservationDTO.getCfChild(), reservationDTO.getData(), reservationDTO.getVerso());
        return check.isPresent();
    }

    /**
     * Restituisce la reservation controllando che idLinea e Data corrispondano a
     * quelli del reservation_id
     *
     * @param idLinea
     * @param data
     * @param reservationId
     * @return
     */
    public ReservationDTO getReservationCheck(String idLinea, Date data, ObjectId reservationId) {
        ReservationEntity reservationEntity = getReservationEntityById(reservationId);
        if (idLinea.equals(reservationEntity.getIdLinea()) && data.equals(reservationEntity.getData()))
            return new ReservationDTO(reservationEntity);
        else
            throw new ReservationNotFoundException("Reservation " + reservationId + " non trovata");
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
    public ReservationEntity getChildReservation(Boolean verso, Date data, String cfChild) {
        Optional<ReservationEntity> check = reservationRepository.findByCfChildAndDataAndVerso(cfChild, data, verso);
        if (check.isPresent()) {
            return check.get();
        } else {
           throw new ReservationNotFoundException("Reservation non trovata");
        }
    }

    public List<ReservationDTO> getChildReservationsAR(Date data, String cfChild) {
        Optional<List<ReservationEntity>> check = reservationRepository.findByCfChildAndData(cfChild, data);
        List<ReservationDTO> reservationDTOS = new ArrayList<>();
        List<ReservationEntity> entities = check.orElseGet(ArrayList::new);
        entities.forEach((element) -> {
            reservationDTOS.add(new ReservationDTO(element));
        });
        return reservationDTOS;
    }

    /**
     * Restituisce i bambini iscritti tranne quelli che si sono prenotati per quel giorno linea e verso
     *
     * @param data
     * @param verso
     * @return
     */
    public List<ChildDTO> getChildrenNotReserved(Date data, boolean verso) {
        List<String> childrenDataVerso = reservationRepository.findByDataAndVerso(data, verso).stream().map(ReservationEntity::getCfChild).collect(Collectors.toList());
        return childService.getChildrenNotReserved(childrenDataVerso, data, verso);
       /* List<String> childrenAll = childRepository.findAll().stream().map(ChildEntity::getCodiceFiscale).collect(Collectors.toList());
        List<String> childrenNotReserved = childrenAll.stream().filter(bambino -> !childrenDataVerso.contains(bambino)).collect(Collectors.toList());

        return childrenNotReserved.stream().map(codiceFiscale -> childService.getChildDTOById(codiceFiscale)).collect(Collectors.toList());*/
    }

    /**
     * Elimina la reservation indicata dall'objectId controllando: - si sta cercando
     * di effettuare l'operazione per una reservation futura - Dettagli siano
     * consistenti - Si sta cercando di cancellare uno dei proprio children o
     * system-admin o amministratore della linea
     *
     * @param idLinea:       id della Linea
     * @param data:          Data
     * @param reservationId: Id Reservation
     */
    public void deleteReservation(String idLinea, Date data, ObjectId reservationId) {
        ReservationEntity reservationEntity = getReservationEntityById(reservationId);
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        FermataEntity fermataEntity = lineeService.getFermataEntityById(reservationEntity.getIdFermata());
        if (checkTime(reservationEntity.getData(), fermataEntity)
                && reservationEntity.getData().equals(data)
                && idLinea.equals(reservationEntity.getIdLinea())
                && (principal.getChildrenList().contains(reservationEntity.getCfChild())
                || userService.isSysAdmin()
                || lineeService.isAdminLine(idLinea))) {
            reservationRepository.delete(reservationEntity);
        } else {
            throw new IllegalArgumentException("Errore in cancellazione reservation");
        }
    }

    public void deleteReservation(String codiceFiscale, Date dataFormatted, String idLinea, boolean verso) {
        Optional<ReservationEntity> checkReservation = reservationRepository.findByCfChildAndDataAndVerso(codiceFiscale, dataFormatted, verso);
        if (checkReservation.isPresent()) {
            UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            ReservationEntity reservation = checkReservation.get();
            FermataEntity fermataEntity = lineeService.getFermataEntityById(reservation.getIdFermata());
            if (checkTime(reservation.getData(), fermataEntity)
                    && (principal.getChildrenList().contains(reservation.getCfChild())
                    || userService.isSysAdmin()
                    || lineeService.isAdminLine(idLinea))) {
                reservationRepository.delete(reservation);
            }
        } else {
            throw new IllegalArgumentException("Errore in cancellazione reservation");
        }
    }

    private boolean checkTime(Date data, FermataEntity fermataEntity) {
        // se la reservation è per oggi allora controlla che sia prima dell'arrivo alla
        // fermata o che il ruolo sia ADMIN o SYSTEM_ADMIN
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Boolean isAdmin = principal.getRoleList().contains(userService.getRoleEntityById("ROLE_ADMIN"));
        if (!userService.isSysAdmin()) {
            if (data.before(MongoTimeService.getStartOfTomorrow())) {
                // reservation per oggi o nel passato
                if (isAdmin) {
                    return data.after(MongoTimeService.getStartOfToday());
                } else {
                    return data.before(fermataEntity.getDateOrario());
                }
            } else {
                // reservation per domani o nel futuro
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
                .collect(Collectors.toMap(ReservationEntity::getCfChild, p -> p));
        Set<String> cfList = reservations.keySet();
        HashMap<String, ChildEntity> children = childService.getChildrenEntityByCfList(cfList);
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
     * @param datef
     * @param isSet
     * @param idLinea
     * @param cfChild
     * @throws Exception
     */
    public void manageHandled(Boolean verso, String data, Date datef, Boolean isSet, String idLinea, String cfChild) throws Exception {
        if (canModify(idLinea, datef, verso)) {
            UserEntity userEntity = childService.getChildParent(cfChild);

            ReservationEntity reservationEntity = getChildReservation(verso, datef, cfChild);
            ReservationDTO pre = new ReservationDTO(reservationEntity);
            pre.setPresoInCarico(isSet);
            updateReservation(pre, reservationEntity.getId());
            simpMessagingTemplate.convertAndSend("/handled/" + data + "/" + idLinea + "/" + ((verso) ? 1 : 0), new HandledResource(cfChild, isSet, reservationEntity.getIdFermata()));

            if(isSet){
                NotificaEntity notificaEntity = new NotificaEntity(NotificaEntity.NotificationType.BASE, userEntity.getUsername(), "Suo figlio è sul pedibus in direzione scuola.", null);
                notificheService.addNotifica(notificaEntity);      //salvataggio notifica
            }else{
                //todo vedere se cancellarla ma in modo corretto, bisogna trovare il modo di identificare la notifica da cancellare
                NotificaEntity notificaEntity = new NotificaEntity(NotificaEntity.NotificationType.BASE, userEntity.getUsername(), "No scherzavo XD.", null);
                notificheService.addNotifica(notificaEntity);      //salvataggio notifica
            }

            logger.info("/handled/" + data + "/" + idLinea + "/" + verso);
        } else
            throw new IllegalStateException();
    }

    public boolean canModify(String idLinea, Date date, Boolean verso) {
        if (userService.isSysAdmin())
            return true;

        //todo (marcof) io terrei solo il ruolo di guida per gestire la manage e la handle, quello di admin invece concerne la gestione delle disp, turni ecc
        return ((lineeService.isAdminLine(idLinea) || gestioneCorseService.isGuideConfirmed(idLinea,date,verso)) && MongoTimeService.isToday(date));

    }

    /**
     * Admin lina indica che ha lasciato l'alunno a scuola/fermata ritorno
     *
     * @param verso
     * @param data
     * @param cfChild
     * @param idLinea
     * @throws Exception
     */

    public Boolean manageArrived(Boolean verso, Date data, String cfChild, Boolean isSet, String idLinea) throws Exception {
        if (canModify(idLinea, data, verso)) {
            ReservationEntity reservationEntity = getChildReservation(verso, data, cfChild);
            reservationEntity.setArrivatoScuola(isSet);
            //todo da eliminare prima
            reservationRepository.save(reservationEntity);

            UserEntity userEntity = childService.getChildParent(cfChild);
            NotificaEntity notificaEntity = new NotificaEntity(NotificaEntity.NotificationType.BASE, userEntity.getUsername(), "Suo figlio è arrivato a scuola.", null);
            notificheService.addNotifica(notificaEntity);
            //todo messaggio topic per atri admin
            return true;
        }
        return false;
    }

    /**
     * Costruisce l'oggetto richiesto da GET
     * /reservations/verso/{id_linea}/{data}/{verso}
     *
     * @param idLinea
     * @param dataFormatted
     * @param verso
     * @return
     */
    public GetReservationsIdDataVersoResource getReservationsVersoResource(String idLinea, Date dataFormatted,
                                                                           boolean verso) {
        GetReservationsIdDataVersoResource res = new GetReservationsIdDataVersoResource();
        // Ordinati temporalmente, quindi seguendo l'andamento del percorso
        List<FermataDTO> fermate;
        if (verso) {
            fermate = lineeService.getLineaDTOById(idLinea).getAndata();
            res.setOrarioScuola(lineeService.getArrivoScuola());
        } else {
            fermate = lineeService.getLineaDTOById(idLinea).getRitorno();
            res.setOrarioScuola(lineeService.getPartenzaScuola());

        }
        res.setAlunniPerFermata(fermate.stream().map(FermataAlunniResource::new).collect(Collectors.toList()));
        res.getAlunniPerFermata()
                .forEach((f) -> f.setAlunni(findAlunniFermata(dataFormatted, f.getFermata().getId(), verso)));

        res.setChildrenNotReserved(getChildrenNotReserved(dataFormatted, verso));
        res.setCanModify(canModify(idLinea, dataFormatted,verso));

        return res;
    }

    public GetReservationsIdLineaDataResource getReservationsResource(String idLinea, Date dataFormatted) {
        GetReservationsIdLineaDataResource res = new GetReservationsIdLineaDataResource();
        // Ordinati temporalmente, quindi seguendo l'andamento del percorso
        LineaDTO lineaDTO = lineeService.getLineaDTOById(idLinea);
        res.setAlunniPerFermataAndata(
                lineaDTO.getAndata().stream().map(FermataAlunniResource::new).collect(Collectors.toList()));
        // true per indicare l'andata
        res.getAlunniPerFermataAndata()
                .forEach((f) -> f.setAlunni(findAlunniFermata(dataFormatted, f.getFermata().getId(), true)));

        res.setAlunniPerFermataRitorno(
                lineaDTO.getRitorno().stream().map(FermataAlunniResource::new).collect(Collectors.toList()));
        // false per indicare il ritorno
        res.getAlunniPerFermataRitorno()
                .forEach((f) -> f.setAlunni(findAlunniFermata(dataFormatted, f.getFermata().getId(), false)));

        //todo provvisorio, per richiedere sia andata che ritorno dovresti essere stato confermato per entrambi o essere admin, ho bypassato con un or (marcof)
        res.setCanModify(canModify(idLinea, dataFormatted, true) || canModify(idLinea, dataFormatted, false));
        res.setArrivoScuola(lineeService.getArrivoScuola());
        res.setPartenzaScuola(lineeService.getPartenzaScuola());

        return res;
    }


}

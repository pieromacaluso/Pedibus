package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.configuration.PedibusString;
import it.polito.ai.mmap.pedibus.entity.*;
import it.polito.ai.mmap.pedibus.exception.ChildNotFoundException;
import it.polito.ai.mmap.pedibus.exception.ReservationNotFoundException;
import it.polito.ai.mmap.pedibus.exception.ReservationNotValidException;
import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.objectDTO.FermataDTO;
import it.polito.ai.mmap.pedibus.objectDTO.ReservationDTO;
import it.polito.ai.mmap.pedibus.repository.ReservationRepository;
import it.polito.ai.mmap.pedibus.resources.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationService {
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
    MongoTimeService mongoTimeService;
    @Value("${notifiche.type.Base}")
    String NotBASE;
    @Value("${notifiche.type.Disponibilita}")
    String NotDISPONIBILITA;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

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
            throw new ReservationNotValidException(PedibusString.RESERVATION_DUPLICATE);
    }

    /**
     * Metodo utilizzato per aggiornare una vecchia reservation tramite il suo
     * reservationId con uno dei nuovi campi passati dall'utente
     *
     * @param reservationDTO contiene i nuovi dati
     */
    public ReservationEntity updateReservation(ReservationDTO reservationDTO, ObjectId reservationId) {
        ReservationEntity reservationEntity = getReservationEntityById(reservationId);
        ReservationDTO reservationDTOOld = new ReservationDTO(reservationEntity);

        if (isValidReservation(reservationDTO)) {
            reservationEntity.update(reservationDTO);
            ReservationEntity res = reservationRepository.save(reservationEntity);
            reservationDTO.setId(reservationEntity.getId().toString());

            this.notificheService.sendReservationNotification(reservationDTOOld, true);
            this.notificheService.sendReservationNotification(reservationDTO, false);
            return res;
        } else {
            throw new ReservationNotValidException(PedibusString.UPDATE_RESERVATION_NOT_VALID);
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
    public Boolean isValidReservation(ReservationDTO reservationDTO) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        FermataEntity fermataEntity = lineeService.getFermataEntityById(reservationDTO.getIdFermata());
        LineaEntity lineaEntity = lineeService.getLineaEntityById(reservationDTO.getIdLinea());

        return (checkTime(reservationDTO.getData(), fermataEntity)
                && ((lineaEntity.getAndata().contains(fermataEntity.getId()) && reservationDTO.getVerso()) || (lineaEntity.getRitorno().contains(fermataEntity.getId()) && !reservationDTO.getVerso()))
                && (principal.getChildrenList().contains(reservationDTO.getCfChild()) || this.canModify(reservationDTO.getIdLinea(), reservationDTO.getData(), reservationDTO.getVerso())));
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
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal.getChildrenList().contains(cfChild)) {
            List<ReservationEntity> reservationEntityList = reservationRepository.findByCfChildAndData(cfChild, data);
            return reservationEntityList.stream().map(ReservationDTO::new).collect(Collectors.toList());
        } else
            throw new ChildNotFoundException(cfChild);
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
                || userService.isSysAdmin())) {
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
                    // CONSEGNA: Filtro prenotazione odierna per permettere manipolazione
                    return data.after(MongoTimeService.getStartOfToday());
//                    return data.before(fermataEntity.getDateOrario());
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

            ReservationEntity reservationEntity = getChildReservation(verso, datef, cfChild);
            reservationEntity.setPresoInCarico(isSet);
            reservationEntity.setPresoInCaricoDate(isSet ? MongoTimeService.getNow() : null);
            ReservationDTO pre = new ReservationDTO(reservationEntity);
            if (isSet) {
                List<NotificaEntity> notificaEntities = this.notificheService.generateHandledNotification(reservationEntity);
                for (NotificaEntity notificaEntity : notificaEntities) {
                    notificheService.addNotifica(notificaEntity);      //salvataggio notifica
                }
                pre.setPresoInCaricoNotifica(notificaEntities.stream().map(NotificaEntity::getIdNotifica).collect(Collectors.toList()));
            } else {
                notificheService.deleteNotificaAdmin(reservationEntity.getPresoInCaricoNotifica());
            }
            updateReservation(pre, reservationEntity.getId());

            logger.info("/handled/" + data + "/" + idLinea + "/" + verso);
        } else
            throw new IllegalStateException();
    }


    public void manageAssente(Boolean verso, String data, Date datef, Boolean isSet, String idLinea, String cfChild) {
        if (canModify(idLinea, datef, verso)) {

            ReservationEntity reservationEntity = getChildReservation(verso, datef, cfChild);
            reservationEntity.setAssente(isSet);
            reservationEntity.setAssenteDate(isSet ? MongoTimeService.getNow() : null);

            ReservationDTO pre = new ReservationDTO(reservationEntity);

            if (isSet) {
                List<NotificaEntity> notificaEntities = notificheService.generateAssenteNotification(reservationEntity);
                for (NotificaEntity notificaEntity : notificaEntities) {
                    notificheService.addNotifica(notificaEntity);      //salvataggio notifica
                }
                pre.setAssenteNotifica(notificaEntities.stream().map(NotificaEntity::getIdNotifica).collect(Collectors.toList()));
            } else {
                notificheService.deleteNotificaAdmin(reservationEntity.getAssenteNotifica());
            }
            updateReservation(pre, reservationEntity.getId());

            logger.info("/handled/" + data + "/" + idLinea + "/" + verso);
        } else
            throw new IllegalStateException();
    }

    public boolean canModify(String idLinea, Date date, Boolean verso) {
        if (userService.isSysAdmin())
            return true;
        return ((lineeService.isAdminLine(idLinea) || gestioneCorseService.isGuideConfirmed(idLinea, date, verso)) && MongoTimeService.isToday(date));
    }

    /**
     * Admin lina indica che ha lasciato l'alunno a scuola/fermata ritorno
     *
     * @param verso   verso
     * @param data    data
     * @param cfChild id del bambino
     * @param idLinea id della linea
     * @throws Exception
     */

    public Boolean manageArrived(Boolean verso, Date data, String cfChild, Boolean isSet, String idLinea) throws Exception {
        if (canModify(idLinea, data, verso)) {
            ReservationEntity reservationEntity = getChildReservation(verso, data, cfChild);
            reservationEntity.setArrivatoScuola(isSet);
            reservationEntity.setArrivatoScuolaDate(isSet ? MongoTimeService.getNow() : null);
            if (isSet) {
                List<NotificaEntity> notificaEntities = notificheService.generateArrivedNotification(reservationEntity);
                for (NotificaEntity notificaEntity : notificaEntities) {
                    notificheService.addNotifica(notificaEntity);      //salvataggio notifica
                }
                reservationEntity.setArrivatoScuolaNotifica(notificaEntities.stream().map(NotificaEntity::getIdNotifica).collect(Collectors.toList()));
            } else {
                notificheService.deleteNotificaAdmin(reservationEntity.getArrivatoScuolaNotifica());
            }
            logger.info("/arrived/" + data + "/" + idLinea + "/" + verso);
            ReservationDTO pre = new ReservationDTO(reservationEntity);
            updateReservation(pre, reservationEntity.getId());
            return true;
        }
        return false;
    }

    /**
     * Resetta la situazione della prenotazione del bambino indicato
     *
     * @param verso   verso
     * @param data    data
     * @param cfChild codice fiscale bambino
     * @param idLinea id linea
     */
    public void manageRestore(Boolean verso, Date data, String cfChild, String idLinea) {
        if (canModify(idLinea, data, verso)) {

            ReservationEntity reservationEntity = getChildReservation(verso, data, cfChild);
            if (reservationEntity.getPresoInCaricoNotifica() != null) {
                notificheService.deleteNotificaAdmin(reservationEntity.getPresoInCaricoNotifica());
                reservationEntity.setPresoInCaricoNotifica(null);
            }
            if (reservationEntity.getArrivatoScuolaNotifica() != null) {
                notificheService.deleteNotificaAdmin(reservationEntity.getArrivatoScuolaNotifica());
                reservationEntity.setArrivatoScuolaNotifica(null);
            }
            if (reservationEntity.getAssenteNotifica() != null) {
                notificheService.deleteNotificaAdmin(reservationEntity.getAssenteNotifica());
                reservationEntity.setAssenteNotifica(null);
            }
            reservationEntity.setArrivatoScuola(false);
            reservationEntity.setArrivatoScuolaDate(null);
            reservationEntity.setPresoInCarico(false);
            reservationEntity.setPresoInCaricoDate(null);
            reservationEntity.setAssente(false);
            reservationEntity.setAssenteDate(null);
            ReservationDTO pre = new ReservationDTO(reservationEntity);
            updateReservation(pre, reservationEntity.getId());
        }
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
        res.setCanModify(canModify(idLinea, dataFormatted, verso));

        return res;
    }

    /**
     * Questa funzione si preoccupa di andare a inserire tutte le prenotazioni di default di un determinato utente dalla
     * data di iscrizione, fino alla data di fine scuola. Vengono saltati i giorni di chiusura della scuola.
     *
     * @param childEntity entity del bambino
     */
    public void bulkReservation(ChildEntity childEntity) {
        LocalDate dateTime = LocalDate.now();
        List<String> listDate = mongoTimeService.generateListOfDateStartingFrom(dateTime);
        List<ReservationDTO> listRes = new LinkedList<>();
        for (String data : listDate) {
            ReservationResource reservationResourceA = ReservationResource.builder()
                    .cfChild(childEntity.getCodiceFiscale()).idFermata(childEntity.getIdFermataAndata()).verso(true).build();
            ReservationResource reservationResourceR = ReservationResource.builder()
                    .cfChild(childEntity.getCodiceFiscale()).idFermata(childEntity.getIdFermataRitorno()).verso(false).build();

            String idLineaA = this.lineeService.getFermataEntityById(childEntity.getIdFermataAndata()).getIdLinea();
            String idLineaR = this.lineeService.getFermataEntityById(childEntity.getIdFermataRitorno()).getIdLinea();

            Date dataFormatted = mongoTimeService.getMongoZonedDateTimeFromDate(data, true);

            ReservationDTO reservationADTO = new ReservationDTO(reservationResourceA, idLineaA, dataFormatted);
            ReservationDTO reservationRDTO = new ReservationDTO(reservationResourceR, idLineaR, dataFormatted);
            listRes.add(reservationADTO);
            listRes.add(reservationRDTO);
        }
        reservationRepository.saveAll(listRes.stream().map(ReservationEntity::new).collect(Collectors.toList()));
    }

    /**
     * Cancella tutte le prenotazioni di un bambino
     *
     * @param childId codice fiscale del bambino
     */
    public void deleteAllChildReservation(String childId) {
        List<ReservationEntity> reservationEntities = this.reservationRepository.findAllByCfChild(childId).orElseGet(ArrayList::new);

        for (ReservationEntity res : reservationEntities) {
            this.notificheService.sendReservationNotification(new ReservationDTO(res), true);
        }
        this.reservationRepository.deleteAllByCfChild(childId);
    }

    /**
     * @param idLinea
     * @param date
     * @param verso
     * @return il dump delle prenotazioni per la terna idLina, data, verso
     */
    public ReservationsDump getReservationsDump(String idLinea, Date date, boolean verso) {
        return new ReservationsDump(date, idLinea, verso,
                reservationRepository.findAllByIdLineaAndDataAndVerso(idLinea, date, verso),
                getChildrenNotReserved(date, verso).stream().map(ChildDTO::getCodiceFiscale).collect(Collectors.toList()));
    }
}

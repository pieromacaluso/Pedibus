package it.polito.ai.mmap.pedibus.services;


import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import it.polito.ai.mmap.pedibus.entity.ReservationEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.exception.ChildNotFoundException;
import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.objectDTO.ReservationDTO;
import it.polito.ai.mmap.pedibus.repository.ChildRepository;
import it.polito.ai.mmap.pedibus.resources.ChildDefaultStopResource;
import it.polito.ai.mmap.pedibus.resources.UserInsertResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChildService {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserService userService;
    @Autowired
    MongoTimeService mongoTimeService;
    @Autowired
    ChildRepository childRepository;
    @Autowired
    LineeService lineeService;
    @Autowired
    ReservationService reservationService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;


    public ChildDTO getChildDTOById(String codiceFiscale) {
        Optional<ChildEntity> checkChild = childRepository.findById(codiceFiscale);
        if (checkChild.isPresent())
            return new ChildDTO(checkChild.get());
        else
            throw new ChildNotFoundException("Alunno non trovato");
    }

    public List<ChildDTO> getAllChildren() {
        return childRepository.findAll().stream().map(ChildDTO::new).collect(Collectors.toList());
    }

    /**
     * Recuperiamo da db i figli dell'utente loggato
     *
     * @return
     */
    public List<ChildDTO> getMyChildren() {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ((List<ChildEntity>) childRepository.findAllById(principal.getChildrenList())).stream().map(ChildDTO::new).collect(Collectors.toList());
    }

    /**
     * Metodo che permette di cambiare la fermata di default di un bambino o dal suo genitore o da un System-Admin
     *
     * @param cfChild
     * @param stopRes
     * @param date
     */
    public void updateChildStop(String cfChild, ChildDefaultStopResource stopRes, Date date) {
        Optional<ChildEntity> c = childRepository.findById(cfChild);
        if (c.isPresent()) {
            UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal.getChildrenList().contains(cfChild) || principal.getRoleList().contains(userService.getRoleEntityById("ROLE_SYSTEM-ADMIN"))) {
                ChildEntity childEntity = c.get();

                lineeService.getFermataEntityById(stopRes.getIdFermataAndata());
                childEntity.setIdFermataAndata(stopRes.getIdFermataAndata());
                lineeService.getFermataEntityById(stopRes.getIdFermataRitorno());
                childEntity.setIdFermataRitorno(stopRes.getIdFermataRitorno());

                childRepository.save(childEntity);
                // Remove one day
                LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).minusDays(1);
                Date out = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                this.simpMessagingTemplate.convertAndSendToUser(userService.getUserEntity(childEntity.getIdParent()).getUsername(), "/child/" + childEntity.getCodiceFiscale(), childEntity);
                Optional<List<ReservationEntity>> toBeUpdatedCheck = reservationService.reservationRepository.findByCfChildAndDataIsAfter(cfChild, out);
                List<ReservationEntity> toBeUpdated = toBeUpdatedCheck.orElseGet(ArrayList::new);
                for (ReservationEntity res : toBeUpdated) {
                    ReservationDTO reservationDTO = new ReservationDTO(res);

                    ReservationDTO oldreservationDTO = new ReservationDTO(res);
                    oldreservationDTO.setIdFermata(null);

                    ReservationEntity oldRes = new ReservationEntity();
                    oldRes.setCfChild(reservationDTO.getCfChild());
                    oldRes.setIdFermata(null);

                    Integer fermata = reservationDTO.getVerso() ? stopRes.getIdFermataAndata() : stopRes.getIdFermataRitorno();
                    String linea = lineeService.getFermataEntityById(reservationDTO.getVerso() ? stopRes.getIdFermataAndata() : stopRes.getIdFermataRitorno()).getIdLinea();

                    reservationDTO.setIdFermata(fermata);
                    reservationDTO.setIdLinea(linea);
                    res = reservationService.updateReservation(reservationDTO, res.getId());
                    UserEntity parent = this.getChildParent(reservationDTO.getCfChild());

                    //Vecchia prenotazione (Eliminazione)
                    simpMessagingTemplate.convertAndSend("/reservation/" + MongoTimeService.dateToString(oldreservationDTO.getData()) + "/" + oldreservationDTO.getIdLinea() + "/" + ((oldreservationDTO.getVerso()) ? 1 : 0), oldRes);
                    simpMessagingTemplate.convertAndSendToUser(parent.getUsername(), "/child/res/" + oldreservationDTO.getCfChild() + "/" + MongoTimeService.dateToString(oldreservationDTO.getData()), oldreservationDTO);

                    //Nuova prenotazione (Aggiunta)
                    simpMessagingTemplate.convertAndSend("/reservation/" + MongoTimeService.dateToString(reservationDTO.getData()) + "/" + reservationDTO.getIdLinea() + "/" + ((reservationDTO.getVerso()) ? 1 : 0), res);
                    simpMessagingTemplate.convertAndSendToUser(parent.getUsername(), "/child/res/" + reservationDTO.getCfChild() + "/" + MongoTimeService.dateToString(reservationDTO.getData()), reservationDTO);
                }
            } else
                throw new ChildNotFoundException("Bambino non trovato tra i tuoi figli");
        } else
            throw new ChildNotFoundException("Bambino non trovato");
    }

    /**
     * Metodo da usare in altri service in modo da non dover fare sempre i controlli
     *
     * @param cfChild
     * @return
     */
    public ChildEntity getChildrenEntity(String cfChild) {
        Optional<ChildEntity> checkChild = childRepository.findById(cfChild);
        if (checkChild.isPresent()) {
            return checkChild.get();
        } else {
            throw new ChildNotFoundException("Codice Fiscale non valido.");
        }
    }

    /**
     * Restituisce i bambini iscritti tranne quelli che si sono prenotati per quel giorno linea e verso
     *
     * @param data
     * @param verso
     * @return
     */
    public List<ChildDTO> getChildrenNotReserved(List<String> childrenDataVerso, Date data, boolean verso) {
        List<String> childrenAll = childRepository.findAll().stream().map(ChildEntity::getCodiceFiscale).collect(Collectors.toList());
        List<String> childrenNotReserved = childrenAll.stream().filter(bambino -> !childrenDataVerso.contains(bambino)).collect(Collectors.toList());

        return childrenNotReserved.stream().map(this::getChildDTOById).collect(Collectors.toList());
    }

    public HashMap<String, ChildEntity> getChildrenEntityByCfList(Set<String> cfList) {
        return (HashMap<String, ChildEntity>) ((List<ChildEntity>) childRepository
                .findAllById(cfList)).stream().collect(Collectors.toMap(ChildEntity::getCodiceFiscale, c -> c));
    }


    public UserEntity getChildParent(String cfChild) {
        ChildEntity childEntity = getChildrenEntity(cfChild);
        return userService.getUserEntity(childEntity.getIdParent());
    }

    public Page<ChildDTO> getAllPagedChildren(Pageable pageable, String keyword) {
        Page<ChildEntity> pagedUsersEntity = childRepository.findAllByNameContainingOrSurnameContainingOrCodiceFiscaleContainingOrderBySurnameAscNameAscCodiceFiscaleAsc(keyword, keyword, keyword, pageable);
        return PageableExecutionUtils.getPage(pagedUsersEntity.stream().map(ChildDTO::new).collect(Collectors.toList()), pageable, pagedUsersEntity::getTotalElements);

    }
}

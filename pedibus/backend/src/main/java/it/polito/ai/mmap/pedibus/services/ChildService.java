package it.polito.ai.mmap.pedibus.services;


import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import it.polito.ai.mmap.pedibus.entity.ReservationEntity;
import it.polito.ai.mmap.pedibus.entity.RoleEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.exception.ChildNotFoundException;
import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.objectDTO.ReservationDTO;
import it.polito.ai.mmap.pedibus.repository.ChildRepository;
import it.polito.ai.mmap.pedibus.resources.ChildDefaultStopResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sun.misc.Regexp;

import java.time.LocalDate;
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
                List<UserEntity> parents = this.getChildParents(cfChild);
                for (UserEntity parent : parents) {
                    this.simpMessagingTemplate.convertAndSendToUser(parent.getUsername(), "/child/" + childEntity.getCodiceFiscale(), childEntity);
                }
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
                    //Vecchia prenotazione (Eliminazione)
                    simpMessagingTemplate.convertAndSend("/reservation/" + MongoTimeService.dateToString(oldreservationDTO.getData()) + "/" + oldreservationDTO.getIdLinea() + "/" + ((oldreservationDTO.getVerso()) ? 1 : 0), oldRes);
                    //Nuova prenotazione (Aggiunta)
                    simpMessagingTemplate.convertAndSend("/reservation/" + MongoTimeService.dateToString(reservationDTO.getData()) + "/" + reservationDTO.getIdLinea() + "/" + ((reservationDTO.getVerso()) ? 1 : 0), res);
                    for (UserEntity parent : parents) {
                        //Vecchia prenotazione (Eliminazione)
                        simpMessagingTemplate.convertAndSendToUser(parent.getUsername(), "/child/res/" + oldreservationDTO.getCfChild() + "/" + MongoTimeService.dateToString(oldreservationDTO.getData()), oldreservationDTO);
                        //Nuova prenotazione (Aggiunta)
                        simpMessagingTemplate.convertAndSendToUser(parent.getUsername(), "/child/res/" + reservationDTO.getCfChild() + "/" + MongoTimeService.dateToString(reservationDTO.getData()), reservationDTO);
                    }
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


    public List<UserEntity> getChildParents(String cfChild) {
        Optional<List<UserEntity>> userEntities = this.userService.userRepository.findAllByChildrenListIsContaining(cfChild);

        return userEntities.orElseGet(ArrayList::new);
    }

    public Page<ChildDTO> getAllPagedChildren(Pageable pageable, String keyword) {
        Page<ChildEntity> pagedUsersEntity = childRepository.searchByNameSurnameCF(ChildService.fromKeywordToRegex(keyword), pageable);
        return PageableExecutionUtils.getPage(pagedUsersEntity.stream().map(ChildDTO::new).collect(Collectors.toList()), pageable, pagedUsersEntity::getTotalElements);
    }

    public static String fromKeywordToRegex(String keyword) {
        List<String> keywords = Arrays.asList(keyword.split("\\s+"));
        StringBuilder regex = new StringBuilder("");
        for (int i = 0; i < keywords.size(); i++) {
            regex.append(".*").append(keywords.get(i)).append(".*");
            if (i != keywords.size() -1) regex.append("|");
        }
        return regex.toString();
    }

    public ChildEntity createChild(ChildDTO childDTO) {
        if (childRepository.findById(childDTO.getCodiceFiscale()).isPresent()) {
            throw new IllegalArgumentException("Duplicate child with ID:" + childDTO.getCodiceFiscale());
        }
        ChildEntity child = new ChildEntity(childDTO);
        ChildEntity childEntity = childRepository.save(child);
        this.reservationService.bulkReservation(childEntity);
        this.sendUpdateNotification();
        return childEntity;
    }

    public ChildEntity updateChild(String childId, ChildDTO childDTO) {
        if (!childId.equals(childDTO.getCodiceFiscale()) && childRepository.findById(childDTO.getCodiceFiscale()).isPresent()) {
            // TODO: Eccezione Custom?
            throw new IllegalArgumentException("Duplicate child with ID:" + childDTO.getCodiceFiscale());
        }
        Optional<ChildEntity> childEntityOptional = childRepository.findById(childId);
        if (!childEntityOptional.isPresent()) {
            // TODO: Eccezione Custom?
            throw new IllegalArgumentException("There is no child with ID:" + childId);
        }
        if (!childId.equals(childDTO.getCodiceFiscale())) {
            Optional<List<UserEntity>> parents = this.userService.userRepository.findAllByChildrenListIsContaining(childId);
            if (parents.isPresent()) {
                List<UserEntity> parentsOfChild = parents.get();
                this.deleteChildFromParent(childId, parentsOfChild);
                this.addChildToParent(childDTO, parentsOfChild);
                this.userService.userRepository.saveAll(parentsOfChild);
            }
        }
        ChildEntity childEntityOld = childEntityOptional.get();
        ChildEntity childEntityNew = new ChildEntity(childDTO);
        childRepository.delete(childEntityOld);
        ChildEntity result = childRepository.save(childEntityNew);
        LocalDate date = LocalDate.now();
        ChildDefaultStopResource defaultStopResource = new ChildDefaultStopResource(result.getIdFermataAndata(), result.getIdFermataRitorno(), date.toString());
        this.updateChildStop(result.getCodiceFiscale(), defaultStopResource, mongoTimeService.getMongoZonedDateTimeFromDate(date.toString(), false));
        this.sendUpdateNotification();
        return result;
    }

    private void addChildToParent(ChildDTO childDTO, List<UserEntity> userEntities) {
        for (UserEntity u : userEntities) {
            u.getChildrenList().add(childDTO.getCodiceFiscale());
        }
    }

    private void deleteChildFromParent(String childId, List<UserEntity> parentsOfChild) {
        for (UserEntity u : parentsOfChild) {
            u.getChildrenList().remove(childId);
        }

    }

    private void sendUpdateNotification() {
        Optional<RoleEntity> roleEntity = this.userService.roleRepository.findById("ROLE_SYSTEM-ADMIN");
        if (roleEntity.isPresent()) {
            Optional<List<UserEntity>> userEntities = this.userService.userRepository.findAllByRoleListContaining(roleEntity.get());
            if (userEntities.isPresent()) {
                for (UserEntity admin : userEntities.get())
                    this.simpMessagingTemplate.convertAndSendToUser(admin.getUsername(), "/anagrafica", "updates");
            }
        }
    }

    public void deleteChild(String childId) {
        Optional<ChildEntity> childEntityOptional = this.childRepository.findById(childId);
        if (!childEntityOptional.isPresent()) {
            // TODO: Eccezione Custom?
            throw new IllegalArgumentException("There is no child with ID:" + childId);
        }
        this.childRepository.delete(childEntityOptional.get());
        Optional<List<UserEntity>> parents = this.userService.userRepository.findAllByChildrenListIsContaining(childId);
        if (parents.isPresent()) {
            List<UserEntity> parentsOfChild = parents.get();
            this.deleteChildFromParent(childId, parentsOfChild);
            this.userService.userRepository.saveAll(parentsOfChild);
        }
        this.sendUpdateNotification();
    }
}

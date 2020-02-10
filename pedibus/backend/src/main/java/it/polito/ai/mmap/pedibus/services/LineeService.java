package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.entity.FermataEntity;
import it.polito.ai.mmap.pedibus.entity.LineaEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.exception.FermataNotFoundException;
import it.polito.ai.mmap.pedibus.exception.LineaNotFoundException;
import it.polito.ai.mmap.pedibus.objectDTO.FermataDTO;
import it.polito.ai.mmap.pedibus.objectDTO.LineaDTO;
import it.polito.ai.mmap.pedibus.repository.FermataRepository;
import it.polito.ai.mmap.pedibus.repository.LineaRepository;
import it.polito.ai.mmap.pedibus.repository.ReservationRepository;
import it.polito.ai.mmap.pedibus.resources.UserInsertResource;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Data
public class LineeService {

    @Autowired
    LineaRepository lineaRepository;
    @Autowired
    FermataRepository fermataRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    UserService userService;

    @Value("${arrivoScuola}")
    String arrivoScuola;
    @Value("${partenzaScuola}")
    String partenzaScuola;


    /**
     * Restituisce una LineaEntity a partire dal suo nome
     *
     * @param idLinea id linea
     * @return LineaEntity
     */
    public LineaEntity getLineaEntityById(String idLinea) {
        Optional<LineaEntity> checkLinea = lineaRepository.findById(idLinea);
        if (checkLinea.isPresent()) {
            return checkLinea.get();
        } else {
            throw new LineaNotFoundException(idLinea);
        }
    }

    /**
     * Restituisce una lista di linea Entity di cui l'utente è amministratore
     *
     * @param username nome utente
     * @return Lista di linea entiny
     */
    public List<LineaEntity> getAdminLineForUser(String username) {
        return lineaRepository.findAllByAdminListContaining(username);
    }

    /**
     * Restituisce una LineaDTO a partire dal suo nome
     * Se possibile usare l'entity che è più leggera
     *
     * @param idLinea id linea
     * @return LineaEntity
     */
    public LineaDTO getLineaDTOById(String idLinea) {
        return new LineaDTO(getLineaEntityById(idLinea), fermataRepository);
    }

    /**
     * FermataEntity da ID fermata
     * Ha un costo minore del DTO
     *
     * @param idFermata ID fermata
     * @return FermataEntity
     */
    public FermataEntity getFermataEntityById(Integer idFermata) {
        Optional<FermataEntity> check = fermataRepository.findById(idFermata);
        if (check.isPresent()) {
            return check.get();
        } else {
            throw new FermataNotFoundException("Fermata con ID " + idFermata + " non trovata!");
        }
    }

    /**
     * FermataDTO da ID fermata
     * Ha un costo maggiore dell'entity
     *
     * @param idFermata ID fermata
     * @return FermataDTO
     */
    public FermataDTO getFermataDTOById(Integer idFermata) {
        FermataEntity fermataEntity = getFermataEntityById(idFermata);
        Optional<LineaEntity> lineaEntity = this.lineaRepository.findById(fermataEntity.getIdLinea());
        if (lineaEntity.isPresent()) {
            FermataDTO fermataDTO = new FermataDTO(getFermataEntityById(idFermata));
            fermataDTO.setNomeLinea(lineaEntity.get().getNome());
            return fermataDTO;
        } else {
            throw new LineaNotFoundException(fermataEntity.getIdLinea());
        }
    }


    /**
     * Restituisce tutti i nomi delle linee presenti in DB
     *
     * @return Lista di linee
     */
    public List<String> getAllLinesIds() {
        return lineaRepository.findAll().stream().map(LineaEntity::getId).collect(Collectors.toList());
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
     * Aggiunge alla lista di admin di una linea l'user indicato
     *
     * @param email   email dell'user da aggiungere
     * @param idLinea id della linea a cui aggiungerlo.
     */
    public void addAdminLine(String email, String idLinea) {
        LineaEntity lineaEntity = getLineaEntityById(idLinea);
        Set<String> adminList = lineaEntity.getAdminList();
        if (adminList == null)
            adminList = new TreeSet<>(Collections.singletonList(email));
        else if (!adminList.contains(email))
            adminList.add(email);
        lineaEntity.setAdminList(adminList);
        lineaRepository.save(lineaEntity);
    }

    /**
     * Rimozione dell'utente specificato da tutte le linee di cui è amministratore. Solitamente viene utilizzata appena
     * prima di una reinizializzazione dei permessi dell'utente sulle linee.
     *
     * @param userId indirizzo email dell'utente
     */
    public void removeAdminFromAllLine(String userId) {
        List<LineaEntity> linesMaster = this.getAllLinesMasterMail(userId);
        List<LineaEntity> entityList = lineaRepository.findAll();
        entityList.forEach(lineaEntity -> {
            if (!linesMaster.contains(lineaEntity)) lineaEntity.getAdminList().remove(userId);
        });
        lineaRepository.saveAll(entityList);
    }

    /**
     * Rimuove dalla lista di admin di una linea l'user indicato
     *
     * @param email   email dell'user da eliminare
     * @param idLinea id della linea da cui eliminarlo
     */
    public void delAdminLine(String email, String idLinea) {
        LineaEntity lineaEntity = getLineaEntityById(idLinea);
        Set<String> adminList = lineaEntity.getAdminList();
        if (adminList == null)
            adminList = new TreeSet<>(Arrays.asList(email));
        else if (adminList.contains(email))
            adminList.remove(email);

        lineaEntity.setAdminList(adminList);
        lineaRepository.save(lineaEntity);
    }


    // TODO: non utilizzata, eliminare?
    public FermataDTO getFermataPartenzaOrArrivo(String idLinea, Boolean verso) {
        LineaDTO lineaDTO = getLineaDTOById(idLinea);
        return verso ? lineaDTO.getAndata().stream().min(FermataDTO::compareTo).get() : lineaDTO.getRitorno().stream().max(FermataDTO::compareTo).get();
    }


    /**
     * Controlla se l'amministratore è tale per la linea specificata
     *
     * @param idLinea linea da controllare
     * @return true se è amministratore, false altrimenti
     */
    public Boolean isAdminLine(String idLinea) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return getLineaEntityById(idLinea).getAdminList().contains(principal.getUsername()) && this.userService.isAdmin();
    }

    /**
     * Funzione che permette di verificare se un utente è amministratore master per una determinata linea
     * <p>
     * ì     * @param idLinea identificatore della linea
     *
     * @return true se è master, falso altrimenti
     */
    public boolean isMasterLine(String idLinea) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity user = this.userService.getUserEntity(principal.getId());
        LineaEntity lineaEntity = this.getLineaEntityById(idLinea);
        return lineaEntity.getMaster().equals(user.getUsername());
    }

    /**
     * Funzione che permette di verificare se un utente è amministratore master per una determinata linea
     *
     * @param userID  identificatore dell'utente
     * @param idLinea identificatore della linea
     * @return true se è master, falso altrimenti
     */
    public boolean isMasterLine(String userID, String idLinea) {
        UserInsertResource user = this.userService.getUserByEmail(userID);
        LineaEntity lineaEntity = this.getLineaEntityById(idLinea);
        return lineaEntity.getMaster().equals(user.getUserId());
    }

    /**
     * Restituisce tutte le linee di cui il principal è admin
     *
     * @return Lista di linee di cui è amministratore
     */
    public List<LineaDTO> getAllLinesAdminPrincipal() {
        List<LineaDTO> lines = this.getAllLinesIds().stream().map(e -> new LineaDTO(this.getLineaEntityById(e), fermataRepository)).collect(Collectors.toList());
        List<LineaDTO> adminLines = new ArrayList<>();
        if (this.userService.isSysAdmin()) return lines;
        for (LineaDTO line : lines)
            if (isMasterLine(line.getId()) || isAdminLine(line.getId())) adminLines.add(line);
        return adminLines;
    }

    /**
     * Restituisce tutte le linee di cui il principal è admin
     *
     * @return Lista di linee di cui è amministratore
     */
    public List<String> getAllLinesPrincipal() {
        List<String> lines = this.getAllLinesIds();
        List<String> adminLines = new ArrayList<>();
        if (this.userService.isSysAdmin()) return lines;
        for (String line : lines)
            if (isMasterLine(line) || isAdminLine(line)) adminLines.add(line);
        return adminLines;
    }

    public List<LineaEntity> getAllLinesMasterMail(String email) {
        return this.lineaRepository.findAllByMasterIs(email);
    }
}

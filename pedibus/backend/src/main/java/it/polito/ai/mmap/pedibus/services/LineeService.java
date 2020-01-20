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
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
            throw new LineaNotFoundException("Nessuna linea trovata con nome " + idLinea);
        }
    }

    /**
     * Restituisce una lista di linea Entity di cui l'utente è amministratore
     *
     * @param username nome utente
     * @return Lista di linea entiny
     */
    public List<LineaEntity> getAdminLineForUser(String username) {
        Optional<List<LineaEntity>> checkLinea = lineaRepository.findAllByAdminListContaining(username);
        return checkLinea.orElseGet(ArrayList::new);
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
            throw new LineaNotFoundException("Linea " + fermataEntity.getIdLinea() + "non trovata!");
        }
    }


    /**
     * Restituisce tutti i nomi delle linee presenti in DB
     *
     * @return Lista di nomi linee
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
     * @param userID
     * @param idLinea
     */
    public void addAdminLine(String userID, String idLinea) {
        LineaEntity lineaEntity = getLineaEntityById(idLinea);
        ArrayList<String> adminList = lineaEntity.getAdminList();
        if (adminList == null)
            adminList = new ArrayList<>(Arrays.asList(userID));
        else if (!adminList.contains(userID))
            adminList.add(userID);

        lineaEntity.setAdminList(adminList);
        lineaRepository.save(lineaEntity);
    }

    public void removeAdminFromAllLine(String userId) {
        List<LineaEntity> entityList = lineaRepository.findAll();
        entityList.forEach(lineaEntity -> lineaEntity.getAdminList().remove(userId));
        lineaRepository.saveAll(entityList);
    }

    public void delAdminLine(String userID, String idLinea) {
        LineaEntity lineaEntity = getLineaEntityById(idLinea);
        ArrayList<String> adminList = lineaEntity.getAdminList();
        if (adminList == null)
            adminList = new ArrayList<>(Arrays.asList(userID));
        else if (adminList.contains(userID))
            adminList.remove(userID);

        lineaEntity.setAdminList(adminList);
        lineaRepository.save(lineaEntity);
    }


    public FermataDTO getFermataPartenzaOrArrivo(String idLinea, Boolean verso) {
        LineaDTO lineaDTO = getLineaDTOById(idLinea);
        return verso ? lineaDTO.getAndata().stream().min(FermataDTO::compareTo).get() : lineaDTO.getRitorno().stream().max(FermataDTO::compareTo).get();
    }


    public Boolean isAdminLine(String idLinea) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return getLineaEntityById(idLinea).getAdminList().contains(principal.getUsername()) && this.userService.isAdmin();
    }
}

package it.polito.ai.mmap.pedibus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.pedibus.entity.*;
import it.polito.ai.mmap.pedibus.repository.*;
import it.polito.ai.mmap.pedibus.resources.PermissionResource;
import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import it.polito.ai.mmap.pedibus.services.LineeService;
import it.polito.ai.mmap.pedibus.services.MongoTimeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.util.*;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
// Controllare di non andare a specificare una data che è in vacanza che causa una IllegalArgumentException


/*
 * - Usando i dati di test qualsiasi aggiunta o modifica viene cancellata dopo ogni test
 * - Non supporre che tra 4, n giorni le uniche prenotazioni siano quelle introdotte come test
 * - usare solo gli endpoint http che si stanno testando (a meno che lo si faccia per comodità), per il resto fare accesso diretto al db
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class Esercitazione3ApplicationTests {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${superadmin.email}")
    private String superAdminMail;
    @Value("${superadmin.password}")
    private String superAdminPass;

    @Autowired
    LineeService lineeService;
    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    LineaRepository lineaRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RecoverTokenRepository recoverTokenRepository;
    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    private String mailTest = "appmmap@pieromacaluso.com";


    @Autowired
    ChildRepository childRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    FermataRepository fermataRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    MongoTimeService mongoTimeService;


    Map<Integer, ChildEntity> childMap = new HashMap<>();
    Map<String, UserDTO> userDTOMap = new HashMap<>();
    Map<String, UserEntity> userEntityMap = new HashMap<>();
    RoleEntity roleUser;
    RoleEntity roleAdmin;
    LineaEntity defLinea;

    @PostConstruct
    public void postInit() {
        defLinea = lineaRepository.findAll().get(0);
        roleUser = roleRepository.findById("ROLE_USER").get();
        roleAdmin = roleRepository.findById("ROLE_ADMIN").get();
        childMap.put(0, new ChildEntity("RSSMRA30A01H501I", "Mario", "Rossi"));
        childMap.put(1, new ChildEntity("SNDPTN80C15H501C", "Sandro", "Pertini"));
        childMap.put(2, new ChildEntity("CLLCRL80A01H501D", "Carlo", "Collodi"));

        userDTOMap.put("testGenitore", new UserDTO("test.Genitore@test.it", "321@%$User", "321@%$User"));
        userDTOMap.put("testNonGenitore", new UserDTO("test.NonGenitore@test.it", "321@%$User", "321@%$User"));
        userDTOMap.put("testNonno", new UserDTO("test.Nonno@test.it", "321@%$User", "321@%$User"));

        userEntityMap.put("testGenitore", new UserEntity(userDTOMap.get("testGenitore"), new HashSet<>(Arrays.asList(roleUser)), passwordEncoder, new HashSet<>(Arrays.asList(childMap.get(0).getCodiceFiscale()))));
        userEntityMap.put("testNonGenitore", new UserEntity(userDTOMap.get("testNonGenitore"), new HashSet<>(Arrays.asList(roleUser)), passwordEncoder));
        userEntityMap.put("testNonno", new UserEntity(userDTOMap.get("testNonno"), new HashSet<>(Arrays.asList(roleAdmin)), passwordEncoder));

    }

    @Before
    public void setUpMethod() {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .apply(documentationConfiguration(this.restDocumentation))
                .alwaysDo(document("{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
                .build();

        childRepository.saveAll(childMap.values());
        userEntityMap.values().forEach(userEntity -> {
            userEntity.setEnabled(true);
            if (userEntity.getRoleList().contains(roleAdmin)) {
                logger.info("Il nonno" + userEntity.getUsername() + " sarà admin della linea: " + defLinea.getId());
                lineeService.addAdminLine(userEntity.getUsername(), defLinea.getId());
            }
            userRepository.save(userEntity);
        });
    }

    @After
    public void tearDownMethod() {
        Optional<UserEntity> check = userRepository.findByUsername(mailTest);
        if (check.isPresent()) {
            userRepository.delete(check.get());
            recoverTokenRepository.deleteByUserId(check.get().getId());
        }

        //cancello l'utente di test dalla lista degli admin di tutte le linee
        List<LineaEntity> lineaEntityList = lineaRepository.findAll();
        lineaEntityList.forEach(lineaEntity -> lineaEntity.getAdminList().remove(mailTest));
        lineaRepository.saveAll(lineaEntityList);


        childMap.values().forEach(childEntity ->
        {
            reservationRepository.deleteAllByCfChild(childEntity.getCodiceFiscale());
            childRepository.delete(childEntity);
        });
        userEntityMap.values().forEach(userEntity -> {
            if (userEntity.getRoleList().contains(roleAdmin))
                lineeService.delAdminLine(userEntity.getUsername(), defLinea.getId());
            userRepository.delete(userEntity);
        });
    }

    /**
     * Controlla l'endpoint POST /login con dati corretti
     *
     * @throws Exception
     */
    @Test
    public void postLogin_correct() throws Exception {
        logger.info("Test POST /login ...");

        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);
        String json = objectMapper.writeValueAsString(user);
        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk());

        logger.info("PASSED");
    }

    /**
     * Controlla l'endpoint POST /login con username e pw sbagliata
     *
     * @throws Exception
     */
    @Test
    public void postLogin_incorrect() throws Exception {
        logger.info("Test POST /login ...");

        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword("aimaimaim");

        String json1 = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isUnauthorized());

        user.setEmail("applicazioni.internet.mmapgmail.com");
        user.setPassword("aimaimaim");
        String json2 = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json2))
                .andExpect(status().isUnauthorized());

        user.setEmail(superAdminMail);
        user.setPassword("1");
        String json3 = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json3))
                .andExpect(status().isUnauthorized());

        logger.info("PASSED");
    }

    /**
     * Controlla che POST /recover risponda sempre 200 come da specifiche
     *
     * @throws Exception
     */
    @Test
    public void postRecover_always200() throws Exception {
        logger.info("Test POST /recover");

        String token = loginAsGenitore();
        UserDTO userDTO = userDTOMap.get("testGenitore");
        mockMvc.perform(post("/recover").contentType(MediaType.APPLICATION_JSON).content(userDTO.getEmail()))
                .andExpect(status().isOk());

        UserEntity userEntity = userRepository.findByUsername(userDTO.getEmail()).get();
        Optional<RecoverTokenEntity> recoverCheck = recoverTokenRepository.findByUserId(userEntity.getId());
        assert recoverCheck.isPresent();
        String UUID = recoverCheck.get().getId().toString();
        UserDTO user1 = new UserDTO();
        user1.setPassword("123456@User");
        user1.setPassMatch("123456@User");
        String json2 = objectMapper.writeValueAsString(user1);
        mockMvc.perform(post("/recover/" + UUID).contentType(MediaType.APPLICATION_JSON).content(json2))
                .andExpect(status().isOk());

        userDTO.setPassword("123456@User");
        String json3 = objectMapper.writeValueAsString(userDTO);

        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json3))
                .andExpect(status().isOk());

        String email = "random";
        mockMvc.perform(post("/recover").contentType(MediaType.APPLICATION_JSON).content(email))
                .andExpect(status().isOk());

        logger.info("PASSED");
    }


    /**
     * Controlla PUT /admin/users/{userID}
     *
     * @throws Exception
     */
    @Test
    public void putUsers() throws Exception {

        logger.info("Test PUT /admin/users");
        // SYS-ADMIN

        UserDTO user = new UserDTO();
        String token = loginAsSystemAdmin();



        LineaEntity lineaEntity = lineaRepository.findAll().get(0);
        PermissionResource perm = new PermissionResource();
        perm.setIdLinea(lineaEntity.getId());
        perm.setAddOrDel(true);
        String json = objectMapper.writeValueAsString(perm);

        mockMvc.perform(put("/admin/users/" + userDTOMap.get("testGenitore").getEmail())
                .contentType(MediaType.APPLICATION_JSON).content(json)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(put("/admin/users/" + userDTOMap.get("testGenitore").getEmail())
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isUnauthorized());

        assert lineaRepository.findById(lineaEntity.getId()).get().getAdminList().contains(userDTOMap.get("testGenitore").getEmail());

        perm.setAddOrDel(false);
        json = objectMapper.writeValueAsString(perm);
        mockMvc.perform(put("/admin/users/" + userDTOMap.get("testGenitore").getEmail())
                .contentType(MediaType.APPLICATION_JSON).content(json)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(put("/admin/users/" + userDTOMap.get("testGenitore").getEmail())
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isUnauthorized());

        assert !lineaRepository.findById(lineaEntity.getId()).get().getAdminList().contains(userDTOMap.get("testGenitore").getEmail());

        logger.info("PASSED");

    }


    private String loginAsGenitore() throws Exception {
        String json = objectMapper.writeValueAsString(userDTOMap.get("testGenitore"));
        MvcResult result = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String loginAsSystemAdmin() throws Exception {

        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);
        String json = objectMapper.writeValueAsString(user);
        MvcResult result = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }
}

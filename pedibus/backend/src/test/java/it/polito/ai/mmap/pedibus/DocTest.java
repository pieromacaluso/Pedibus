package it.polito.ai.mmap.pedibus;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.pedibus.entity.LineaEntity;
import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import it.polito.ai.mmap.pedibus.repository.LineaRepository;
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
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DocTest {
    private Logger logger = LoggerFactory.getLogger(Esercitazione2ApplicationTests.class);

    @Value("${superadmin.email}")
    private String superAdminMail;
    @Value("${superadmin.password}")
    private String superAdminPass;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    LineaRepository lineaRepository;

    @Before
    public void setUp() {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation))
                .alwaysDo(document("{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
                .build();
    }

    @Test
    public void lines() throws Exception {
        String token = loginAsSystemAdmin();

        logger.info(token);
        List<String> expectedResult = lineaRepository.findAll().stream().map(LineaEntity::getId).collect(Collectors.toList());
        String expectedJson = objectMapper.writeValueAsString(expectedResult);

        mockMvc.perform(get("/lines")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson))
                .andDo(document("lines",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));

//        this.mockMvc.perform(get("/lines")
//                .header("Authorization", "Bearer " + token))
//                .andExpect(status().isOk())
//                .andExpect(content().json(expectedJson))
//                .andDo(document("crud-delete-example", pathParameters(parameterWithName("id").description("The id of the input to delete"))));

    }

//    @Test
//    public void crudGetExample() throws Exception {
//
//        Map<String, Object> crud = new HashMap<>();
//        crud.put("id", 1L);
//        crud.put("title", "Sample Model");
//        crud.put("body", "http://www.baeldung.com/");
//
//        String tagLocation = this.mockMvc.perform(RestDocumentationRequestBuilders.get("/crud").contentType(MediaTypes.HAL_JSON)
//                .content(this.objectMapper.writeValueAsString(crud)))
//                .andExpect(status().isOk())
//                .andReturn()
//                .getResponse()
//                .getHeader("Location");
//
//        crud.put("tags", singletonList(tagLocation));
//
////        ConstraintDescriptions desc = new ConstraintDescriptions(CrudInput.class);
////
////        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/crud").contentType(MediaTypes.HAL_JSON)
////                .content(this.objectMapper.writeValueAsString(crud)))
////                .andExpect(status().isOk())
////                .andDo(document("crud-get-example", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()), requestFields(fieldWithPath("id").description("The id of the input" + collectionToDelimitedString(desc.descriptionsForProperty("id"), ". ")),
////                        fieldWithPath("title").description("The title of the input"), fieldWithPath("body").description("The body of the input"), fieldWithPath("tags").description("An array of tag resource URIs"))));
//    }
//
//    @Test
//    public void crudCreateExample() throws Exception {
//        Map<String, Object> crud = new HashMap<>();
//        crud.put("id", 2L);
//        crud.put("title", "Sample Model");
//        crud.put("body", "http://www.baeldung.com/");
//
//        String tagLocation = this.mockMvc.perform(post("/crud").contentType(MediaTypes.HAL_JSON)
//                .content(this.objectMapper.writeValueAsString(crud)))
//                .andExpect(status().isCreated())
//                .andReturn()
//                .getResponse()
//                .getHeader("Location");
//
//        crud.put("tags", singletonList(tagLocation));
//
//        this.mockMvc.perform(post("/crud").contentType(MediaTypes.HAL_JSON)
//                .content(this.objectMapper.writeValueAsString(crud)))
//                .andExpect(status().isCreated())
//                .andDo(document("crud-create-example", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()), requestFields(fieldWithPath("id").description("The id of the input"), fieldWithPath("title").description("The title of the input"),
//                        fieldWithPath("body").description("The body of the input"), fieldWithPath("tags").description("An array of tag resource URIs"))));
//    }
//
//    @Test
//    public void crudDeleteExample() throws Exception {
//        this.mockMvc.perform(delete("/crud/{id}", 10))
//                .andExpect(status().isOk())
//                .andDo(document("crud-delete-example", pathParameters(parameterWithName("id").description("The id of the input to delete"))));
//    }
//
//    @Test
//    public void crudPatchExample() throws Exception {
//
//        Map<String, String> tag = new HashMap<>();
//        tag.put("name", "PATCH");
//
//        String tagLocation = this.mockMvc.perform(patch("/crud/{id}", 10).contentType(MediaTypes.HAL_JSON)
//                .content(this.objectMapper.writeValueAsString(tag)))
//                .andExpect(status().isOk())
//                .andReturn()
//                .getResponse()
//                .getHeader("Location");
//
//        Map<String, Object> crud = new HashMap<>();
//        crud.put("title", "Sample Model Patch");
//        crud.put("body", "http://www.baeldung.com/");
//        crud.put("tags", singletonList(tagLocation));
//
//        this.mockMvc.perform(patch("/crud/{id}", 10).contentType(MediaTypes.HAL_JSON)
//                .content(this.objectMapper.writeValueAsString(crud)))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    public void crudPutExample() throws Exception {
//        Map<String, String> tag = new HashMap<>();
//        tag.put("name", "PUT");
//
//        String tagLocation = this.mockMvc.perform(put("/crud/{id}", 10).contentType(MediaTypes.HAL_JSON)
//                .content(this.objectMapper.writeValueAsString(tag)))
//                .andExpect(status().isAccepted())
//                .andReturn()
//                .getResponse()
//                .getHeader("Location");
//
//        Map<String, Object> crud = new HashMap<>();
//        crud.put("title", "Sample Model");
//        crud.put("body", "http://www.baeldung.com/");
//        crud.put("tags", singletonList(tagLocation));
//
//        this.mockMvc.perform(put("/crud/{id}", 10).contentType(MediaTypes.HAL_JSON)
//                .content(this.objectMapper.writeValueAsString(crud)))
//                .andExpect(status().isAccepted());
//    }
//

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

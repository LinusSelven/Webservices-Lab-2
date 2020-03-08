package com.lab2webservices.lab2webservices;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;


@WebMvcTest(PhoneController.class)
@Import({PhoneDataModelAssembler.class})
class PhoneControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    PhoneRepository repository;

    @BeforeEach
    void setUpNewTest() {
        when(repository.findAll()).thenReturn(List.of(new Phone(1L, "Iphone X", 1),
                new Phone(2L, "Samsung Galaxy S10", 2)));
        when(repository.findById(1L)).thenReturn(Optional.of(new Phone(1L, "Iphone X", 1)));
        when(repository.findByPhoneName("Iphone X")).thenReturn(Optional.of(new Phone(1L, "Iphone X",1)));
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.save(any(Phone.class))).thenAnswer(invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            var p = (Phone) args[0];
            return new Phone(1L, p.getPhoneName(), p.getBrandId());
        });
    }

    @Test
    void getAllReturnsListOfAllPhones() throws Exception {
        mockMvc.perform(
                get("/api/v1/phones").contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.phoneList[0]._links.self.href", is("http://localhost/api/v1/phones/1")))
                .andExpect(jsonPath("_embedded.phoneList[0].phoneName", is("Iphone X")));
    }

    @Test
    @DisplayName("Calls Get method with url /api/v1/phones/1")
    void getOnePhoneWithValidIdOne() throws Exception {
        mockMvc.perform(
                get("/api/v1/phones/1").accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self.href", is("http://localhost/api/v1/phones/1")));
    }

    @Test
    @DisplayName("Calls Get method with invalid id url /api/v1/phones/3")
    void getOnePhoneWithInValidIdThree() throws Exception {
        mockMvc.perform(
                get("/api/phones/0").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void addNewPhoneWithPostReturnsCreatedPhone() throws Exception {
        mockMvc.perform(
                post("/api/v1/phones/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":0,\"name\":\"Iphone\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Delete user with ID in url")
    void deleteUserInRepository() throws Exception {
        mockMvc.perform(delete("/api/v1/phones/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Trying to delete user with invalid ID")
    void deleteUserWithInvalidID() throws Exception {
        mockMvc.perform(delete("/api/v1/phones/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Put with complete data")
    void putUserWithCompleteDataWithId1() throws Exception {
        mockMvc.perform(put("/api/v1/phones/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"phoneName\":\"Iphone X\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self.href", is("http://localhost/api/v1/phones/1")))
                .andExpect(jsonPath("phoneName", is("Iphone X")));
    }

    @Test
    @DisplayName("Put with incomplete data, should return null on missing content")
    void putUserWithIncompleteData() throws Exception {
        mockMvc.perform(put("/api/v1/phones/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"phoneName\":\"Iphone X\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self.href", is("http://localhost/api/v1/phones/1")))
                .andExpect(jsonPath("phoneName", is("Iphone X")))
                .andExpect(jsonPath("realName").doesNotExist());
    }

    @Test
    @DisplayName("Patch user with new complete data")
    void patchUserWithAllData() throws Exception {
        mockMvc.perform(patch("/api/v1/phones/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"phoneName\":\"Iphone 4s\",\"brandId\":\"1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self.href", is("http://localhost/api/v1/phones/1")))
                .andExpect(jsonPath("phoneName", is("Iphone 4s")));
    }

    @Test
    @DisplayName("Patch with only username and expect other values to remain unchanged")
    void patchUserWithNewUsername() throws Exception {
        mockMvc.perform(patch("/api/v1/phones/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"phoneName\":\"Iphone 6s\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self.href", is("http://localhost/api/v1/phones/1")))
                .andExpect(jsonPath("phoneName", is("Iphone 6s")))
                .andExpect(jsonPath("brandId", is(1)));
    }
}
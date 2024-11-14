package com.spring.qbe.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
class EmployeeControllerTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:latest"));

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee sampleEmployee;
    private List<Employee> employeeList;

    @BeforeEach
    void setUp() {
        sampleEmployee = Employee.builder()
                                 .id(1L)
                                 .firstName("John")
                                 .lastName("Doe")
                                 .department("IT")
                                 .position("Software Engineer")
                                 .salary(new BigDecimal("85000.00"))
                                 .build();
        employeeList = Arrays.asList(
            sampleEmployee,
            Employee.builder()
                    .id(2L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .department("HR")
                    .position("HR Manager")
                    .salary(new BigDecimal("75000.00"))
                    .build()
        );
    }

    @Test
    void searchEmployees_WithValidParameters_ReturnsEmployeeList() throws Exception {
        // given
        when(employeeService.findEmployeesWithCustomMatcher("John", "IT"))
            .thenReturn(Collections.singletonList(sampleEmployee));

        // when
        ResultActions result = mockMvc.perform(get("/api/employees/search")
            .param("firstName", "John")
            .param("department", "IT")
            .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$", hasSize(1)))
              .andExpect(jsonPath("$[0].firstName").value("John"))
              .andExpect(jsonPath("$[0].lastName").value("Doe"))
              .andExpect(jsonPath("$[0].department").value("IT"))
              .andExpect(jsonPath("$[0].position").value("Software Engineer"))
              .andExpect(jsonPath("$[0].salary").value(85000.00));
    }

    @Test
    void searchEmployees_WithNoParameters_ReturnsAllEmployees() throws Exception {
        // given
        when(employeeService.findEmployeesWithCustomMatcher(null, null))
            .thenReturn(employeeList);

        // when
        ResultActions result = mockMvc.perform(get("/api/employees/search")
            .contentType(MediaType.APPLICATION_JSON));


        // then
        result.andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$", hasSize(2)))
              .andExpect(jsonPath("$[0].firstName").value("John"))
              .andExpect(jsonPath("$[1].firstName").value("Jane"));
    }

    @Test
    void findByExample_WithValidEmployee_ReturnsMatchingEmployees() throws Exception {
        // given
        when(employeeService.findEmployeesByExample(any(Employee.class)))
            .thenReturn(Collections.singletonList(sampleEmployee));
        Employee searchExample = Employee.builder()
                                         .department("IT")
                                         .position("Software Engineer")
                                         .build();

        // when
        ResultActions result = mockMvc.perform(post("/api/employees/search/example")
            .content(objectMapper.writeValueAsString(searchExample))
            .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$", hasSize(1)))
              .andExpect(jsonPath("$[0].firstName").value("John"))
              .andExpect(jsonPath("$[0].department").value("IT"));
    }

    @Test
    void findOneByExample_WithExistingEmployee_ReturnsEmployee() throws Exception {
        // given
        when(employeeService.findOneEmployeeByExample(any(Employee.class)))
            .thenReturn(Optional.of(sampleEmployee));

        Employee searchExample = Employee.builder()
                                         .firstName("John")
                                         .department("IT")
                                         .build();

        // when
        ResultActions result = mockMvc.perform(post("/api/employees/search/example/one")
            .content(objectMapper.writeValueAsString(searchExample))
            .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.firstName").value("John"))
              .andExpect(jsonPath("$.lastName").value("Doe"))
              .andExpect(jsonPath("$.department").value("IT"))
              .andExpect(jsonPath("$.position").value("Software Engineer"))
              .andExpect(jsonPath("$.salary").value(85000.00));
    }

    @Test
    void findOneByExample_WithNonExistingEmployee_ThrowsException() throws Exception {
        // given
        when(employeeService.findOneEmployeeByExample(any(Employee.class)))
            .thenReturn(Optional.empty());
        Employee searchExample = Employee.builder()
                                         .firstName("NonExistent")
                                         .department("NonExistent")
                                         .build();

        // when
        ResultActions result = mockMvc.perform(post("/api/employees/search/example/one")
            .content(objectMapper.writeValueAsString(searchExample))
            .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isNotFound());
    }

    @Test
    void countByExample_WithValidEmployee_ReturnsCount() throws Exception {
        // given
        when(employeeService.countEmployeesByExample(any(Employee.class)))
            .thenReturn(5L);
        Employee searchExample = Employee.builder()
                                         .department("IT")
                                         .build();

        // when
        ResultActions result = mockMvc.perform(post("/api/employees/count")
            .content(objectMapper.writeValueAsString(searchExample))
            .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
              .andExpect(content().string("5"));
    }

    @Test
    void existsByExample_WithExistingEmployee_ReturnsTrue() throws Exception {
        // given
        when(employeeService.existsByExample(any(Employee.class)))
            .thenReturn(true);
        Employee searchExample = Employee.builder()
                                         .department("IT")
                                         .position("Software Engineer")
                                         .build();

        // when
        ResultActions result = mockMvc.perform(post("/api/employees/exists")
            .content(objectMapper.writeValueAsString(searchExample))
            .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
              .andExpect(content().string("true"));
    }

    @Test
    void existsByExample_WithNonExistingEmployee_ReturnsFalse() throws Exception {
        // given
        when(employeeService.existsByExample(any(Employee.class)))
            .thenReturn(false);
        Employee searchExample = Employee.builder()
                                         .department("NonExistent")
                                         .build();

        // when
        ResultActions result = mockMvc.perform(post("/api/employees/exists")
            .content(objectMapper.writeValueAsString(searchExample))
            .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
              .andExpect(content().string("false"));
    }

}
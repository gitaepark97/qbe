package com.spring.qbe.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    private EmployeeService employeeService;

    @Captor
    private ArgumentCaptor<Example<Employee>> exampleCaptor;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeRepository);
    }

    @DisplayName("Should find employees by exact criteria")
    @Test
    void shouldFindEmployeesByExample() {
        // given
        Employee probe = Employee.builder()
                                 .department("IT")
                                 .position("Developer")
                                 .build();
        List<Employee> expectedEmployees = List.of(
            Employee.builder()
                    .firstName("Jane")
                    .lastName("Doe")
                    .department("IT")
                    .position("Developer")
                    .build(),
            Employee.builder()
                    .firstName("Mike")
                    .lastName("Johnson")
                    .department("IT")
                    .position("Developer")
                    .build()
        );

        when(employeeRepository.findAll(any(Example.class))).thenReturn(expectedEmployees);

        // when
        List<Employee> result = employeeService.findEmployeesByExample(probe);

        // then
        verify(employeeRepository).findAll(exampleCaptor.capture());
        Example<Employee> capturedExample = exampleCaptor.getValue();

        assertThat(result)
            .hasSize(2)
            .isEqualTo(expectedEmployees);
        assertThat(capturedExample.getProbe())
            .hasFieldOrPropertyWithValue("department", "IT")
            .hasFieldOrPropertyWithValue("position", "Developer");
    }

    @DisplayName("Should find employees with custom matcher")
    @Test
    void shouldFindEmployeesWithCustomMatcher() {
        // given
        String firstName = "John";
        String department = "eng";

        List<Employee> expectedEmployees = List.of(
            Employee.builder()
                    .firstName("John")
                    .lastName("Smith")
                    .department("Engineering")
                    .position("Engineer")
                    .build()
        );

        when(employeeRepository.findAll(any(Example.class))).thenReturn(expectedEmployees);

        // when
        List<Employee> result = employeeService.findEmployeesWithCustomMatcher(firstName, department);

        // then
        verify(employeeRepository).findAll(exampleCaptor.capture());
        Example<Employee> capturedExample = exampleCaptor.getValue();
        ExampleMatcher capturedMatcher = capturedExample.getMatcher();
        assertThat(result).isEqualTo(expectedEmployees);
        assertThat(capturedExample.getProbe())
            .hasFieldOrPropertyWithValue("firstName", "John")
            .hasFieldOrPropertyWithValue("department", "eng");
        assertThat(capturedMatcher.getNullHandler()).isEqualTo(ExampleMatcher.NullHandler.IGNORE);
        assertThat(capturedMatcher.getDefaultStringMatcher())
            .isEqualTo(ExampleMatcher.StringMatcher.EXACT);
    }

    @DisplayName("Should find one employee by example")
    @Test
    void shouldFindOneEmployeeByExample() {
        // given
        Employee probe = Employee.builder()
                                 .firstName("Jane")
                                 .lastName("Doe")
                                 .department("IT")
                                 .position("Developer")
                                 .build();
        Employee expectedEmployee = Employee.builder()
                                            .id(1L)
                                            .firstName("Jane")
                                            .lastName("Doe")
                                            .department("IT")
                                            .position("Developer")
                                            .build();
        when(employeeRepository.findOne(any(Example.class)))
            .thenReturn(Optional.of(expectedEmployee));

        // when
        Optional<Employee> result = employeeService.findOneEmployeeByExample(probe);

        // then
        verify(employeeRepository).findOne(exampleCaptor.capture());
        assertThat(result)
            .isPresent()
            .contains(expectedEmployee);
        assertThat(exampleCaptor.getValue()
                                .getProbe())
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(probe);
    }

    @DisplayName("Should count employees by example")
    @Test
    void shouldCountEmployeesByExample() {
        // given
        Employee probe = Employee.builder()
                                 .department("IT")
                                 .build();

        when(employeeRepository.count(any(Example.class))).thenReturn(5L);

        // when
        long count = employeeService.countEmployeesByExample(probe);

        // then
        verify(employeeRepository).count(exampleCaptor.capture());
        assertThat(count).isEqualTo(5L);
        assertThat(exampleCaptor.getValue()
                                .getProbe())
            .hasFieldOrPropertyWithValue("department", "IT");
    }

    @DisplayName("Should check if employees exist by example")
    @Test
    void shouldCheckIfEmployeesExistByExample() {
        // given
        Employee probe = Employee.builder()
                                 .department("IT")
                                 .position("Developer")
                                 .build();
        when(employeeRepository.exists(any(Example.class))).thenReturn(true);

        // when
        boolean exists = employeeService.existsByExample(probe);

        // Then
        verify(employeeRepository).exists(exampleCaptor.capture());
        assertThat(exists).isTrue();
        assertThat(exampleCaptor.getValue()
                                .getProbe())
            .hasFieldOrPropertyWithValue("department", "IT")
            .hasFieldOrPropertyWithValue("position", "Developer");
    }

    @DisplayName("Should handle empty results for custom matcher")
    @Test
    void shouldHandleEmptyResultsForCustomMatcher() {
        // given
        String firstName = "NonExistent";
        String department = "Unknown";
        when(employeeRepository.findAll(any(Example.class))).thenReturn(List.of());

        // when
        List<Employee> result = employeeService.findEmployeesWithCustomMatcher(firstName, department);

        // then
        verify(employeeRepository).findAll(exampleCaptor.capture());
        assertThat(result).isEmpty();
        assertThat(exampleCaptor.getValue()
                                .getProbe())
            .hasFieldOrPropertyWithValue("firstName", "NonExistent")
            .hasFieldOrPropertyWithValue("department", "Unknown");
    }

}
package com.spring.qbe.employee;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;


@Testcontainers
@SpringBootTest
class EmployeeRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:latest"));

    @Autowired
    private EmployeeRepository employeeRepository;


    @DisplayName("Should find all developers in IT department")
    @Test
    void shouldFindAllDevelopers() {
        // given
        Employee developerProbe = Employee.builder()
                                          .department("IT")
                                          .position("Developer")
                                          .build();

        // when
        List<Employee> developers = employeeRepository.findAll(Example.of(developerProbe));

        // then
        assertThat(developers)
            .hasSize(2)
            .extracting(Employee::getFirstName)
            .containsExactlyInAnyOrder("Jane", "Mike");
        assertThat(developers)
            .extracting(Employee::getLastName)
            .containsExactlyInAnyOrder("Doe", "Johnson");
        developers.forEach(dev -> {
            assertEquals("IT", dev.getDepartment());
            assertEquals("Developer", dev.getPosition());
        });
    }

    @DisplayName("Should find all employees with Smith lastname")
    @Test
    void shouldFindAllSmithEmployees() {
        // given
        Employee smithProbe = Employee.builder()
                                      .lastName("Smith")
                                      .build();

        // when
        List<Employee> smiths = employeeRepository.findAll(Example.of(smithProbe));

        // then
        assertThat(smiths)
            .hasSize(4)
            .extracting(Employee::getFirstName)
            .containsExactlyInAnyOrder("John", "Thomas", "Anna", "Robert");
        smiths.forEach(smith ->
            assertEquals("Smith", smith.getLastName(), "All employees should have Smith as lastname"));
    }

    @DisplayName("Should find all John-like names with case-insensitive partial match")
    @Test
    void shouldFindAllJohnVariations() {
        // given
        Employee johnProbe = Employee.builder()
                                     .firstName("john")
                                     .build();
        ExampleMatcher nameMatcher = ExampleMatcher.matching()
                                                   .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        // when
        List<Employee> johns = employeeRepository.findAll(Example.of(johnProbe, nameMatcher));

        // then
        assertThat(johns)
            .hasSize(2)
            .extracting(Employee::getFirstName)
            .containsExactlyInAnyOrder("John", "Johnny");
        johns.forEach(john ->
            assertTrue(john.getFirstName()
                           .toLowerCase()
                           .contains("john"), "Each name should contain 'john'"));
    }

    @DisplayName("Should find all managers across departments")
    @Test
    void shouldFindAllManagers() {
        // given
        Employee managerProbe = Employee.builder()
                                        .position("Manager")
                                        .build();

        // when
        List<Employee> managers = employeeRepository.findAll(Example.of(managerProbe));

        // then
        assertThat(managers)
            .hasSize(4)
            .extracting(Employee::getDepartment)
            .containsExactlyInAnyOrder("HR", "Marketing", "Sales", "Operations");
        managers.forEach(manager -> {
            assertEquals("Manager", manager.getPosition(), "All employees should be managers");
        });
    }

    @DisplayName("Should find engineers in Engineering department using complex matcher")
    @Test
    void shouldFindEngineersWithComplexCriteria() {
        // given
        Employee complexProbe = Employee.builder()
                                        .department("Engineering")
                                        .position("Engineer")
                                        .build();
        ExampleMatcher complexMatcher = ExampleMatcher.matching()
                                                      .withIgnoreNullValues()
                                                      .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        // when
        List<Employee> engineers = employeeRepository.findAll(Example.of(complexProbe, complexMatcher));

        // then
        assertThat(engineers)
            .hasSize(4)
            .extracting(Employee::getPosition)
            .allMatch(position -> position.contains("Engineer"));
        engineers.forEach(engineer -> {
            assertEquals("Engineering", engineer.getDepartment());
            assertTrue(engineer.getPosition()
                               .contains("Engineer"), "Position should contain 'Engineer'");
        });
    }

    @DisplayName("Should find no matches for non-existent criteria")
    @Test
    void shouldFindNoMatchesForNonExistentCriteria() {
        // given
        Employee nonExistentProbe = Employee.builder()
                                            .department("Non-Existent")
                                            .position("Imaginary Position")
                                            .build();

        // when
        List<Employee> results = employeeRepository.findAll(Example.of(nonExistentProbe));

        // then
        assertThat(results).isEmpty();
    }

    @DisplayName("Should find single employee by exact match")
    @Test
    void shouldFindSingleEmployeeByExactMatch() {
        // given
        Employee specificProbe = Employee.builder()
                                         .firstName("Jane")
                                         .lastName("Doe")
                                         .department("IT")
                                         .position("Developer")
                                         .build();

        // when
        Example<Employee> example = Example.of(specificProbe);
        boolean exists = employeeRepository.exists(example);
        Optional<Employee> employee = employeeRepository.findOne(example);

        // then
        assertTrue(exists, "Should find exact match");
        assertTrue(employee.isPresent(), "Should find the employee");
        employee.ifPresent(emp -> {
            assertEquals("Jane", emp.getFirstName());
            assertEquals("Doe", emp.getLastName());
            assertEquals("IT", emp.getDepartment());
            assertEquals("Developer", emp.getPosition());
        });
    }

    @DisplayName("Should handle null values in probe entity")
    @Test
    void shouldHandleNullValuesInProbe() {
        // given
        Employee probeWithNulls = Employee.builder()
                                          .department("IT")
                                          .firstName(null)
                                          .lastName(null)
                                          .position(null)
                                          .build();
        ExampleMatcher matcherIgnoringNulls = ExampleMatcher.matching()
                                                            .withIgnoreNullValues();

        // when
        List<Employee> itEmployees = employeeRepository.findAll(
            Example.of(probeWithNulls, matcherIgnoringNulls));

        // then
        assertThat(itEmployees)
            .isNotEmpty()
            .allMatch(employee -> "IT".equals(employee.getDepartment()));
        long expectedItEmployeeCount = employeeRepository.findAll()
                                                         .stream()
                                                         .filter(e -> "IT".equals(e.getDepartment()))
                                                         .count();
        assertEquals(expectedItEmployeeCount, itEmployees.size(), "Should find all IT department employees regardless of other fields");
    }

}
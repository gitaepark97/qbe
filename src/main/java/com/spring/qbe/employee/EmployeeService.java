package com.spring.qbe.employee;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> findEmployeesByExample(Employee employee) {
        Example<Employee> example = Example.of(employee);
        return employeeRepository.findAll(example);
    }

    public Optional<Employee> findOneEmployeeByExample(Employee employee) {
        Example<Employee> example = Example.of(employee);
        return employeeRepository.findOne(example);
    }

    public List<Employee> findEmployeesWithCustomMatcher(String firstName, String department) {
        Employee employee = Employee.builder()
                                    .firstName(firstName)
                                    .department(department)
                                    .build();

        ExampleMatcher matcher = ExampleMatcher.matching()
                                               .withIgnoreNullValues()
                                               .withStringMatcher(ExampleMatcher.StringMatcher.EXACT)
                                               .withMatcher("department", ExampleMatcher.GenericPropertyMatcher::contains);

        Example<Employee> example = Example.of(employee, matcher);
        return employeeRepository.findAll(example);
    }

    public long countEmployeesByExample(Employee employee) {
        Example<Employee> example = Example.of(employee);
        return employeeRepository.count(example);
    }

    public boolean existsByExample(Employee employee) {
        Example<Employee> example = Example.of(employee);
        return employeeRepository.exists(example);
    }

}

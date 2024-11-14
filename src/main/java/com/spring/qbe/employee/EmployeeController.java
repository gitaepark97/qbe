package com.spring.qbe.employee;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/search")
    public List<Employee> searchEmployees(
        @RequestParam(required = false) String firstName,
        @RequestParam(required = false) String department
    ) {
        return employeeService
            .findEmployeesWithCustomMatcher(firstName, department);
    }

    @PostMapping("/search/example")
    public List<Employee> findByExample(@RequestBody @Valid Employee employee) {
        return employeeService.findEmployeesByExample(employee);
    }

    @PostMapping("/search/example/one")
    public Employee findOneByExample(@RequestBody @Valid Employee employee) {
        return employeeService.findOneEmployeeByExample(employee)
                              .orElseThrow(() -> new EmployeeNotFoundException("No employee found matching the example"));
    }

    @PostMapping("/count")
    public long countByExample(@RequestBody @Valid Employee employee) {
        return employeeService.countEmployeesByExample(employee);
    }

    @PostMapping("/exists")
    public boolean existsByExample(@RequestBody @Valid Employee employee) {
        return employeeService.existsByExample(employee);
    }

}

package com.employee.management.controller;

import com.employee.management.exception.EmployeeNotFoundException;
import com.employee.management.model.Employee;
import com.employee.management.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class EmployeeController {

    @Autowired
    EmployeeRepository employeeRepository;

    /**
     * Get all the employees
     *
     * @return ResponseEntity
     */
    @GetMapping("/employees")
    @Cacheable(value="employees")
    public ResponseEntity<List<Employee>> getEmployees() {
        try {
            return new ResponseEntity<>(employeeRepository.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get the employee by id
     *
     * @param id
     * @return ResponseEntity
     */
    @GetMapping("/employee/{id}")
    @Cacheable(value="employees", key="#id")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable("id") long id) throws EmployeeNotFoundException {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found for this id : "+id));
        return ResponseEntity.ok().body(employee);
    }

    /**
     * Create new employee
     *
     * @param employee
     * @return ResponseEntity
     */
    @PostMapping("/employee")
    public ResponseEntity<Employee> newEmployee(@RequestBody Employee employee) {
        Employee newEmployee = employeeRepository
                .save(Employee.builder()
                        .name(employee.getName())
                        .address(employee.getAddress())
                        .role(employee.getRole())
                        .build());
        return new ResponseEntity<>(newEmployee, HttpStatus.OK);
    }

    /**
     * Update Employee record by using it's id
     *
     * @param id
     * @param employee
     * @return
     */
    @PutMapping("/employee/{id}")
    @CachePut(value="employees", key="#id")
    public ResponseEntity<Employee> updateEmployee(@PathVariable("id") long id, @RequestBody Employee employee) throws EmployeeNotFoundException {

        Employee employee1 =  employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found for this id : "+id));
        employee1.setName(employee.getName());
        employee1.setAddress(employee.getAddress());
        employee1.setRole(employee.getRole());
        final Employee updateEmployee = employeeRepository.save(employee1);
        return ResponseEntity.ok(updateEmployee);
    }

    /**
     * Delete Employee by Id
     *
     * @param id
     * @return ResponseEntity
     */
    @DeleteMapping("/employee/{id}")
    @CacheEvict(value="employees", key="#id")
    public Map<String, Boolean> deleteEmployeeById(@PathVariable("id") long id) throws EmployeeNotFoundException {

        Employee employee1 =  employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found for this id : "+id));
        employeeRepository.deleteById(id);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return response;
    }


    /**
     * Delete all employees
     *
     * @return ResponseEntity
     */
    @DeleteMapping("/employees")
    public ResponseEntity<HttpStatus> deleteAllEmployees() {
        try {
            employeeRepository.deleteAll();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * Method to get the employee record by id
     *
     * @param id
     * @return Employee
     */
    private Employee getEmpRec(long id) {
        Optional<Employee> empObj = employeeRepository.findById(id);

        if (empObj.isPresent()) {
            return empObj.get();
        }
        return null;
    }

}

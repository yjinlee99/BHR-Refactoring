package com.nineties.bhr.employee.service;

import com.nineties.bhr.employee.domain.Employees;
import com.nineties.bhr.employee.dto.EmployeeProfileDTO;
import com.nineties.bhr.employee.repository.EmployeesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeProfileService {

    @Autowired
    private EmployeesRepository employeesRepository;

    public EmployeeProfileDTO empProfile (String username) {

        Employees employees = employeesRepository.findByUsername(username);

        EmployeeProfileDTO empProfileDTO = new EmployeeProfileDTO();
        empProfileDTO.setName(employees.getName());
        empProfileDTO.setPosition(employees.getPosition());
        empProfileDTO.setDeptName(employees.getDept().getDeptName());
        empProfileDTO.setJobId(employees.getJobId());
        empProfileDTO.setHireDate(employees.getHireDate());
        empProfileDTO.setIntroduction(employees.getIntroduction());

        return empProfileDTO;
    }


    public void updateEmployeeIntroduction(String username, EmployeeProfileDTO employeeProfileDTO) {

        Employees employee = employeesRepository.findByUsername(username);

        employee.setIntroduction(employeeProfileDTO.getIntroduction());
        employeesRepository.save(employee);
    }
}


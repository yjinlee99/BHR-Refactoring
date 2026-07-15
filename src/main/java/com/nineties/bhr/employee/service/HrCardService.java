package com.nineties.bhr.employee.service;


import com.nineties.bhr.employee.dto.EmployeeProjection;
import com.nineties.bhr.employee.repository.DeptRepository;
import com.nineties.bhr.employee.repository.EmployeesRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HrCardService {
    private final EmployeesRepository employeesRepository;
    private final DeptRepository deptRepository;

    public HrCardService(EmployeesRepository employeesRepository, DeptRepository deptRepository) {
        this.employeesRepository = employeesRepository;
        this.deptRepository = deptRepository;
    }

    public List<EmployeeProjection> findAllEmployeeSummary() {
        return employeesRepository.findEmpNoNameDeptNameEmail();
    }

    public List<String> findAllDept() {
        return deptRepository.findAllDeptNames();
    }

}

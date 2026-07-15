package com.nineties.bhr.employee.controller;


import com.nineties.bhr.employee.dto.EmployeeProjection;
import com.nineties.bhr.employee.service.HrCardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class HrCardController {

    private final HrCardService hrCardService;

    public HrCardController(HrCardService hrCardService) {
        this.hrCardService = hrCardService;
    }

    @GetMapping("/hrCard/empList")
    public List<EmployeeProjection> getEmployeeSummaries() {
        return hrCardService.findAllEmployeeSummary();
    }

    @GetMapping("/hrCard/deptList")
    public List<String> getDeptList() {
        return hrCardService.findAllDept();
    }
}

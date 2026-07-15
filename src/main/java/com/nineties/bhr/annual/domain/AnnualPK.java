package com.nineties.bhr.annual.domain;

import com.nineties.bhr.employee.domain.Employees;
import lombok.Data;

@Data
public class AnnualPK {

    private String annualYear;

    private Employees employees;

}

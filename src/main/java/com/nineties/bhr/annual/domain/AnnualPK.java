package com.nineties.bhr.annual.domain;

import com.nineties.bhr.employee.domain.Employees;
import java.io.Serializable;
import lombok.Data;

@Data
public class AnnualPK implements Serializable {

    private String annualYear;

    private String employees;
}

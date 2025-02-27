package com.chargepoint.csms.authentication.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;

@Data
@Table("driver")
public class Driver {

    @Id
    private String driverIdentifier;
    private BigDecimal credit;
    private String name;
    private String surname;
    private String phoneNumber;
}
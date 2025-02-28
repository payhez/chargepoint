package com.chargepoint.csms.authentication.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Driver {

    @Id
    private Long id;

    @Column("driver_identifier")
    private String driverIdentifier;

    private BigDecimal credit;
    private String name;
    private String surname;

    @Column("phone_number")
    private String phoneNumber;
}
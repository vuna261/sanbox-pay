package com.cloudnut.payment.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pay_code")
public class PayCodeEntityDB {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "pay_code")
    private String payCode;

    @Column(name = "used")
    private Boolean used;

    @Column(name = "created_date")
    private Date createdDate;
}

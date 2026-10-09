package com.example.SPSProjectBackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "ncre_addendums", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Addendum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "addendum_id")
    private Long addendumId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agreement_id", nullable = false)
    @JsonIgnore
    private Agreement agreement;

    @Column(name = "developer_name", length = 150)
    private String developerName;

    @Column(name = "new_sppa_signed")
    @Temporal(TemporalType.DATE)
    private Date newSppaSigned;

    @Column(name = "initial_tariff_revised", length = 50)
    private String initialTariffRevised;

    @Column(name = "expiration_extension_date")
    @Temporal(TemporalType.DATE)
    private Date expirationExtensionDate;

    @Column(name = "recommissioned_on")
    @Temporal(TemporalType.DATE)
    private Date recommissionedOn;
}

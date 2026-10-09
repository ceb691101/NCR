package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "ncre_ru_change", schema = "dbadmin")
@IdClass(NcreRuChangeId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NcreRuChange {

    @Id
    @Column(name = "folio_no", nullable = false)
    private Short folioNo;

    @Column(name = "curent_ru")
    private Short curentRu;

    @Column(name = "change_ru")
    private Short changeRu;

    @Column(name = "ent_by", length = 12)
    private String entBy;

    @Id
    @Column(name = "ent_dt", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date entDt;
}

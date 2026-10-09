package com.example.SPSProjectBackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ncre_type", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NcreType {

    @Id
    @Column(name = "type_id", length = 4, nullable = false)
    private String typeId;

    @Column(name = "type_nm", length = 60)
    private String typeName;

    @Column(name = "status", length = 1)
    private String status;
}

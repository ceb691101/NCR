package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "ncre_documents", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NcreDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "ref_id", nullable = false)
    private Long referenceId;

    @Column(name = "ref_type", length = 20, nullable = false)
    private String referenceType;

    @Column(name = "doc_category", length = 50, nullable = false)
    private String documentCategory;

    @Column(name = "doc_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "doc_path", length = 255, nullable = false)
    private String filePath;

    @Column(name = "upld_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadedAt;
}

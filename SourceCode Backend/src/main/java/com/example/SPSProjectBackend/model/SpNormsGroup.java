package com.example.SPSProjectBackend.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SPNORMSGROUP")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpNormsGroup {

    @Id
    @Column(name = "SECTIONTYPEID", nullable = false)
    private String sectionTypeId;

    @Column(name = "LINEPARENTID")
    private String lineParentId;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "LINESUMMARYID")
    private String lineSummaryId;

    @Column(name = "LINE_PARENT_ID")
    private String line_parent_id;

    @Column(name = "LINE_SUMMARY_ID")
    private String line_summary_id;
}


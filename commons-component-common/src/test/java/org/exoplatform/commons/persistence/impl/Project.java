package org.exoplatform.commons.persistence.impl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.exoplatform.commons.api.persistence.ExoEntity;

@ExoEntity
@Entity(name = "Project")
@Table(name = "PROJECT")
public class Project {

  @Id
  @GeneratedValue
  @Column(name = "PROJECT_ID")
  private Long id;

  public Long getId() {
    return id;
  }

}

package org.exoplatform.commons.persistence.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

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

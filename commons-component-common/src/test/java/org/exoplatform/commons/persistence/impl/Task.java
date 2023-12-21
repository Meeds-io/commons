package org.exoplatform.commons.persistence.impl;

import org.exoplatform.commons.api.persistence.ExoEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@ExoEntity
@Entity(name = "Task")
@Table(name = "TASK")
public class Task {
  @Id
  @GeneratedValue(generator = "hibernate_sequence")
  @Column(name = "TASK_ID")
  private Long id;

  // uniqueness constraint to test failures and rollbacks on transaction commits
  @Column(name = "TASK_NAME", unique = true)
  private String name;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}

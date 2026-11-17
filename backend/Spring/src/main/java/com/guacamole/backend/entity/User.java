package com.guacamole.backend.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/** Usuario identificado por email unico, com vinculos de watchlist e conclusoes de aulas. */
@Entity
@Table(name = "user")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	@JsonIgnore
	private String password;
	// O lado ManyToOne controla a FK; alterar apenas estas colecoes nao persiste o vinculo.
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	@JsonIgnore
	private List<CourseWatchlist> watchlistEntries = new ArrayList<>();

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	@JsonIgnore
	private List<LessonCompletion> lessonCompletions = new ArrayList<>();

	public User() {
	}

	public User(String name, String email, String password) {
		this.name = name;
		this.email = email;
		this.password = password;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	/** Recebe o hash da senha, calculado pela camada de autenticacao. */
	public void setPassword(String password) {
		this.password = password;
	}

	public List<CourseWatchlist> getWatchlistEntries() {
		return watchlistEntries;
	}

	public List<LessonCompletion> getLessonCompletions() {
		return lessonCompletions;
	}
}

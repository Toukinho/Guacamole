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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/** Curso ministrado por um professor; suas aulas pertencem ao curso e sao ordenadas por posicao. */
@Entity
@Table(name = "course")
public class Course {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "teacher_id", nullable = false)
	private Teacher teacher;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "youtube_id_playlist")
	private String youtubeIdPlaylist;
	// O lado ManyToOne controla a FK; alterar apenas estas colecoes nao persiste o vinculo.
	@OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
	@OrderBy("position ASC")
	@JsonIgnore
	private List<Lesson> lessons = new ArrayList<>();

	@OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
	@JsonIgnore
	private List<CourseWatchlist> watchlistEntries = new ArrayList<>();

	public Course() {
	}

	public Course(Teacher teacher, String title, String description, String youtubeIdPlaylist) {
		this.teacher = teacher;
		this.title = title;
		this.description = description;
		this.youtubeIdPlaylist = youtubeIdPlaylist;
	}

	public Long getId() {
		return id;
	}

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getYoutubeIdPlaylist() {
		return youtubeIdPlaylist;
	}

	public void setYoutubeIdPlaylist(String youtubeIdPlaylist) {
		this.youtubeIdPlaylist = youtubeIdPlaylist;
	}

	public List<Lesson> getLessons() {
		return lessons;
	}

	public List<CourseWatchlist> getWatchlistEntries() {
		return watchlistEntries;
	}
}

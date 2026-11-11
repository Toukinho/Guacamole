package com.guacamole.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
}

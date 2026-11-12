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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Aula pertencente a um curso; a posicao e unica dentro desse curso. */
@Entity
@Table(name = "lesson", uniqueConstraints = @UniqueConstraint(name = "uk_lesson_course_position", columnNames = {"course_id", "position"}))
public class Lesson {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@Column(nullable = false)
	private String title;

	@Column(name = "youtube_id_video")
	private String youtubeIdVideo;

	@Column(nullable = false)
	private Integer position;
	// O lado ManyToOne controla a FK; alterar apenas esta colecao nao persiste o vinculo.
	@OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY)
	@JsonIgnore
	private List<LessonCompletion> completions = new ArrayList<>();

	public Lesson() {
	}

	public Lesson(Course course, String title, String youtubeIdVideo, Integer position) {
		this.course = course;
		this.title = title;
		this.youtubeIdVideo = youtubeIdVideo;
		this.position = position;
	}

	public Long getId() {
		return id;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getYoutubeIdVideo() {
		return youtubeIdVideo;
	}

	public void setYoutubeIdVideo(String youtubeIdVideo) {
		this.youtubeIdVideo = youtubeIdVideo;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public List<LessonCompletion> getCompletions() {
		return completions;
	}
}

package com.guacamole.backend.services;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import com.guacamole.backend.entity.Course;
import com.guacamole.backend.entity.Lesson;
import com.guacamole.backend.entity.Teacher;
import com.guacamole.backend.repositories.CourseRepository;
import com.guacamole.backend.repositories.LessonRepository;
import com.guacamole.backend.repositories.TeacherRepository;

/** Importação explícita de uma playlist pública; não atualiza cursos existentes. */
@Service
public class YoutubeImportService {
	private final CourseRepository courses;
	private final LessonRepository lessons;
	private final TeacherRepository teachers;
	private final RestClient client;

	@org.springframework.beans.factory.annotation.Autowired
	public YoutubeImportService(CourseRepository courses, LessonRepository lessons, TeacherRepository teachers) {
		this(courses, lessons, teachers, client());
	}

	YoutubeImportService(CourseRepository courses, LessonRepository lessons, TeacherRepository teachers, RestClient client) {
		this.courses = courses;
		this.lessons = lessons;
		this.teachers = teachers;
		this.client = client;
	}

	private static RestClient client() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofSeconds(10));
		factory.setReadTimeout(Duration.ofSeconds(20));
		return RestClient.builder().baseUrl("https://www.googleapis.com/youtube/v3").requestFactory(factory).build();
	}

	@Transactional
	public void importPlaylist(String playlistId, String key) {
		if (!playlistId.matches("[A-Za-z0-9_-]{1,255}") || key.isBlank()) throw new IllegalArgumentException("Playlist ID and API key are required");
		if (courses.existsByYoutubeIdPlaylist(playlistId)) throw new IllegalStateException("Playlist already imported");
		JsonNode playlist = client.get().uri(b -> b.path("/playlists").queryParam("part", "snippet").queryParam("id", playlistId).build()).header("x-goog-api-key", key)
				.retrieve().body(JsonNode.class);
		if (playlist == null || playlist.path("items").isEmpty()) throw new IllegalArgumentException("Playlist not found or unavailable");
		JsonNode info = playlist.path("items").get(0).path("snippet");
		List<JsonNode> videos = new ArrayList<>();
		String page = "";
		do {
			String currentPage = page;
			JsonNode result = client.get().uri(b -> {
				b.path("/playlistItems").queryParam("part", "snippet").queryParam("playlistId", playlistId).queryParam("maxResults", 50);
				if (!currentPage.isEmpty()) b.queryParam("pageToken", currentPage);
				return b.build();
			}).header("x-goog-api-key", key).retrieve().body(JsonNode.class);
			if (result == null || !result.path("items").isArray()) throw new IllegalStateException("Invalid YouTube response");
			result.path("items").forEach(videos::add);
			page = result.path("nextPageToken").asText("");
		} while (!page.isEmpty());
		Teacher teacher = teachers.save(new Teacher(info.path("channelTitle").asText()));
		Course course = courses.save(new Course(teacher, info.path("title").asText(), info.path("description").asText(), playlistId));
		for (JsonNode video : videos) {
			JsonNode snippet = video.path("snippet");
			String id = snippet.path("resourceId").path("videoId").asText();
			String title = snippet.path("title").asText();
			if (!id.isBlank() && !title.equals("Private video") && !title.equals("Deleted video")) {
				lessons.save(new Lesson(course, title, id, snippet.path("position").asInt() + 1));
			}
		}
	}
}

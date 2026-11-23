package com.guacamole.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.client.RestClient;
import com.guacamole.backend.entity.Course;
import com.guacamole.backend.entity.Lesson;
import com.guacamole.backend.entity.Teacher;
import com.guacamole.backend.repositories.CourseRepository;
import com.guacamole.backend.repositories.LessonRepository;
import com.guacamole.backend.repositories.TeacherRepository;

class YoutubeImportServiceTests {
	@Test
	void youtubeHttpFailureDoesNotCreateAnyRecords() throws Exception {
		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/playlists", exchange -> {
			exchange.sendResponseHeaders(403, -1);
			exchange.close();
		});
		server.start();
		try {
			CourseRepository courses = mock(CourseRepository.class);
			LessonRepository lessons = mock(LessonRepository.class);
			TeacherRepository teachers = mock(TeacherRepository.class);
			RestClient client = RestClient.builder().baseUrl("http://127.0.0.1:" + server.getAddress().getPort()).build();
			YoutubeImportService importer = new YoutubeImportService(courses, lessons, teachers, client);
			assertThrows(org.springframework.web.client.RestClientResponseException.class, () -> importer.importPlaylist("playlist", "key"));
			verify(teachers, never()).save(any());
			verify(courses, never()).save(any());
			verify(lessons, never()).save(any());
		} finally {
			server.stop(0);
		}
	}

	@Test
	void importsAllPagesInPlaylistOrderAndRejectsDuplicates() throws Exception {
		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/playlists", exchange -> respond(exchange, "{\"items\":[{\"snippet\":{\"title\":\"Curso\",\"description\":\"Descrição\",\"channelTitle\":\"Professor\"}}]}"));
		server.createContext("/playlistItems", exchange -> {
			assertEquals("key", exchange.getRequestHeaders().getFirst("x-goog-api-key"));
			String query = exchange.getRequestURI().getQuery();
			respond(exchange, query.contains("pageToken=")
					? "{\"items\":[{\"snippet\":{\"title\":\"Aula 2\",\"position\":1,\"resourceId\":{\"videoId\":\"video2\"}}}]}"
					: "{\"nextPageToken\":\"next\",\"items\":[{\"snippet\":{\"title\":\"Aula 1\",\"position\":0,\"resourceId\":{\"videoId\":\"video1\"}}}]}" );
		});
		server.start();
		try {
			CourseRepository courses = mock(CourseRepository.class);
			LessonRepository lessons = mock(LessonRepository.class);
			TeacherRepository teachers = mock(TeacherRepository.class);
			when(teachers.save(any(Teacher.class))).thenAnswer(invocation -> invocation.getArgument(0));
			when(courses.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
			RestClient client = RestClient.builder().baseUrl("http://127.0.0.1:" + server.getAddress().getPort()).build();
			YoutubeImportService importer = new YoutubeImportService(courses, lessons, teachers, client);
			importer.importPlaylist("playlist", "key");
			ArgumentCaptor<Lesson> captured = ArgumentCaptor.forClass(Lesson.class);
			verify(lessons, times(2)).save(captured.capture());
			assertEquals("video1", captured.getAllValues().get(0).getYoutubeIdVideo());
			assertEquals(2, captured.getAllValues().get(1).getPosition());
			when(courses.existsByYoutubeIdPlaylist("playlist")).thenReturn(true);
			assertThrows(IllegalStateException.class, () -> importer.importPlaylist("playlist", "key"));
		} finally {
			server.stop(0);
		}
	}

	private static void respond(com.sun.net.httpserver.HttpExchange exchange, String body) throws java.io.IOException {
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "application/json");
		exchange.sendResponseHeaders(200, bytes.length);
		try (var output = exchange.getResponseBody()) { output.write(bytes); }
	}
}

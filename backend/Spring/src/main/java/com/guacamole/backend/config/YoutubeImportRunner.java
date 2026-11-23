package com.guacamole.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.guacamole.backend.services.YoutubeImportService;

@Configuration
public class YoutubeImportRunner {
	@Bean
	@ConditionalOnProperty(name = "youtube.import.playlist")
	ApplicationRunner importYoutube(YoutubeImportService importer,
			@Value("${youtube.import.playlist}") String playlist,
			@Value("${YOUTUBE_API_KEY:}") String key) {
		return args -> importer.importPlaylist(playlist, key);
	}
}

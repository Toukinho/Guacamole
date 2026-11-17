CREATE TABLE `user` (
	id BIGINT NOT NULL AUTO_INCREMENT,
	name VARCHAR(255) NOT NULL,
	email VARCHAR(255) NOT NULL,
	password VARCHAR(255) NOT NULL,
	PRIMARY KEY (id),
	CONSTRAINT uk_user_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE teacher (
	id BIGINT NOT NULL AUTO_INCREMENT,
	name VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE course (
	id BIGINT NOT NULL AUTO_INCREMENT,
	teacher_id BIGINT NOT NULL,
	title VARCHAR(255) NOT NULL,
	description TEXT,
	youtube_id_playlist VARCHAR(255),
	PRIMARY KEY (id),
	CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES teacher (id)
) ENGINE=InnoDB;

CREATE TABLE lesson (
	id BIGINT NOT NULL AUTO_INCREMENT,
	course_id BIGINT NOT NULL,
	title VARCHAR(255) NOT NULL,
	youtube_id_video VARCHAR(255),
	position INT NOT NULL,
	PRIMARY KEY (id),
	CONSTRAINT uk_lesson_course_position UNIQUE (course_id, position),
	CONSTRAINT fk_lesson_course FOREIGN KEY (course_id) REFERENCES course (id)
) ENGINE=InnoDB;

CREATE TABLE course_watchlist (
	user_id BIGINT NOT NULL,
	course_id BIGINT NOT NULL,
	created_at DATETIME(6) NOT NULL,
	PRIMARY KEY (user_id, course_id),
	CONSTRAINT fk_watchlist_user FOREIGN KEY (user_id) REFERENCES `user` (id),
	CONSTRAINT fk_watchlist_course FOREIGN KEY (course_id) REFERENCES course (id)
) ENGINE=InnoDB;

CREATE TABLE lesson_completion (
	user_id BIGINT NOT NULL,
	lesson_id BIGINT NOT NULL,
	completed_at DATETIME(6) NOT NULL,
	PRIMARY KEY (user_id, lesson_id),
	CONSTRAINT fk_completion_user FOREIGN KEY (user_id) REFERENCES `user` (id),
	CONSTRAINT fk_completion_lesson FOREIGN KEY (lesson_id) REFERENCES lesson (id)
) ENGINE=InnoDB;

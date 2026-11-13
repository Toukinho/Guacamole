package com.guacamole.backend.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import com.guacamole.backend.entity.Teacher;
/** Acesso aos professores por meio das operacoes de JpaRepository. */
public interface TeacherRepository extends JpaRepository<Teacher, Long> { }

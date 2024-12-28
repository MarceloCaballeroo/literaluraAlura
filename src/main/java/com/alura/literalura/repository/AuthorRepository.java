package com.alura.literalura.repository;

import com.alura.literalura.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    @Query("SELECT a FROM Author a WHERE :year BETWEEN a.birthYear AND a.deathYear OR (a.birthYear <= :year AND a.deathYear IS NULL)")
    List<Author> findAuthorsAliveInYear(@Param("year") Integer year);
}
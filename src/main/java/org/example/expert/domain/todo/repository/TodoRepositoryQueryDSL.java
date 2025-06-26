package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.response.TodoQueryDslResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepositoryQueryDSL {
    Optional<Todo> findByIdWithUser(Long todoId);

    Page<TodoQueryDslResponse> findAllTodoQueryDSL(String title, LocalDateTime startDateTime, LocalDateTime endDateTime, String orderBy, String managerNickname, Pageable pageable);
}

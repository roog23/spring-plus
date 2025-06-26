package org.example.expert.domain.todo.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TodoQueryDslResponse {
    private final Long id;
    private final String title;
    private final Long managerCnt;
    private final Long commentCnt;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public TodoQueryDslResponse(Long id, String title, Long managerCnt, Long commentCnt, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.title = title;
        this.managerCnt = managerCnt;
        this.commentCnt = commentCnt;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }
}

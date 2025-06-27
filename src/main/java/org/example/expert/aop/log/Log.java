package org.example.expert.aop.log;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Entity
@Table(name = "log")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Log {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    private LocalDateTime createdAt;

    private Long userId;
    private String requestUrl;
    private Long requestManagerUserId;
    private Boolean requestResult;

    public Log(Long userId, String requestUrl, Long requestManagerUserId, Boolean requestResult) {
        this.userId = userId;
        this.requestUrl = requestUrl;
        this.requestManagerUserId = requestManagerUserId;
        this.requestResult = requestResult;
    }
}

package org.example.expert.domain.todo.repository;


import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.TodoQueryDslResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;

@RequiredArgsConstructor
public class TodoRepositoryQueryDSLImpl implements TodoRepositoryQueryDSL {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        QTodo todo = QTodo.todo;
        return jpaQueryFactory.selectFrom(todo)
                .leftJoin(todo.user).fetchJoin()
                .where(todo.id.eq(todoId))
                .stream().findFirst();
    }

    @Override
    public Page<TodoQueryDslResponse> findAllTodoQueryDSL(String title, LocalDateTime startDateTime, LocalDateTime endDateTime, String orderBy, String managerNickname, Pageable pageable) {
        QTodo todo = QTodo.todo;
        var query = jpaQueryFactory.select(
                Projections.constructor(TodoQueryDslResponse.class, //TodoQueryDslResponse의 구조로 값을 반환
                        todo.id,
                        todo.title,
                        JPAExpressions.select(manager.count()).from(manager).where(manager.todo.eq(todo)),
                        JPAExpressions.select(comment.count()).from(comment).where(comment.todo.eq(todo)),
                        todo.createdAt,
                        todo.modifiedAt
                ))
                .distinct()     //leftjoin으로 매니저가 2명인 경우 중복 출력되서 중복 제거
                .from(todo)
                .leftJoin(todo.user)
                .leftJoin(todo.managers, manager)
                .where(titleEq(title))      // 제목으로 조회
                .where(nicknameEq(managerNickname)) //매니저 닉네임으로 조회
                .where(duringDateTime(startDateTime, endDateTime))  //일정 생성일의 범위로 조회
                .orderBy(createOrderSpecifier(orderBy)) //기본은 생성일 최신순, modifiedAt 입력시 수정일 최신순으로 정렬
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        // 총 개수 조회
        long totalSize = jpaQueryFactory.select(todo.count()).from(todo)
                .leftJoin(todo.user)
                .leftJoin(todo.managers, manager)
                .where(titleEq(title))
                .where(nicknameEq(managerNickname)).fetch().get(0);

        //리스트 형태를 페이지 형태로 변경
        return PageableExecutionUtils.getPage(query, pageable, () -> totalSize);
    }

    // 시간 범위를 지정 받으면 그에 해당하는 쿼리를 넣고 아닌 경우 null을 넣어서 넘김
    private BooleanExpression duringDateTime(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if(startDateTime == null && endDateTime == null) {
            return null;
        }
        else if(endDateTime == null) {
            return todo.createdAt.after(startDateTime);
        }
        else if(startDateTime == null) {
            return todo.createdAt.before(endDateTime);
        }
        return todo.createdAt.between(startDateTime, endDateTime);
    }

    // 닉네임 입력이 들어오면 조회 쿼리를 넣고 아닌 경우 null을 넣어서 넘김
    private BooleanExpression nicknameEq(String managerNickname) {
        return Objects.nonNull(managerNickname) ? manager.user.nickname.contains(managerNickname) : null;
    }

    // 제목 입력이 들어오면 조회 쿼리를 넣고 아닌 경우 null을 넣어서 넘김
    private BooleanExpression titleEq(String title) {
        return Objects.nonNull(title) ? todo.title.contains(title) : null;
    }

    // modifiedAt 입력이 안들어오면 생성일 최신순으로 정렬
    private OrderSpecifier createOrderSpecifier(String orderBy) {
        if (orderBy != null && orderBy.equals("modifiedAt")) {
            return todo.modifiedAt.desc();
        } else {
            return todo.createdAt.desc();
        }
    }
}

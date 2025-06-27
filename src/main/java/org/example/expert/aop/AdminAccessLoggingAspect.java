package org.example.expert.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.expert.aop.log.Log;
import org.example.expert.aop.log.LogRepository;
import org.example.expert.config.dto.CustomUserDetails;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AdminAccessLoggingAspect {

    private final HttpServletRequest request;
    private final LogRepository logRepository;

    @Before("execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    public void logBeforeChangeUserRole(JoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        Long userId = principal.getUserId();
        String requestUrl = request.getRequestURI();
        LocalDateTime requestTime = LocalDateTime.now();

        log.info("Admin Access Log - User ID: {}, Request Time: {}, Request URL: {}, Method: {}",
                userId, requestTime, requestUrl, joinPoint.getSignature().getName());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Around("execution(* org.example.expert.domain.manager.controller.ManagerController.saveManager(..))")
    public Object logSaveManager(ProceedingJoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        Long userId = principal.getUserId();
        String requestURI = request.getRequestURI();
        Object[] args = joinPoint.getArgs();

        // 매니저로 등록하기 위해 요청(requestBody)한 유저아이디
        Long requestManagerUserId = 0L;
        for(Object arg : args) {
            if (arg instanceof ManagerSaveRequest managerSaveRequest) {
                requestManagerUserId = managerSaveRequest.getManagerUserId();
            }
        }

        // 성공, 실패 여부를 확인
        boolean check = false;
        try {
            Object result = joinPoint.proceed();
            check = true;
            return result;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally { // 메소드가 성공, 실패 여부에 상관없이 로그는 저장이 되어야합니다.
            Log logging = new Log(userId, requestURI, requestManagerUserId, check);
            logRepository.save(logging);
        }
    }
}

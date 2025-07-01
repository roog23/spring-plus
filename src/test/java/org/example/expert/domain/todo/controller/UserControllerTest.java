package org.example.expert.domain.todo.controller;

import com.navercorp.fixturemonkey.FixtureMonkey;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserJdbcRepository;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static net.jqwik.api.Arbitraries.strings;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserJdbcRepository userJdbcRepository;

    //fixtureMonkey 유저 객체 랜덤 생성
    //batch insert 100만건 입력할때 빠르게 처리하기 위해서
    @Test
    @DisplayName("유저 100만건 생성")
    void createUser() {
        FixtureMonkey fixtureMonkey = FixtureMonkey.create();
        for(int i = 0; i < 100; i++) {
            List<User> users = fixtureMonkey.giveMeBuilder(User.class)
                    .set("nickname", strings().withCharRange('a', 'z').ofMinLength(8).ofMaxLength(12))
                    .set("email", null)
                    .set("userRole", UserRole.USER).sampleList(10000);
            userJdbcRepository.batchInsert(users);
        }
    }

    @Test
    @DisplayName("유저 닉네임 조회")
    void searchUser() throws Exception {

        mockMvc.perform(get("/users/search")
                        .param("userNickname", "nickname"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

    }
}

package com.mukplay.domain.user;

import com.mukplay.domain.user.dto.UserRankingResponse;
import com.mukplay.domain.user.entity.User;
import com.mukplay.domain.user.entity.UserRole;
import com.mukplay.domain.user.repository.UserRepository;
import com.mukplay.domain.user.service.UserRankingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RankingQueryTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserRankingService rankingService;

    @Test
    @DisplayName("경험치 내림차순으로 정렬된 상위 유저 랭킹 목록이 순위(1위, 2위, 3위...)와 함께 반환된다")
    void getTopRankingsOrderedByExpDesc() {
        User u1 = User.builder().loginId("user1").password("p").nickname("1위유저").role(UserRole.ROLE_USER).build();
        ReflectionTestUtils.setField(u1, "id", 101L);
        u1.addExp(500);

        User u2 = User.builder().loginId("user2").password("p").nickname("2위유저").role(UserRole.ROLE_USER).build();
        ReflectionTestUtils.setField(u2, "id", 102L);
        u2.addExp(300);

        User u3 = User.builder().loginId("user3").password("p").nickname("3위유저").role(UserRole.ROLE_USER).build();
        ReflectionTestUtils.setField(u3, "id", 103L);
        u3.addExp(100);

        given(userRepository.findTop100ByOrderByExpDesc()).willReturn(List.of(u1, u2, u3));

        List<UserRankingResponse> rankings = rankingService.getTopRankings();

        assertThat(rankings).hasSize(3);

        assertThat(rankings.get(0).rank()).isEqualTo(1);
        assertThat(rankings.get(0).userId()).isEqualTo(101L);
        assertThat(rankings.get(0).nickname()).isEqualTo("1위유저");
        assertThat(rankings.get(0).exp()).isEqualTo(500);

        assertThat(rankings.get(1).rank()).isEqualTo(2);
        assertThat(rankings.get(1).userId()).isEqualTo(102L);
        assertThat(rankings.get(1).exp()).isEqualTo(300);

        assertThat(rankings.get(2).rank()).isEqualTo(3);
        assertThat(rankings.get(2).userId()).isEqualTo(103L);
        assertThat(rankings.get(2).exp()).isEqualTo(100);
    }
}

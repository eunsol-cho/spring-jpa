package study.querydsl;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {

	private final InitMemberService initMemberService;

	@PostConstruct // 의존성 주입이 끝난 후에 실행해야 하는 초기화 로직
	public void init() {
		// 해당 메서드를 직접 여기 구현 하지 못하는건
		// 스프링의 라이프 사이클 때문임
		initMemberService.init();
	}

	@Component
	static class InitMemberService {
		@PersistenceContext
		EntityManager em;
		@Transactional
		public void init() {
			Team teamA = new Team("teamA");
			Team teamB = new Team("teamB");
			em.persist(teamA);
			em.persist(teamB);

			for (int i = 0; i < 100; i++) {
				Team selectedTeam = i % 2 == 0 ? teamA : teamB;
				em.persist(new Member("member" + i, i, selectedTeam));
			}
		}
	}

}

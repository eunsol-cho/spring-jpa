package study.querydsl.repository;

import static org.springframework.util.StringUtils.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QTeam;

@Repository
public class MemberJpaRepository {

	private final EntityManager em;
	private final JPAQueryFactory queryFactory;

	// 방법1
	/*public MemberJpaRepository(EntityManager em) {
		this.em = em;
		this.queryFactory = new JPAQueryFactory(em);
	}*/

	// 방법2
	// 빈으로 등록후 이런 방식을 쓸수 있다.
	// 테스트 코드 작성시에는 queryFactory를 주입받아야하므로 방법1이 더 편함!
	// queryFactory 동시성 문제?
	// EntityManager에 의존적이고, EntityManager는 트랜젝션 단위로 다른데 바인딩 되게끔 처리해줌
	public MemberJpaRepository(EntityManager em, JPAQueryFactory queryFactory) {
		this.em = em;
		this.queryFactory = queryFactory;
	}

	public void save(Member member) {
		em.persist(member);
	}
	public Optional<Member> findById(Long id) {
		Member findMember = em.find(Member.class, id);
		return Optional.ofNullable(findMember);
	}
	public List<Member> findAll() {
		return em.createQuery("select m from Member m", Member.class)
				.getResultList();
	}

	public List<Member> findAll_Querydsl() {
		return queryFactory
				.selectFrom(member).fetch();
	}

	public List<Member> findByUsername(String username) {
		return em.createQuery("select m from Member m where m.username = :username", Member.class)
				.setParameter("username", username)
				.getResultList();
	}

	public List<Member> findByUsername_Querydsl(String username) {
		return queryFactory
				.selectFrom(member)
				.where(member.username.eq(username))
				.fetch();
	}

	//Builder 사용
	//회원명, 팀명, 나이(ageGoe, ageLoe)
	public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {

		BooleanBuilder builder = new BooleanBuilder();
		if (hasText(condition.getUsername())) {
			builder.and(member.username.eq(condition.getUsername()));
		}
		if (hasText(condition.getTeamName())) {
			builder.and(team.name.eq(condition.getTeamName()));
		}
		if (condition.getAgeGoe() != null) {
			builder.and(member.age.goe(condition.getAgeGoe()));
		}
		if (condition.getAgeLoe() != null) {
			builder.and(member.age.loe(condition.getAgeLoe()));
		}

		return queryFactory
				.select(new QMemberTeamDto(
						member.id.as("memberId"),
						member.username,
						member.age,
						team.id.as("teamId"),
						team.name))
				.from(member)
				.leftJoin(member.team, team)
				.where(builder)
				.fetch();
	}

	public List<MemberTeamDto> search(MemberSearchCondition condition) {

		return queryFactory
				.select(new QMemberTeamDto(
						member.id.as("memberId"),
						member.username,
						member.age,
						team.id.as("teamId"),
						team.name))
				.from(member)
				.leftJoin(member.team, team)
				.where(
						usernameEq(condition.getUsername()),
						teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()),
						ageLoe(condition.getAgeLoe())
				)
				.fetch();

	}

	//where 파라미터 방식은 이런식으로 재사용이 가능하다.
	public List<Member> searchMember(MemberSearchCondition condition) {
		return queryFactory
				.selectFrom(member)
				.leftJoin(member.team, team)
				.where(
						usernameEq(condition.getUsername()),
						teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()),
						ageLoe(condition.getAgeLoe())
				)
				.fetch();
	}

	private BooleanExpression usernameEq(String username) {
		return isEmpty(username) ? null : member.username.eq(username);
	}
	private BooleanExpression teamNameEq(String teamName) {
		return isEmpty(teamName) ? null : team.name.eq(teamName);
	}

	private BooleanExpression ageGoe(Integer ageGoe) {
		return ageGoe == null ? null : member.age.goe(ageGoe);
	}
	private BooleanExpression ageLoe(Integer ageLoe) {
		return ageLoe == null ? null : member.age.loe(ageLoe);
	}

}

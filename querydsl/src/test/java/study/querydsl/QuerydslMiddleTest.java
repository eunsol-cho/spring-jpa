package study.querydsl;

import static study.querydsl.entity.QMember.*;

import java.util.List;

import javax.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
public class QuerydslMiddleTest {

	@Autowired
	EntityManager em;

	JPAQueryFactory queryFactory;

	@BeforeEach
	public void init() {

		queryFactory = new JPAQueryFactory(em);

		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		em.persist(teamA);
		em.persist(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);
		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);
		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);

	}

	/**
	 * projection - select대상 지정
	 * - 하나
	 * - 하나이상 : DTO, Tuple
	 */

	// 하나
	@Test
	public void simpleProjection() {

		List<String> result1 = queryFactory
				.select(member.username)
				.from(member)
				.fetch();

		List<Member> result2 = queryFactory
				.select(member)
				.from(member)
				.fetch();

	}

	// 하나 이상
	@Test
	public void TupleProjection() {

		List<Tuple> result = queryFactory
				.select(member.username, member.age)
				.from(member)
				.fetch();

		for (Tuple tuple : result) {
			String username = tuple.get(member.username);
			int age = tuple.get(member.age);
			System.out.println("username = " + username);
			System.out.println("age = " + age);
		}

		// 패키지 : com.querydsl.core.Tuple
		// repository 계층까지만 쓰는게 좋다.
		// service, controller까지 의존하게 하는것은 좋은 기술이 아님
		// 예를 들어 jpa를 나중에 안쓰게 되면 해당 의존성이 좋지 않음
		// 즉, DTO를 쓰는 것을 권장
	}


	// DTO로 조회 **
	@Test
	public void findDtoByJPQL() {

		// new operation 활용
		List<MemberDto> result = em.createQuery(
						"select new study.querydsl.dto.MemberDto(m.username, m.age) " +
						"from Member m", MemberDto.class)
				.getResultList();

	}

	@Test
	public void findDtoBySetter() {

		// 기본생성자 필요. setter필요
		List<MemberDto> result = queryFactory
				.select(Projections.bean(MemberDto.class,
						// 필드명 매칭이 안되는 경우 alias를 넣는다
						member.username.as("name"),
						member.age))
				.from(member)
				.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}

	}

	@Test
	public void findDtoByFields() {

		// setter가 없어도 됨. 필드에 직접넣어
		List<MemberDto> result = queryFactory
				.select(Projections.fields(MemberDto.class,
						// 필드명 매칭이 안되는 경우 alias를 넣는다
						member.username.as("name"),
						member.age))
				.from(member)
				.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}

	}

	@Test
	public void findDtoByConstructor() {

		// 생성자방식
		List<MemberDto> result = queryFactory
				.select(Projections.constructor(MemberDto.class,
						// 생성자 방식은 필드명이 달라도, 타입만 같으면 들어감
						member.username,
						member.age))
				.from(member)
				.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}

	}

	@Test
	public void findUserDto() {

		QMember memberSub = new QMember("memberSub");

		List<UserDto> fetch = queryFactory
				.select(Projections.fields(UserDto.class,
								member.username.as("name"),
								// as도 이렇게 쓸수 있지만 위에 방식이 더 심플
								// ExpressionUtils.as(member.username, "name"),

								// sub쿼리
								ExpressionUtils.as(
										JPAExpressions
												.select(memberSub.age.max())
												.from(memberSub), "age")
						)
				).from(member)
				.fetch();

	}

	@Test
	public void findDtoByQueryProjection() {

		// dto 생성자에 @QueryProjection 달고, compileQuerydsl 하면,
		// Q 클래스를 만들어서 다름과 같이 만들어줌

		// constructor방식은 runtime에러가 나지만 이 방식은 컴파일 에러가 나서 good~

		List<MemberDto> result = queryFactory
				.select(new QMemberDto(member.username, member.age))
				.from(member)
				.fetch();

		// 다만, QueryProjection을 다는거 자체가 querydsl의 의존도를 가지는 단점이 있다.
		// dto같은 경우는 service, controller 레이어 까지 왓다갓다 하는데,
		// querydsl에 의존한다는게 좀 아쉽다.

	}

	/**
	 * 동적 쿼리
	 */

	@Test
	public void 동적쿼리_BooleanBuilder() throws Exception {
		String usernameParam = "member1";
		Integer ageParam = 10;

		List<Member> result = searchMember1(usernameParam, ageParam);

		Assertions.assertThat(result.size()).isEqualTo(1);
	}

	private List<Member> searchMember1(String usernameCond, Integer ageCond) {
		BooleanBuilder builder = new BooleanBuilder();
		// 이런식으로 초기값 세팅도 가능
		//BooleanBuilder builder = new BooleanBuilder(member.username.eq(usernameCond));

		if (usernameCond != null) {
			builder.and(member.username.eq(usernameCond));
		}

		if (ageCond != null) {
			builder.and(member.age.eq(ageCond));
		}

		return queryFactory
				.selectFrom(member)
				.where(builder)
				// 이어서 작성도 가능
				// .where(builder.and(...))
				.fetch();
	}

	@Test
	public void 동적쿼리_WhereParam() throws Exception {
		String usernameParam = "member1";
		Integer ageParam = 10;

		List<Member> result = searchMember2(usernameParam, ageParam);

		Assertions.assertThat(result.size()).isEqualTo(1);
	}

	private List<Member> searchMember2(String usernameCond, Integer ageCond) {
		return queryFactory
				.selectFrom(member)
				//.where(usernameEq(usernameCond), ageEq(ageCond))
				// 이렇게 조립해서 사용할수 있다 good 		*단, null처리 주의!
				.where(allEq(usernameCond, ageCond))
				.fetch();
	}

	private BooleanExpression usernameEq(String usernameCond) {
		// where조건에서 null은 무시됨
		return usernameCond != null ? member.username.eq(usernameCond) : null;
	}

	private BooleanExpression ageEq(Integer ageCond) {
		return ageCond != null ? member.age.eq(ageCond) : null;
	}

	private BooleanExpression allEq(String usernameCond, Integer ageCond) {
		return usernameEq(usernameCond).and(ageEq(ageCond));
	}

	/**
	 * 수정, 벌크연산
	 */

	@Test
	//@Commit
	public void bulkUpdate() {

		long count = queryFactory
				.update(member)
				.set(member.username, "비회원")
				.where(member.age.lt(28))
				.execute();

		// 영속성 컨텍스트에는 "비회원" 으로 바뀌지 않고
		// 이전값으로 남아 있음
		// DB에 바로 업데이트 하기 때문에

		List<Member> result = queryFactory
				.selectFrom(member)
				.where(member.age.lt(28))
				.fetch();

		// 영속성컨텍스트 값이 나옴 -> 변경전 값이 나옴
		String username = result.get(0).getUsername();
		System.out.println("username = " + username); // member1


		// 그러므로 벌크연산 후 영속성컨텍스트 초기화를 하는게 좋다!
		em.flush();
		em.clear();


		List<Member> result2 = queryFactory
				.selectFrom(member)
				.where(member.age.lt(28))
				.fetch();


		// 변경된 값이 잘나옴
		String username2 = result2.get(0).getUsername();
		System.out.println("username2 = " + username2); // 비회원

	}

	@Test
	public void bulkAdd() {

		long count = queryFactory
				.update(member)
				.set(member.age, member.age.add(1))
				.execute();

		// 곱하기 multiply(x)
		// 빼기는 없음. add(-x) 하면됨

	}

	@Test
	public void bulkDelete() {

		long count = queryFactory
				.delete(member)
				.where(member.age.gt(18))
				.execute();

	}

	/**
	 * SQL function 호출하기
	 * - SQLDialect에 등록된 function만 사용가능
	 * - 또는 상속받아서 정의해서 사용
	 */

	@Test
	public void sqlFunction() {

		String result = queryFactory
				.select(
						Expressions.stringTemplate(
								"function('replace', {0}, {1}, {2})"
								, member.username
								, "member"
								, "M"
						)
				).from(member)
				.fetchFirst();

		System.out.println("result = " + result);

	}

	@Test
	public void sqlFunction2() {

		List<String> result = queryFactory
				.select(member.username)
				.from(member)
				.where(member.username.eq(
								Expressions.stringTemplate(
										"function('lower', {0})"
										, member.username)
						)
				)
				// querydsl에 내장된 함수가 있음. 이게더 간단
				// .where(member.username.eq(member.username.lower()))
				.fetch();

		System.out.println("result.get(0) = " + result.get(0));
	}

}

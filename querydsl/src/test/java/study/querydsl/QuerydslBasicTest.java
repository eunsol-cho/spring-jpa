package study.querydsl;

import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

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

	@Test
	public void startJPQL() {
		// member1을 찾아라.
		String qlString = "select m from Member m " +
						  // 런타임에러
						  "where m.username = :username";
		Member findMember = em.createQuery(qlString, Member.class)
				.setParameter("username", "member1")
				.getSingleResult();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	@Test
	public void startQuerydsl() {
		// 전역으로 빼서 사용 가능
		// 동시성? 여러 스레드에서 접근? 어느 트랜젝션인지에 따라서 알아서 분배하여 멀티쓰레드에서도 안전함
		// JPAQueryFactory queryFactory = new JPAQueryFactory(em);

		// Q type을 만들어서, 자바소스로 쿼리 짤수 있음
		// 컴파일에러 처리 및 코드제너레이터 사용 이점이 있다.
		QMember m = new QMember("m"); // 어떤 QMember인지 구분하는 이름, 별로중요하지 않음. (JPQL의 alias로 사용됨)
		// QMember.member; 기본타입으로 대체 가능 (static import시 member로 깔끔하게 사용가능)

		Member findMember = queryFactory
				.select(m)
				.from(m)
				// prepareStatement의 쿼리 바인딩 사용 => sql injection을 방지
				// 컴파일에러
				.where(m.username.eq("member1"))
				.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	@Test
	public void search() {
		Member findMember = queryFactory
				.selectFrom(member)
				.where(member.username.eq("member1")
						.and(member.age.eq(10)))
				.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");
		/*
		member.username.eq("member1") // username = 'member1'
		member.username.ne("member1") //username != 'member1'
		member.username.eq("member1").not() // username != 'member1'
		member.username.isNotNull() //이름이 is not null

		member.age.in(10, 20) // age in (10,20)
		member.age.notIn(10, 20) // age not in (10, 20)
		member.age.between(10,30) //between 10, 30

		member.age.goe(30) // age >= 30
		member.age.gt(30) // age > 30
		member.age.loe(30) // age <= 30
		member.age.lt(30) // age < 30

		member.username.like("member%") //like 검색
		member.username.contains("member") // like ‘%member%’ 검색
		member.username.startsWith("member") //like ‘member%’ 검색
		* */
	}

	@Test
	public void searchAndParam() {
		Member findMember = queryFactory
				.selectFrom(member)
				.where(
						// 1. and 로 연결
						//member.username.eq("member1").and(member.age.eq(10))
						// 2. , 로 연결 *선호   + , null이 있으면, 무시함
						member.username.eq("member1"), member.age.eq(10)
				)
				.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	@Test
	public void resultFetchTest() {

		// List
		List<Member> fetch = queryFactory
				.selectFrom(member)
				.fetch();

		// 단건
		Member findMember1 = queryFactory
				.selectFrom(member)
				.fetchOne(); // 단건아니면 익셉션발생

		// 처음 한 건 조회
		Member findMember2 = queryFactory
				.selectFrom(member)
				.fetchFirst(); // limit(1).fetchOne() 와 동일하다.

		// 페이징에서 사용
		QueryResults<Member> results = queryFactory
				.selectFrom(member)
				.fetchResults(); // 쿼리가 두번 실행됨. => 카운트 정보가져오기 위해서. getTotal안해도 무조건 두번나감
		// 복잡한 쿼리인 경우 최적화를 위해 totalcount쿼리랑, content 가져오는 쿼리가 다르게 나가기도 한다.
		// 위험하다. 따로 카운트 쿼리 날리는게 좋다.

		results.getTotal();
		List<Member> content = results.getResults();

		// count 쿼리로 변경
		long count = queryFactory
				.selectFrom(member)
				.fetchCount();

	}

	/*****************
	 * 정렬
	 *****************/

	/**
	 * 회원 정렬 순서
	 * 1. 회원 나이 내림차순(desc)
	 * 2. 회원 이름 올림차순(asc)
	 * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
	 */
	@Test
	public void sort() {

		em.persist(new Member(null, 100));
		em.persist(new Member("member5", 100));
		em.persist(new Member("member6", 100));

		List<Member> result = queryFactory
				.selectFrom(member)
				.where(member.age.eq(100))
				.orderBy(member.age.desc(), member.username.asc().nullsLast())
				.fetch();

		Member member5 = result.get(0);
		Member member6 = result.get(1);
		Member memberNull = result.get(2);

		assertThat(member5.getUsername()).isEqualTo("member5");
		assertThat(member6.getUsername()).isEqualTo("member6");
		assertThat(memberNull.getUsername()).isNull();

	}

	/*****************
	 * 페이징
	 *****************/

	@Test
	public void paging1() {

		List<Member> result = queryFactory
				.selectFrom(member)
				.orderBy(member.username.desc())
				.offset(1) //0부터 시작(zero index)
				.limit(2) //최대 2건 조회
				.fetch();

		assertThat(result.size()).isEqualTo(2);

	}

	@Test
	public void paging2() {

		QueryResults<Member> queryResults = queryFactory
				.selectFrom(member)
				.orderBy(member.username.desc())
				.offset(1)
				.limit(2)
				.fetchResults();

		assertThat(queryResults.getTotal()).isEqualTo(4); // 전체조회수
		assertThat(queryResults.getLimit()).isEqualTo(2);
		assertThat(queryResults.getOffset()).isEqualTo(1);
		assertThat(queryResults.getResults().size()).isEqualTo(2);

	}

	/*****************
	 * 집합
	 *****************/

	/**
	 * JPQL
	 * select
	 * COUNT(m), //회원수
	 * SUM(m.age), //나이 합
	 * AVG(m.age), //평균 나이
	 * MAX(m.age), //최대 나이
	 * MIN(m.age) //최소 나이
	 * from Member m
	 */
	@Test
	public void aggregation() throws Exception {

		List<Tuple> result = queryFactory
				.select(member.count(),
						member.age.sum(),
						member.age.avg(),
						member.age.max(),
						member.age.min())
				.from(member)
				.fetch();
		Tuple tuple = result.get(0);

		assertThat(tuple.get(member.count())).isEqualTo(4);
		assertThat(tuple.get(member.age.sum())).isEqualTo(100);
		assertThat(tuple.get(member.age.avg())).isEqualTo(25);
		assertThat(tuple.get(member.age.max())).isEqualTo(40);
		assertThat(tuple.get(member.age.min())).isEqualTo(10);

	}

	/**
	 * 팀의 이름과 각 팀의 평균 연령을 구해라.
	 */
	@Test
	public void group() throws Exception {

		List<Tuple> result = queryFactory
				.select(team.name, member.age.avg())
				.from(member)
				.join(member.team, team)
				.groupBy(team.name)
				// having ...
				.fetch();

		Tuple teamA = result.get(0);
		Tuple teamB = result.get(1);

		assertThat(teamA.get(team.name)).isEqualTo("teamA");
		assertThat(teamA.get(member.age.avg())).isEqualTo(15);
		assertThat(teamB.get(team.name)).isEqualTo("teamB");
		assertThat(teamB.get(member.age.avg())).isEqualTo(35);

	}

	/*****************
	 * 조인
	 *****************/

	/**
	 * 팀 A에 소속된 모든 회원
	 */
	@Test
	public void join() throws Exception {

		QMember member = QMember.member;
		QTeam team = QTeam.team;

		List<Member> result = queryFactory
				.selectFrom(member)
				.join(member.team, team)
				.where(team.name.eq("teamA"))
				.fetch();

		assertThat(result)
				.extracting("username")
				.containsExactly("member1", "member2");

	}

	/**
	 * 세타 조인(연관관계가 없는 필드로 조인)
	 * 회원의 이름이 팀 이름과 같은 회원 조회
	 */
	@Test
	public void theta_join() throws Exception {

		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));

		List<Member> result = queryFactory
				.select(member)
				.from(member, team)
				.where(member.username.eq(team.name))
				.fetch();

		assertThat(result)
				.extracting("username")
				.containsExactly("teamA", "teamB");

	}

	/**
	 * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
	 * JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'teamA'
	 * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and
	 t.name='teamA'
	 */
	@Test
	public void join_on_filtering() throws Exception {

		List<Tuple> result = queryFactory
				.select(member, team)
				.from(member)
				.leftJoin(member.team, team).on(team.name.eq("teamA"))
				.fetch();

		for (Tuple tuple : result) {
			System.out.println("tuple = " + tuple);
		}

	}

	/**
	 * 2. 연관관계 없는 엔티티 외부 조인
	 * 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
	 * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
	 * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name
	 */
	@Test
	public void join_on_no_relation() throws Exception {

		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));

		List<Tuple> result = queryFactory
				.select(member, team)
				.from(member)
				.leftJoin(team).on(member.username.eq(team.name))
				.fetch();

		for (Tuple tuple : result) {
			System.out.println("t=" + tuple);
		}

	}

	@PersistenceUnit
	EntityManagerFactory emf;

	@Test
	public void fetchJoinNo() throws Exception {
		em.flush();
		em.clear();

		Member findMember = queryFactory
				.selectFrom(member)
				.where(member.username.eq("member1"))
				.fetchOne();

		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		assertThat(loaded).as("페치 조인 미적용").isFalse();
	}

	@Test
	public void fetchJoinUse() throws Exception {
		em.flush();
		em.clear();

		Member findMember = queryFactory
				.selectFrom(member)
				.join(member.team, team).fetchJoin()
				.where(member.username.eq("member1"))
				.fetchOne();

		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		assertThat(loaded).as("페치 조인 적용").isTrue();
	}

	/*****************
	 * 서브쿼리
	 *****************/

	/**
	 * 나이가 가장 많은 회원 조회
	 */
	@Test
	public void subQuery() throws Exception {

		QMember memberSub = new QMember("memberSub");

		List<Member> result = queryFactory
				.selectFrom(member)
				.where(member.age.eq(
						select(memberSub.age.max())
								.from(memberSub)
				))
				.fetch();

		assertThat(result).extracting("age")
				.containsExactly(40);
	}

	/**
	 * 나이가 평균 나이 이상인 회원
	 */
	@Test
	public void subQueryGoe() throws Exception {

		QMember subMember = new QMember("sub");

		List<Member> result = queryFactory
				.selectFrom(member)
				.where(member.age.goe(
						select(subMember.age.max())
								.from(subMember)
				))
				.fetch();

		assertThat(result).extracting("age")
				.containsExactly(30,40);
	}

	/**
	 * 서브쿼리 여러 건 처리, in 사용
	 */

	/**
	 * select 절에 subquery
	 */


	/*****************
	 * Case문
	 *****************/

	@Test
	public void simpleCase() throws Exception {

		List<String> result = queryFactory
				.select(member.age
						.when(10).then("열살")
						.when(20).then("스무살")
						.otherwise("기타"))
				.from(member)
				.fetch();

	}

	@Test
	public void complexCase() throws Exception {

		List<String> result = queryFactory
				.select(new CaseBuilder()
						.when(member.age.between(0, 20)).then("0~20살")
						.when(member.age.between(21, 30)).then("21~30살")
						.otherwise("기타"))
				.from(member)
				.fetch();

	}

	/*****************
	 * 상수, 문자 더하기
	 *****************/

	@Test
	public void constant() throws Exception {

		Tuple result = queryFactory
				.select(member.username, Expressions.constant("A"))
				.from(member)
				.fetchFirst();

	}

	@Test
	public void concat() throws Exception {

		String result = queryFactory
				.select(member.username.concat("_").concat(member.age.stringValue()))
				.from(member)
				.where(member.username.eq("member1"))
				.fetchOne();

	}


}

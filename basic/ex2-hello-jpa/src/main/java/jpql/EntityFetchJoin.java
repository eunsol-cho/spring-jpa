package jpql;

import jpql.entity.Member;
import jpql.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

public class EntityFetchJoin {
    public static void main(String[] args) {
        // 애플리케이션 로딩시점에 단 하나만 만들어 두기!
        EntityManagerFactory enf = Persistence.createEntityManagerFactory("hello");

        // 트랜잭션 단위 마다 하나씩 생성 (DBconnection이라고 생각)
        EntityManager em = enf.createEntityManager();

        // 명시적으로 트랜잭션을 생성해준다.
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 트랜잭션코드는 정석적으로 try-catch로 commit/rollback을 제어 한다.
        try {

            Team teamA = new Team();
            teamA.setName("팀A");
            em.persist(teamA);

            Team teamB = new Team();
            teamB.setName("팀B");
            em.persist(teamB);

            Member member1 = new Member();
            member1.setName("회원1");
            member1.setTeam(teamA);
            em.persist(member1);

            Member member2 = new Member();
            member2.setName("회원2");
            member2.setTeam(teamA);
            em.persist(member2);

            Member member3 = new Member();
            member3.setName("회원3");
            member3.setTeam(teamB);
            em.persist(member3);

            em.flush();
            em.clear();

            // 엔티티 페치 조인
            String query = "select m from Member m join fetch m.team";
            List<Member> members = em.createQuery(query, Member.class)
                            .getResultList();

            for (Member member : members) {
                System.out.println("member.toString() = " + member.toString());
                System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
            }
            
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            // entityManager는 내부적으로 DBconnection을 물고 있다. 반드시 자원 해제 필요!
            em.close();
        }

        enf.close();
    }
}

package jpql;

import jpql.entity.Member;
import jpql.entity.MemberDTO;
import jpql.entity.Team;

import javax.persistence.*;
import java.util.List;

public class Paging {
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

            for (int i = 0; i < 100; i++) {
                Member member = new Member();
                member.setName("member" + i);
                member.setAge(i);
                em.persist(member);
            }

            em.flush();
            em.clear();

            List<Member> result = em.createQuery("select m from Member m order by m.age desc", Member.class)
                    .setFirstResult(1)
                    .setMaxResults(10)
                    .getResultList();

            System.out.println("result.size() = " + result.size());
            for (Member member1 : result) {
                System.out.println("member1.toString() = " + member1.toString());
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

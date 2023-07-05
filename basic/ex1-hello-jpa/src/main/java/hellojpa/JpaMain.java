package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

public class JpaMain {
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
            // 1. 등록
            Member member = new Member();
            member.setId(1L);
            member.setName("HelloA");

            em.persist(member);

            tx.commit();

            // 2. 조회
            /*Member findMember = em.find(Member.class, 1L);

            System.out.println("findMember.id = " + findMember.getId());
            System.out.println("findMember.name = " + findMember.getName());

            tx.commit();*/

            // 3. 삭제
            /*Member findMember = em.find(Member.class, 1L);
            em.remove(findMember);

            tx.commit();*/

            // 4. 수정
            Member findMember = em.find(Member.class, 1L);
            findMember.setName("HelloJPA");

            /*tx.commit();*/

            // JPQL
            //List<Member> result = em.createQuery("select m from Member as m", Member.class)
                    //.setFirstResult(5)
                    //.setMaxResults(8)
                    //.getResultList();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            // entityManager는 내부적으로 DBconnection을 물고 있다. 반드시 자원 해제 필요!
            em.close();
        }

        enf.close();
    }
}

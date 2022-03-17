package jpql;

import jpql.entity.Member;
import jpql.entity.MemberDTO;
import jpql.entity.Team;

import javax.persistence.*;
import java.util.List;

public class JpqlMain {
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

            Member member = new Member();
            member.setName("member1");
            member.setAge(10);
            em.persist(member);

            em.flush();
            em.clear();

            // 반환타입
            TypedQuery<Member> typeQuery = em.createQuery("select m from Member m", Member.class);
            Query query = em.createQuery("select m.name, m.age from Member m");

            // 파라미터 바인딩
            Member singleResult = em.createQuery("select m from Member m where m.name =:name", Member.class)
                                    .setParameter("name", "member1")
                                    .getSingleResult();
            System.out.println("singleResult = " + singleResult.getName());

            // 엔티티 프로젝션
            // 영속성
            List<Member> result = em.createQuery("select m from Member m", Member.class)
                    .getResultList();

            Member findMember = result.get(0);
            findMember.setAge(20);

            // 조인
            List<Team> teams = em.createQuery("select m.team from Member m", Team.class).getResultList();

            // 스칼라 타입 프로젝션
            // Object[] 타입으로 조회
            List<Object[]> resultList = em.createQuery("select m.name, m.age from Member m")
                            .getResultList();

            Object[] r = resultList.get(0);
            System.out.println("r = " + r[0]);
            System.out.println("r = " + r[1]);

            // new 명령어로 조회
            List<MemberDTO> members = em.createQuery("select new jpql.MemberDTO(m.name, m.age) from Member m", MemberDTO.class)
                    .getResultList();

            MemberDTO memberDTO = members.get(0);
            System.out.println("memberDTO.getName() = " + memberDTO.getName());
            System.out.println("memberDTO.getAge() = " + memberDTO.getAge());


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

package hellojpa.ch08;

import hellojpa.entity.Member;
import hellojpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class ProxyInheritance2 {
    public static void main(String[] args) {
        EntityManagerFactory enf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = enf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Member member1 = new Member();
            member1.setName("member1");

            Member member2 = new Member();
            member2.setName("member2");

            em.persist(member1);
            em.persist(member2);

            em.flush();
            em.clear();

            // case1. find -> getReference
            //Member m1 = em.find(Member.class, member1.getId());
            //Member m2 = em.getReference(Member.class, member1.getId());
            // 둘다 진짜 객체
            // m1 == m2 -> true

            // case2. getReference -> getReference
            //Member m1 = em.getReference(Member.class, member1.getId());
            //Member m2 = em.getReference(Member.class, member1.getId());
            // 둘다 프록시 객체
            // m1 == m2 -> true

            // case3. getReference -> find
            Member m1 = em.getReference(Member.class, member1.getId());
            Member m2 = em.find(Member.class, member1.getId());
            // 둘다 프록시 객체*
            // m1 == m2 -> true

            // true
            System.out.println("m1 == m2 : " + (m1.getClass() == m2.getClass()));
            // true
            System.out.println("m1 == m2 : " + (m1 instanceof Member));
            System.out.println("m1 == m2 : " + (m2 instanceof Member));

            tx.commit();

        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        enf.close();
    }

    private static void printMember(Member member) {
        System.out.println("username = " + member.getName());
    }

    private static void printMemberAndTeam(Member member) {
        String username = member.getName();
        System.out.println("username = " + username);

        Team team = member.getTeam();
        System.out.println("team = " + team.getName());
    }
}

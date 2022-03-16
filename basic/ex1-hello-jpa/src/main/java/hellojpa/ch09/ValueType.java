package hellojpa.ch09;

import hellojpa.entity.Address;
import hellojpa.entity.Member;
import hellojpa.entity.Team;
import org.hibernate.Hibernate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class ValueType {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            Address address = new Address("city", "street", "10000");

            Member member1 = new Member();
            member1.setName("member1");
            member1.setHomeAddress(address);
            em.persist(member1);

            Member member2 = new Member();
            member2.setName("member2");
            member2.setHomeAddress(address);
            em.persist(member2);

            member1.getHomeAddress().setCity("newCity");

            tx.commit();

        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }

        emf.close();
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

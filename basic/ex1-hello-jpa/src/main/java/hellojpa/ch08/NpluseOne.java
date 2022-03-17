package hellojpa.ch08;

import hellojpa.entity.Child;
import hellojpa.entity.Member;
import hellojpa.entity.Parent;
import hellojpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.List;

public class NpluseOne {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            Team team1 = new Team();
            team1.setName("A");
            Team team2 = new Team();
            team2.setName("B");

            Member member1 = new Member();
            member1.setName("memberA");
            member1.setTeam(team1);

            Member member2 = new Member();
            member2.setName("member2");
            member2.setTeam(team2);

            Member member3 = new Member();
            member3.setName("member3");
            member3.setTeam(team2);

            em.persist(team1);
            em.persist(team2);
            em.persist(member1);
            em.persist(member2);
            em.persist(member3);

            em.flush();
            em.clear();

            System.out.println("========================");
            //List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();
            //List<Team> teams = em.createQuery("select t from Team t", Team.class).getResultList();
            //Member member = em.find(Member.class, member1.getId());
            //Team t = em.find(Team.class, team2.getId());
            List<Team> teams = em.createQuery("select t from Team t where t.name = 'B'", Team.class).getResultList();

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

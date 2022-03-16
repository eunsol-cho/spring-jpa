package hellojpa.ch09;

import hellojpa.entity.Address;
import hellojpa.entity.Member;
import hellojpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class ValueTypeCollection {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            // 등록
            Member member = new Member();
            member.setName("member1");
            member.setHomeAddress(new Address("city", "street", "10000"));

            member.getFavoriteFoods().add("피자");
            member.getFavoriteFoods().add("치킨");
            member.getFavoriteFoods().add("라면");

            member.getAddressHistory().add(new Address("city1", "street1", "10001"));
            member.getAddressHistory().add(new Address("city2", "street2", "10002"));

            em.persist(member);

            em.flush();
            em.clear();

            // 조회
            System.out.println("================ START ==============");
            Member findMember = em.find(Member.class, member.getId());

            System.out.println("===== getFood : ");
            for (String food : findMember.getFavoriteFoods()) {
                System.out.println(food);
            }

            // 수정

            // 값타입
            //findMember.getHomeAddress().setCity("newCity"); X
            findMember.setHomeAddress(new Address("newCity", "street", "10000"));

            // 값타입 컬렉션
            findMember.getFavoriteFoods().remove("치킨");
            findMember.getFavoriteFoods().add("한식");

            // equals 재정의가 잘되어 있어야함
            findMember.getAddressHistory().remove(new Address("city1", "street1", "10001"));
            findMember.getAddressHistory().add(new Address("newCity", "street1", "10001"));

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

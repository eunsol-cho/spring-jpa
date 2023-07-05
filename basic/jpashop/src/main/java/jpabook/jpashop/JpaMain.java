package jpabook.jpashop;

import jpabook.jpashop.domain.Book;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory enf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = enf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            /*Member m = new Member();
            m.setName("은솔");

            em.persist(m);

            System.out.println("m.getName() = " + m.getName());
            
            em.flush();
            em.clear();*/

            //System.out.println("m.getId() = " + m.getId());
            //System.out.println("m.getName() = " + m.getName());
            Member findMember = em.find(Member.class, 1L);
            findMember.setName("은솔4");

            System.out.println("m.getName() = " + findMember.getName());

            em.flush();

            Thread.sleep(5000000);

            System.out.println("m.getName() = " + findMember.getName());

            //tx.commit();


        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        enf.close();
    }
}

package devyu.querydsl.entity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
//@Commit
class MemberTest {

    @PersistenceContext
    EntityManager entityManager;

    @Test
    public void testEntity() {
        Team 영업팀 = new Team("영업팀");
        Team 개발팀 = new Team("개발팀");

        entityManager.persist(영업팀);
        entityManager.persist(개발팀);

        Member member1 = new Member("A", 10, 영업팀);
        Member member2 = new Member("B", 20, 영업팀);
        Member member3 = new Member("C", 30, 개발팀);
        Member member4 = new Member("D", 40, 개발팀);

        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(member3);
        entityManager.persist(member4);

        // 초기화
        entityManager.flush(); // 영속성 컨텍스트에 있는 쿼리를 DB에 날림
        entityManager.clear(); // 영속성 컨텍스트를 실제로 비워 캐시 초기화

        // 확인 JPQL
        List<Member> members = entityManager.createQuery("select m from Member m", Member.class)
                .getResultList();

        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("-> member.team=" + member.getTeam()); // FetchType.LAZY 로 인하여 실제 데이터를 사용하고자 할때 select query가 동작
        }

    }

}
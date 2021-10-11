package devyu.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import devyu.querydsl.entity.Member;
import devyu.querydsl.entity.QMember;
import devyu.querydsl.entity.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@SpringBootTest
@Transactional
public class querydelBasicTest {

    @Autowired
    EntityManager entityManager;

    JPAQueryFactory jpaQueryFactory;

    @BeforeEach
    public void before() {

        jpaQueryFactory = new JPAQueryFactory(entityManager);

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
    }

    @Test
    public void startJPQL() {
        Member findMember = entityManager.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "A")
                .getSingleResult();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("A");

    }

    @Test
    public void startQuerydsl() {
//        QMember m = new QMember("m");
        QMember m = QMember.member;

        Member findMember = jpaQueryFactory.select(m)
                .from(m)
                .where(m.username.eq("A"))
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("A");
    }

    @Test
    public void search() {
        QMember m = QMember.member;
        Member member = jpaQueryFactory
                .selectFrom(m)
                .where(m.username.eq("A").and(m.age.eq(10)))
                .fetchOne();

        Assertions.assertThat(member.getUsername()).isEqualTo("A");
    }

    @Test
    public void searchAndParam() {
        QMember m = QMember.member;
        Member member = jpaQueryFactory
                .selectFrom(m)
                .where(
                        m.username.eq("A"), // and로 인식함
                        m.age.eq(10)
                )
                .fetchOne();

        Assertions.assertThat(member.getUsername()).isEqualTo("A");
    }

    @Test
    public void resultFetch() {
        QMember m = QMember.member;

        // 리스트 조회
        List<Member> members = jpaQueryFactory
                .selectFrom(m)
                .fetch();

        // 단건 조회
        Member member1 = jpaQueryFactory
                .selectFrom(m)
                .fetchOne();

        // 단건 조회
        Member member2 = jpaQueryFactory
                .selectFrom(m)
                .fetchFirst(); // limit 1 + fetchOne

        QueryResults<Member> memberQueryResults = jpaQueryFactory
                .selectFrom(m)
                .fetchResults();

        memberQueryResults.getTotal();
        List<Member> results = memberQueryResults.getResults();

    }

}

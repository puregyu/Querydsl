package devyu.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import devyu.querydsl.entity.Member;
import devyu.querydsl.entity.QMember;
import devyu.querydsl.entity.QTeam;
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

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {
        QMember m = QMember.member;

        entityManager.persist(new Member(null, 100));
        entityManager.persist(new Member("E", 100));
        entityManager.persist(new Member("F", 100));

        List<Member> result = jpaQueryFactory
                .selectFrom(m)
                .where(m.age.eq(100))
                .orderBy(m.age.desc(), m.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);
        Assertions.assertThat(member5.getUsername()).isEqualTo("E");
        Assertions.assertThat(member6.getUsername()).isEqualTo("F");
        Assertions.assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1() {
        QMember m = QMember.member;

        List<Member> result = jpaQueryFactory
                .selectFrom(m)
                .orderBy(m.username.desc())
                .offset(1) //0부터 시작(zero index)
                .limit(2) //최대 2건 조회
                .fetch();
        Assertions.assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void paging2() {
        QMember m = QMember.member;

        QueryResults<Member> memberQueryResults = jpaQueryFactory
                .selectFrom(m)
                .orderBy(m.username.desc())
                .offset(1) //0부터 시작(zero index)
                .limit(2) //최대 2건 조회
                .fetchResults(); // count쿼리가 먼저 나가고 select 쿼리가 한번 더 나간다.
        // where 절이 추가될 경우.. count쿼리 / select쿼리 둘 다 where절이 붙기때문에 성능상 이슈가 있을 수 있다.
        // 가벼운 페이징 처리에서만 사용하고 복잡한 쿼리의 경우 count쿼리를 별도로 만드는것을 추천함

        Assertions.assertThat(memberQueryResults.getTotal()).isEqualTo(4);
        Assertions.assertThat(memberQueryResults.getLimit()).isEqualTo(2);
        Assertions.assertThat(memberQueryResults.getOffset()).isEqualTo(1);
        Assertions.assertThat(memberQueryResults.getResults().size()).isEqualTo(2);
    }

    @Test
    public void aggregation() throws Exception {
        QMember m = QMember.member;

        List<Tuple> result = jpaQueryFactory
                .select(m.count(),
                        m.age.sum(),
                        m.age.avg(),
                        m.age.max(),
                        m.age.min())
                .from(m)
                .fetch();

        Tuple tuple = result.get(0);

        Assertions.assertThat(tuple.get(m.count())).isEqualTo(4);
        Assertions.assertThat(tuple.get(m.age.sum())).isEqualTo(100);
        Assertions.assertThat(tuple.get(m.age.avg())).isEqualTo(25);
        Assertions.assertThat(tuple.get(m.age.max())).isEqualTo(40);
        Assertions.assertThat(tuple.get(m.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    public void group() throws Exception {
        QMember m = QMember.member;
        QTeam t = QTeam.team;

        List<Tuple> result = jpaQueryFactory
                .select(t.name, m.age.avg())
                .from(m)
                .join(m.team, t)
                .groupBy(t.name)
                .fetch();
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        Assertions.assertThat(teamA.get(t.name)).isEqualTo("teamA");
        Assertions.assertThat(teamA.get(m.age.avg())).isEqualTo(15);
        Assertions.assertThat(teamB.get(t.name)).isEqualTo("teamB");
        Assertions.assertThat(teamB.get(m.age.avg())).isEqualTo(35);
    }

    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    public void join() throws Exception {
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        Assertions.assertThat(result)
                .extracting("username")
                .containsExactly("A", "B");
    }

    /**
     * 세타 조인(연관관계가 없는 필드로 조인)
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join() throws Exception {
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        entityManager.persist(new Member("teamA"));
        entityManager.persist(new Member("teamB"));
        List<Member> result = jpaQueryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();
        Assertions.assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }
}

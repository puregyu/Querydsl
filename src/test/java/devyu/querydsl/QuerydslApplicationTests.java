package devyu.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import devyu.querydsl.entity.Hello;
import devyu.querydsl.entity.QHello;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@SpringBootTest
@Transactional // org.springframework, TEST에 @Transactional이 있으면 수행 후 Rollback
@Commit // Rollback 안되도록..
class QuerydslApplicationTests {

//    @Autowired // 스프링
    @PersistenceContext // 자바 표준스펙
    EntityManager entityManager;

    @Test
    void contextLoads() {
        Hello hello = new Hello();
        entityManager.persist(hello);

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(entityManager);
        QHello qHello = new QHello("h");

        Hello result = jpaQueryFactory
                        .selectFrom(qHello)
                        .fetchOne();

        Assertions.assertThat(result).isEqualTo(hello);
        Assertions.assertThat(result.getId()).isEqualTo(hello.getId());

    }

}

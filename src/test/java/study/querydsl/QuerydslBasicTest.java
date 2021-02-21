package study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.jpa.impl.JPAQueryFactory;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;
    private JPAQueryFactory queryFactory;

    @BeforeEach
    void setUp() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        persistTeams(teamA, teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        persistMembers(member1, member2, member3, member4);
    }

    private void persistMembers(Member... members) {
        for (Member member : members) {
            em.persist(member);
        }
    }

    private void persistTeams(Team... teams) {
        for (Team team : teams) {
            em.persist(team);
        }
    }

    @Test
    void startJPQL() {
        String queryString = "select m from Member m where m.username = :username";

        Member findMember = em.createQuery(queryString, Member.class)
            .setParameter("username", "member1")
            .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void startQuerydsl() {
        QMember m = new QMember("m");

        Member findMember = queryFactory
            .select(m)
            .from(m)
            .where(m.username.eq("member1"))
            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }
}

package study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
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
//        QMember m = new QMember("m");

        Member findMember = queryFactory
            .select(member)
            .from(member)
            .where(member.username.eq("member1"))
            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void search() {
        Member findMember = queryFactory
            .selectFrom(member)
            .where(member.username.eq("member1")
                .and(member.age.between(10, 30)))
            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void searchAndParam() {
        Member findMember = queryFactory
            .selectFrom(member)
            .where(
                member.username.eq("member1"),
                member.age.eq(10)
            )
            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void resultFetch() {
        List<Member> fetch = queryFactory
            .selectFrom(member)
            .fetch();

        Member fetchOne = queryFactory
            .selectFrom(QMember.member)
            .where(member.username.eq("member1"))
            .fetchOne();

        Member fetchFi = queryFactory
            .selectFrom(QMember.member)
            .fetchFirst();

        QueryResults<Member> results = queryFactory
            .selectFrom(member)
            .fetchResults();

        results.getTotal();
        List<Member> content = results.getResults();

        System.out.println("results = " + results);
        System.out.println("results.getTotal() = " + results.getTotal());
        System.out.println("content = " + content);

        long count = queryFactory
            .selectFrom(member)
            .fetchCount();
    }

    /**
     * 1. 회원 나이 내림차순
     * 2. 회원 이름 올림차순
     * 단 2에서 회원 이름이 없으면(null) 마지막에 출력
     */
    @Test
    void sort() {
        // given
        persistMembers(
            new Member(null, 100),
            new Member("member5", 100),
            new Member("member6", 100)
        );

        // when
        List<Member> findMembers = queryFactory
            .selectFrom(member)
            .where(member.age.eq(100))
            .orderBy(member.age.desc(), member.username.asc().nullsLast())
            .fetch();

        // then
        Member member5 = findMembers.get(0);
        Member member6 = findMembers.get(1);
        Member memberNull = findMembers.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isEqualTo(null);
    }
}
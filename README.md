# Study QueryDSL

### from 절의 서브쿼리 한계
JPA JPQL 서브쿼리의 한계점으로 from 절의 서브쿼리(인라인 뷰)는 지원하지 않는다. 당연히 QueryDSL도 지원하지 않는다. 하이버네이트 구현체를 사용하면 select 절의 서브쿼리를 지원한다.


### from 절의 서브쿼리 해결방안
1. 서브쿼리를 join으로 변경한다. (가능한 상황도 있고, 불가능한 상황도 있다.)
2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
3. native SQL을 사용한다.


### Projection
- select 절을 정의

**반환 방법**
- 대상이 하나면 타입을 명확하게 저정할 수 있다.
- Tuple 조회
    - 대상이 둘 이상일 때
    - `String username = tuple.get(member.username);`
- DTO
    - QueryDSL 빈 생성
        - 프로퍼티(setter) 접근
            - `select(Projections.bean(MemberDto.class, member.username, member.age))`
        - field 직접 접근
            - `select(Projections.field(MemberDto.class, member.username, member.age))`
            - 별칭이 다를경우 alias 사용
                - `member.username.as("name")`
        - 생성자 사용
            - `select(Projections.constructor(MemberDto.class, member.username, member.age))`
    - `@QueryProjection` 활용
        - dto 생성자에 어노테이션을 붙여서 사용한다.
    
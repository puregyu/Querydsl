package devyu.querydsl.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 기본 생성자가 있어야 함
@ToString(of = {"id", "username", "age"})
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String username;

    private int age;

    /**
     * 연관관계 주인 (Member -> Team)
     * 여러 명의 맴버는 하나의 팀에 속할 수 있다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name =  "team_id") // 외래키 명
    private Team team;

    public Member(String username) {
        this(username, 0, null);
    }

    public Member(String username, int age) {
        this(username, age, null);
    }

    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if (team != null) {
            changeTeam(team);
        }
    }

    /**
     * 연관관계 설정 메소드
     *
     * @param team
     */
    public void changeTeam(Team team) {
        //  Member(연관관계의 주인)의 Team만 수정하여도 데이터 관점에서 DB FK가 업데이트 되기 때문에 이슈가 없음
        this.team = team;

        // Team(연관관계 거울)의 Member List에도 add()를 해주긴 하지만, 객체의 관점에서는 사실 remove()도 해주는 것이 맞다.
        // 하지만 remove() 등.. 로직이 너무 복잡해지기 때문에 연관관계의 주인쪽을 잘 관리하고 양쪽을 연결하고 있다는 사실로 타협을 보는 것도 괜찮다고 한다.
        team.getMembers().add(this);
    }
}

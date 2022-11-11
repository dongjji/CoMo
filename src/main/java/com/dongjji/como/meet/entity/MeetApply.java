package com.dongjji.como.meet.entity;

import com.dongjji.como.meet.type.MeetJoinStatus;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Meet_Apply")
@EntityListeners(AuditingEntityListener.class)
public class MeetApply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long meetId;
    private String email;

    @Enumerated(EnumType.STRING)
    private MeetJoinStatus status;
}

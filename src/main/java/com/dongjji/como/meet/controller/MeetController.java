package com.dongjji.como.meet.controller;

import com.dongjji.como.meet.dto.CreateMeetDto;
import com.dongjji.como.meet.dto.ExtendMeetPeriodDto;
import com.dongjji.como.meet.dto.InviteMembersDto;
import com.dongjji.como.meet.dto.MeetMembersDto;
import com.dongjji.como.meet.entity.Meet;
import com.dongjji.como.meet.entity.MeetApply;
import com.dongjji.como.meet.entity.MeetInvitation;
import com.dongjji.como.meet.service.MeetApplyService;
import com.dongjji.como.meet.service.MeetInvitationService;
import com.dongjji.como.meet.service.MeetJoinService;
import com.dongjji.como.meet.service.MeetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@Controller
@RequestMapping("/meet")
public class MeetController {
    private final MeetService meetService;
    private final MeetJoinService meetJoinService;
    private final MeetInvitationService meetInvitationService;
    private final MeetApplyService meetApplyService;

    @GetMapping
    public String getAllMeets(Pageable pageable, Model model) {
        Page<Meet> meets = meetService.getAllMeets(pageable);
        model.addAttribute("meets", meets);

        return "/meet/all";
    }

    @PostMapping
    public String createMeet(CreateMeetDto.Request createMeetDto, Model model,
                              Authentication authentication) {
        CreateMeetDto.Response meet = meetService.createMeet(createMeetDto);
        meetInvitationService.inviteMembers(InviteMembersDto.Request
                                    .builder()
                                    .meetId(meet.getId())
                                    .emailList(createMeetDto.getEmails())
                                    .build());

        model.addAttribute("meet", meet);

        return "meet/meet-detail";
    }

    @PostMapping("/invite")
    public String inviteMember(InviteMembersDto.Request inviteMembersDto) {
        meetInvitationService.inviteMembers(inviteMembersDto);

        return "meet/meet-detail";
    }

    //    @PreAuthorize()
    @DeleteMapping("/{meetId}")
    public String deActivateMeet(@PathVariable Long meetId) {
        // TODO: ???????????? ??????????????? ???????????? PreAuthorize ?????? ?????????.
        meetService.deActivateMeet(meetId);

        return "meet/meet-detail";
    }

    @GetMapping("/apply/{meetId}")
    public String getAllApplies(@PathVariable Long meetId, String email, Model model) {
        // TODO: ???????????? ????????? ???????????? email
        List<MeetApply> applies = meetApplyService.getAllApplies(meetId, email);
        model.addAttribute("applies", applies);

        return "meet/applies";
    }

    @PostMapping("/apply/{meetId}")
    public String applyMeet(@PathVariable Long meetId, String email) {
        // TODO: ?????? ?????? ???????????? email ????????????
        meetApplyService.applyMeetByMeetId(meetId, email);

        return "meet/meet-detail";
    }

    // ????????? ???????????? ????????? ??????
    @PostMapping("/apply/approve/{applyId}")
    public String approveApply(@PathVariable Long applyId) {
        Long meetId = meetApplyService.approveApply(applyId);

        return "redirect:/meet/apply/" + meetId;
    }

    // ????????? ???????????? ????????? ??????
    @PostMapping("/apply/refuse/{applyId}")
    public String refuseApply(@PathVariable Long applyId) {
        Long meetId = meetApplyService.refuseApply(applyId);

        return "redirect:/meet/apply/" + meetId;
    }

    // ???????????? ????????? ????????? ??????
    @PostMapping("/invite/approve/{invitationId}")
    public String approveInvitation(@PathVariable Long invitationId) {
        Long meetId = meetInvitationService.approveInvitation(invitationId);

        return "redirect:/meet/apply/" + meetId;
    }

    // ???????????? ????????? ????????? ??????
    @PostMapping("/invite/refuse/{invitationId}")
    public String refuseInvitation(@PathVariable Long invitationId) {
        Long meetId = meetInvitationService.refuseInvitation(invitationId);

        return "redirect:/meet/apply/" + meetId;
    }

    @GetMapping("/invites")
    public String getAllInvitations(String email, Model model) {
        // TODO: ?????? ?????? ???????????? email ????????????
        List<MeetInvitation> invitations = meetInvitationService.getAllInvitations(email);
        model.addAttribute("invitations", invitations);

        return "meet/invites";
    }

    @PutMapping("/period/extend")
    public String extendPeriod(ExtendMeetPeriodDto.Request extendMeetPeriodDto) {
        // TODO: ???????????? ?????? ????????? ???????????? PreAuthorize ?????? ?????????.
        meetService.extendPeriod(extendMeetPeriodDto);
        return "meet/meet-detail";
    }

    @GetMapping("/members/{meetId}")
    public String getMeetMembers(@PathVariable Long meetId, String email, Model model) {
        // TODO: ?????? ?????? ???????????? email ????????????
        MeetMembersDto.Response members = meetJoinService.getMeetMembers(meetId);
        model.addAttribute("members", members);

        return "meet/members";
    }

    @Scheduled(cron = "${scheduler.cron.period}")
    public void finishMeets() {
        meetService.finishMeets();
    }
}

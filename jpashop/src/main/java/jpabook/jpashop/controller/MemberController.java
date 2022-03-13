package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members/new")
    public String createForm(Model model) {
	// 빈화면임에도 빈 객체를 들고간다. -> 벨리데이션으로 사용될 수도 있음.
	model.addAttribute("memberForm", new MemberForm());
	return "members/createMemberForm";
    }

    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result) {
	// @Valid : @NotEmpty와 같은 벨리데이션 처리
	// BindingResult : valid에서 걸리더라도 소스가 실행됨
	if (result.hasErrors()) {
	    return "members/createMemberForm";
	}
	Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());
	Member member = new Member();
	member.setName(form.getName());
	member.setAddress(address);

	memberService.join(member);
	return "redirect:/"; // 저장후에 리다이렉트
    }

    @GetMapping("members")
    public String list(Model model) {
	// 이건 예제여서 Entity를 화면에 뿌린거임. 실무에서는 특히API 개발시에는 무조건 DTO별도로 만들어서 뿌려야함
	List<Member> members = memberService.findMembers();
	model.addAttribute("members", members);
	return "members/memberList";
    }
}

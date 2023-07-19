package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Data;

@Data
public class MemberDto {

	private String name;
	private int age;

	public MemberDto() {
	}

	// compileQuerydsl 하면 Q로 생성해줌
	@QueryProjection
	public MemberDto(String name, int age) {
		this.name = name;
		this.age = age;
	}

}

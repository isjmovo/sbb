package com.ll.exam.sbb.Answer;

import com.ll.exam.sbb.DataNotFoundException;
import com.ll.exam.sbb.Question.Question;
import com.ll.exam.sbb.Question.QuestionService;
import com.ll.exam.sbb.user.SiteUser;
import com.ll.exam.sbb.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/answer")
public class AnswerController {
  private final QuestionService questionService;
  private final AnswerService answerService;
  private final UserService userService;

  @PostMapping("/create/{id}")
  @PreAuthorize("isAuthenticated()")
  public String createAnswer(Principal principal,  Model model, @PathVariable("id") long id, @Valid AnswerForm answerForm, BindingResult bindingResult) {
    Question question = this.questionService.getQuestion(id);

    if (bindingResult.hasErrors()) {
      model.addAttribute("question", question);

      return "question_detail";
    }

    SiteUser siteUser = userService.getUser(principal.getName());

    answerService.create(question, answerForm.getContent(), siteUser);

    return "redirect:/question/detail/%d".formatted(id);
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/modify/{id}")
  public String answerModify(AnswerForm answerForm, @PathVariable("id") Long id, Principal principal) {
    Answer answer = answerService.getAnswer(id);

    if (answer == null) {
      throw new DataNotFoundException("데이터가 없습니다.");
    }

    if (!answer.getAuthor().getUsername().equals(principal.getName())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
    }

    answerForm.setContent(answer.getContent());

    return "answer_form";
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/modify/{id}")
  @ResponseBody
  public String answerModify(@Valid AnswerForm answerForm, BindingResult bindingResult, @PathVariable("id") Long id, Principal principal) {
    return answerForm.getContent();
  }
}
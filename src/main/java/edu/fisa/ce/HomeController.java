package edu.fisa.ce;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Spring Boot Auto Deploy Practice");
        model.addAttribute("message", "파일 변경 감지와 자동 배포 실습용 페이지입니다.");
        return "index";
    }

    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
        return "Hello Spring Boot";
    }
}
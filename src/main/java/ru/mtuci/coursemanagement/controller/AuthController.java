package ru.mtuci.coursemanagement.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mtuci.coursemanagement.model.User;
import ru.mtuci.coursemanagement.service.UserService;

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService users;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpServletRequest req,
                          Model model) {
        Optional<User> opt = users.findByUsername(username);
        if (opt.isPresent()) {
            User u = opt.get();
            if (passwordEncoder.matches(password, u.getPassword())) {
                // A02: не логируем пароль
                log.info("User {} logged in successfully", username);

                HttpSession s = req.getSession(true);
                s.setAttribute("username", username);
                s.setAttribute("role", u.getRole());
                return "redirect:/";
            }
        }
        model.addAttribute("error", "Неверные учетные данные");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();
        return "redirect:/login";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(required = false, defaultValue = "STUDENT") String role) {

        // A06:2025 Insecure Design — whitelist ролей
        if (!"STUDENT".equals(role) && !"TEACHER".equals(role)) {
            role = "STUDENT";
        }

        String hashedPassword = passwordEncoder.encode(password);
        users.save(new User(null, username, hashedPassword, role));

        return "redirect:/login";
    }
}

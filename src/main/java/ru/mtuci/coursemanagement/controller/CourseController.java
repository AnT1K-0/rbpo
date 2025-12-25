package ru.mtuci.coursemanagement.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import ru.mtuci.coursemanagement.model.Course;
import ru.mtuci.coursemanagement.repository.CourseRepository;
import ru.mtuci.coursemanagement.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import java.net.URI;

import java.util.List;

@Slf4j
@Controller

@RequiredArgsConstructor
public class CourseController {
    private final CourseRepository repo;
    private final CourseService service;

    @GetMapping("/courses")
    public String coursesPage(Model model) {
        model.addAttribute("courses", repo.findAll());
        model.addAttribute("course", new Course());
        return "courses";
    }

    @PostMapping("/courses")
    public String createCourse(@ModelAttribute Course c) {
        repo.save(c);
        return "redirect:/courses";
    }

    @GetMapping("/api/courses")
    @ResponseBody
    public List<Course> all() {
        return repo.findAll();
    }

    @GetMapping("/api/courses/{id}")
    @ResponseBody
    public ResponseEntity<Course> one(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/api/courses/{id}")
    @ResponseBody
    public ResponseEntity<Course> update(@PathVariable Long id, @RequestBody Course payload) {
        return repo.findById(id).map(c -> {
            c.setTitle(payload.getTitle());
            c.setDescription(payload.getDescription());
            c.setTeacherId(payload.getTeacherId());
            return ResponseEntity.ok(repo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/courses/search")
    @ResponseBody
    public List<Course> search(@RequestParam String title) {
        return service.searchByTitle(title);
    }

    @GetMapping("/api/courses/import")
    @ResponseBody
    @GetMapping("/api/courses/import")
    @ResponseBody
    public ResponseEntity<String> importFromUrl(@RequestParam String url) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid URL scheme");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid URL host");
            }

            RestTemplate rt = new RestTemplate();
            rt.getForObject(uri, String.class);

            log.info("Импортированы данные курсов из внешнего источника");
            return ResponseEntity.ok("OK");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid URL");

        } catch (RestClientException e) {
            log.warn("Import failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Upstream request failed");

        } catch (Exception e) {
            log.error("Unexpected import error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error");
        }
    }

}

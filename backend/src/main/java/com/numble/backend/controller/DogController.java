package com.numble.backend.controller;

import com.numble.backend.model.Dog;
import com.numble.backend.service.DogService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dogs")
public class DogController {

    private final DogService dogService;

    @Autowired
    public DogController(DogService dogService) {
        this.dogService = dogService;
    }

    @GetMapping
    public Page<Dog> getAllDogs(Pageable pageable) {
        return dogService.getAllDogs(pageable);
    }

    @GetMapping("/{id}")
    public Dog getDogById(@PathVariable Long id) {
        return dogService.getDog(id);
    }

    @PostMapping
    public Dog createDog(@RequestBody Dog dog) {
        return dogService.createDog(dog);
    }

    @PutMapping("/{id}")
    public Dog updateDog(@PathVariable Long id, @RequestBody Dog dog) {
        return dogService.updateDog(id, dog);
    }

    @DeleteMapping("/{id}")
    public void deleteDog(@PathVariable Long id) {
        dogService.deleteDog(id);
    }

    @PostMapping("/{id}/vote")
    public void voteDog(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 투표 정보를 확인
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("voted_" + id) && cookie.getValue().equals("true")) {
                throw new RuntimeException("Already voted");
            }
        }

        // 투표 수를 증가시킴
        dogService.voteDog(id);

        // 쿠키에 투표 정보를 저장
        Cookie cookie = new Cookie("voted_" + id, "true");
        response.addCookie(cookie);
    }

    @PostMapping("/{id}/unvote")
    public void unvoteDog(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 투표 정보를 확인
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("voted_" + id) && cookie.getValue().equals("true")) {
                // 투표 수를 감소시킴
                dogService.unvoteDog(id);

                // 쿠키에서 투표 정보를 제거
                cookie.setMaxAge(0);
                response.addCookie(cookie);

                return;
            }
        }

        throw new RuntimeException("Not voted yet");
    }
}

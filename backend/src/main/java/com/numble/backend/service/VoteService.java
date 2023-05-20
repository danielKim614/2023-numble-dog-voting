package com.numble.backend.service;

import com.numble.backend.model.Dog;
import com.numble.backend.repository.DogRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class VoteService {
    private final DogRepository dogRepository;

    @Autowired
    public VoteService(DogRepository dogRepository) {
        this.dogRepository = dogRepository;
    }

    @KafkaListener(topics = "dog-votes", groupId = "dog-group")
    public void listen(String message) {
        // 여기서 message는 강아지의 ID와 투표 상태를 나타내는 문자열이라고 가정합니다.
        String[] parts = message.split(":");
        Long dogId = Long.parseLong(parts[0]);
        boolean vote = Boolean.parseBoolean(parts[1]);

        Dog dog = dogRepository.findById(dogId).orElseThrow(() -> new RuntimeException("Dog not found"));

        if (vote) {
            dog.setVotes(dog.getVotes() + 1);
        } else {
            dog.setVotes(dog.getVotes() - 1);
        }

        dogRepository.save(dog);
    }
}

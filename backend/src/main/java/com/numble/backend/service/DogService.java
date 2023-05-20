package com.numble.backend.service;

import com.numble.backend.model.Dog;
import com.numble.backend.repository.DogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.CacheEvict;

@Service
public class DogService {

    private final DogRepository dogRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public DogService(DogRepository dogRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.dogRepository = dogRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Cacheable(value = "dogs", key = "#id")
    public Dog getDog(Long id) {
        return dogRepository.findById(id).orElseThrow(() -> new RuntimeException("Dog not found"));
    }

    @Cacheable(value = "dogs", key = "#root.method.name")
    public Page<Dog> getAllDogs(Pageable pageable) {
        return dogRepository.findAll(pageable);
    }

    public void voteDog(Long id) {
        kafkaTemplate.send("dog-votes", id + ":true");
    }

    public void unvoteDog(Long id) {
        kafkaTemplate.send("dog-votes", id + ":false");
    }

    @CachePut(value = "dogs", key = "#dog.id")
    public Dog createDog(Dog dog) {
        return dogRepository.save(dog);
    }

    @CachePut(value = "dogs", key = "#id")
    public Dog updateDog(Long id, Dog dog) {
        Dog existingDog = getDog(id);
        existingDog.setName(dog.getName());
        existingDog.setVotes(dog.getVotes());
        return dogRepository.save(existingDog);
    }

    @CacheEvict(value = "dogs", key = "#id")
    public void deleteDog(Long id) {
        dogRepository.deleteById(id);
    }
}
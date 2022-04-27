package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class Controller {
    private final PlayerService playerService;

    @Autowired
    public Controller(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/players")
    public ResponseEntity<List<Player>> getAllPlayers(
            @RequestParam(name = "name", defaultValue = "") String name,
            @RequestParam(name = "title", defaultValue = "") String title,
            @RequestParam(name = "race", defaultValue = "") Race race,
            @RequestParam(name = "profession", defaultValue = "") Profession profession,
            @RequestParam(name = "after", defaultValue = "") Long after,
            @RequestParam(name = "before", defaultValue = "") Long before,
            @RequestParam(name = "banned", defaultValue = "") Boolean banned,
            @RequestParam(name = "minExperience", defaultValue = "") Integer minExperience,
            @RequestParam(name = "maxExperience", defaultValue = "") Integer maxExperience,
            @RequestParam(name = "minLevel", defaultValue = "") Integer minLevel,
            @RequestParam(name = "maxLevel", defaultValue = "") Integer maxLevel,
            @RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "3") Integer pageSize,
            @RequestParam(name = "order", defaultValue = "ID") PlayerOrder order) {
        return ResponseEntity.ok(playerService.getAllPlayers(
                name,
                title,
                race,
                profession,
                after,
                before,
                banned,
                minExperience,
                maxExperience,
                minLevel,
                maxLevel,
                pageNumber,
                pageSize,
                order));
    }

    @GetMapping("/players/count")
    public Integer count(
            @RequestParam(name = "name", defaultValue = "") String name,
            @RequestParam(name = "title", defaultValue = "") String title,
            @RequestParam(name = "race", defaultValue = "") Race race,
            @RequestParam(name = "profession", defaultValue = "") Profession profession,
            @RequestParam(name = "after", defaultValue = "") Long after,
            @RequestParam(name = "before", defaultValue = "") Long before,
            @RequestParam(name = "banned", defaultValue = "") Boolean banned,
            @RequestParam(name = "minExperience", defaultValue = "") Integer minExperience,
            @RequestParam(name = "maxExperience", defaultValue = "") Integer maxExperience,
            @RequestParam(name = "minLevel", defaultValue = "") Integer minLevel,
            @RequestParam(name = "maxLevel", defaultValue = "") Integer maxLevel) {
        return playerService.count(name,
                title,
                race,
                profession,
                after,
                before,
                banned,
                minExperience,
                maxExperience,
                minLevel,
                maxLevel);
    }

    @PostMapping("/players")
    public ResponseEntity<Player> newPlayer(@RequestBody Player player) {
        return ResponseEntity.ok(playerService
                .save(player)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Player can't be saved")));
    }

    @RequestMapping(value = "/players/{id}", method = RequestMethod.GET)
    public ResponseEntity<Player> findById(@PathVariable("id") String id) {
        if (!id.chars().allMatch(Character::isDigit) || Long.parseLong(id) <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(playerService
                .findById(Long.parseLong(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No players with specified ID were found")));
    }

    @RequestMapping(value = "/players/{id}", method = RequestMethod.POST)
    public ResponseEntity<Player> updateById(@PathVariable("id") String id, @RequestBody Player player) {
        if (id.chars().allMatch(Character::isDigit) && checkEmptyBodyBeforeUpdate(player)) {
            System.out.println("here");
            return ResponseEntity.ok(playerService
                    .findById(Long.parseLong(id))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "No players with specified ID were found")));
        }

        if (checkBeforeUpdate(id, player)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(playerService
                .update(Long.parseLong(id), player)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Player can't be saved")));
    }

    @RequestMapping(value = "/players/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Player> deleteById(@PathVariable("id") String id) {
        if (!id.chars().allMatch(Character::isDigit) || Long.parseLong(id) <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return playerService.deleteById(Long.parseLong(id)) ?
                new ResponseEntity<>(HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    private Boolean checkBeforeUpdate(String id, Player player) {
        System.out.println(player.getBirthday() + " " + player.getExperience() + " " + id);
        return !id.chars().allMatch(Character::isDigit)
                || Long.parseLong(id) <= 0
                || !(player.getBirthday().toLocalDate().getYear() >= 2000
                && player.getBirthday().toLocalDate().getYear() <= 3000)
                || !(player.getExperience() >= 0 && player.getExperience() <= 10_000_000);
    }

    private Boolean checkEmptyBodyBeforeUpdate(Player player) {
        return player.getId() == null
                && player.getName() == null
                && player.getTitle() == null
                && player.getRace() == null
                && player.getProfession() == null
                && player.getBirthday() == null
                && player.getBanned() == null
                && player.getExperience() == null;
    }
}

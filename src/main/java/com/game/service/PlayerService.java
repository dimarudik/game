package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public List<Player> getAllPlayers(String name, String title, Race race, Profession profession, Long after,
                                      Long before, Boolean banned, Integer minExperience, Integer maxExperience,
                                      Integer minLevel, Integer maxLevel, Integer pageNo, Integer pageSize,
                                      PlayerOrder order) {
        Pageable paging = PageRequest.of(pageNo, pageSize);
        int start = (int) paging.getOffset();
        int end = (int) (paging.getOffset() + paging.getPageSize());
        List<Player> players = getPlayersFiltered(name, title, race, profession, after, before, banned,
                minExperience, maxExperience, minLevel, maxLevel, order);
        Page<Player> pagedResult = new PageImpl<>(players.subList(start, (Math.min(end, players.size()))),
                paging, players.size());
        if (pagedResult.hasContent()) {
            return pagedResult.getContent();
        } else {
            return new ArrayList<>();
        }
    }

    public Integer count(String name, String title, Race race, Profession profession, Long after, Long before,
                         Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel,
                         Integer maxLevel) {
        return getPlayersFiltered(name, title, race, profession, after, before, banned, minExperience,
                maxExperience, minLevel, maxLevel, PlayerOrder.valueOf("ID")).size();
    }

    public Optional<Player> savePlayer(Player player) {
        if (checkNewPlayer(player)) {
            return Optional.empty();
        }
        player.setLevel((int) ((Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100));
        player.setUntilNextLevel(50 * (player.getLevel() + 1) * (player.getLevel() + 2) - player.getExperience());
        return Optional.of(playerRepository.save(player));
    }

    public Optional<Player> findById(Long id) {
        return playerRepository.findById(id);
    }

    public Optional<Player> updatePlayer(Long id, Player player) {
        Player oldPlayer;
        if (!playerRepository.existsById(id)) {
            return Optional.empty();
        } else {
            player.setId(Objects.requireNonNull(playerRepository.findById(id).orElse(null)).getId());
            oldPlayer = findById(player.getId()).orElse(null);
            player = updateNewPlayer(oldPlayer, player);
        }
//        System.out.println(player.getId());
        player.setLevel((int) ((Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100));
        player.setUntilNextLevel(50 * (player.getLevel() + 1) * (player.getLevel() + 2) - player.getExperience());
        return Optional.of(playerRepository.save(player));
    }

    public Boolean deleteById(Long id) {
        if (playerRepository.existsById(id)) {
            playerRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private List<Player> getPlayersFiltered(String name, String title, Race race, Profession profession, Long after,
                                            Long before, Boolean banned, Integer minExperience, Integer maxExperience,
                                            Integer minLevel, Integer maxLevel, PlayerOrder order) {
        return playerRepository
                .findAll(Sort.by(order.getFieldName()))
                .stream()
                .filter(player -> player.getName().contains(name))
                .filter(player -> player.getTitle().contains(title))
                .filter(player -> race == null || player.getRace().equals(race))
                .filter(player -> profession == null || player.getProfession().equals(profession))
                .filter(player -> after == null || player.getBirthday().getTime() >= after)
                .filter(player -> before == null || player.getBirthday().getTime() <= before)
                .filter(player -> banned == null || player.getBanned().equals(banned))
                .filter(player -> minExperience == null || player.getExperience() >= minExperience)
                .filter(player -> maxExperience == null || player.getExperience() <= maxExperience)
                .filter(player -> minLevel == null || player.getLevel() >= minLevel)
                .filter(player -> maxLevel == null || player.getLevel() <= maxLevel)
                .collect(Collectors.toList());
    }

    private Boolean checkNewPlayer(Player player) {
        return player.getName() == null
                || player.getTitle() == null
                || player.getRace() == null
                || player.getProfession() == null
                || player.getBirthday() == null
                || player.getExperience() == null
                || player.getName().length() > 12
                || player.getTitle().length() > 30
                || !(player.getBirthday().toLocalDate().getYear() >= 2000
                && player.getBirthday().toLocalDate().getYear() <= 3000)
                || !(player.getExperience() >= 0 && player.getExperience() <= 10_000_000);
    }

    private Player updateNewPlayer (Player oldPlayer, Player newPlayer) {
        if (newPlayer.getName() != null) {
            oldPlayer.setName(newPlayer.getName());
        }
        if (newPlayer.getTitle() != null) {
            oldPlayer.setTitle(newPlayer.getTitle());
        }
        if (newPlayer.getRace() != null) {
            oldPlayer.setRace(newPlayer.getRace());
        }
        if (newPlayer.getProfession() != null) {
            oldPlayer.setProfession(newPlayer.getProfession());
        }
        if (newPlayer.getBirthday() != null) {
            oldPlayer.setBirthday(newPlayer.getBirthday());
        }
        if (newPlayer.getBanned() != null) {
            oldPlayer.setBanned(newPlayer.getBanned());
        }
        if (newPlayer.getExperience() != null) {
            oldPlayer.setExperience(newPlayer.getExperience());
        }
        if (newPlayer.getLevel() != null) {
            oldPlayer.setLevel(newPlayer.getLevel());
        }
        return oldPlayer;
    }
}

package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.dto.PlayerDTO;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayersRepository;
import com.game.repository.specs.PlayerSpecification;
import com.game.repository.specs.SearchCriteria;
import com.game.repository.specs.SearchOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class PlayersService {
    private final PlayersRepository playersRepository;

    @Autowired
    public PlayersService(PlayersRepository playersRepository) {
        this.playersRepository = playersRepository;
    }

    public List<Player> findAll() {
        return playersRepository.findAll();
    }

    public List<Player> findAll(String name, String title, Race race, Profession profession,
                                Long after, Long before, Boolean banned,
                                Integer minExperience, Integer maxExperience,
                                Integer minLevel, Integer maxLevel) {

        PlayerSpecification spec = prepareSpecification(name, title, race, profession, after, before,
                banned, minExperience, maxExperience, minLevel, maxLevel);

        return playersRepository.findAll(spec);
    }

    public List<Player> findAll(String name,
                                String title, Race race, Profession profession,
                                Long after, Long before, Boolean banned, Integer minExperience,
                                Integer maxExperience, Integer minLevel, Integer maxLevel,
                                PlayerOrder order, Integer pageNumber, Integer pageSize) {

        PlayerSpecification spec = prepareSpecification(name, title, race, profession, after, before,
                                            banned, minExperience, maxExperience, minLevel, maxLevel);

        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                Sort.by(order == null ? PlayerOrder.ID.getFieldName() : order.getFieldName()));

        return playersRepository.findAll(spec, pageable).getContent();
    }


    public Player findOne(Long id) {
        if (!playersRepository.findById(id).isPresent())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        else
            return playersRepository.findById(id).get();
    }


    @Transactional
    public void save(Player player) {
        enrichPlayer(player);
        playersRepository.save(player);
    }

    @Transactional
    public Player update(Long id, PlayerDTO newPlayer) {
        Player existingPlayer = findOne(id);
        prepareForUpdate(newPlayer, existingPlayer);
        playersRepository.saveAndFlush(existingPlayer);
        return existingPlayer;
    }

    @Transactional
    public void delete(Long id) {
        playersRepository.deleteById(id);
        playersRepository.flush();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private PlayerSpecification prepareSpecification(String name, String title, Race race,
                                                     Profession profession, Long after, Long before,
                                                     Boolean banned, Integer minExperience, Integer maxExperience,
                                                     Integer minLevel, Integer maxLevel) {
        PlayerSpecification spec = new PlayerSpecification();

        if(name != null)
            spec.add(new SearchCriteria("name", name, SearchOperation.MATCH));
        if(title != null)
            spec.add(new SearchCriteria("title", title, SearchOperation.MATCH));
        if(race!=null)
            spec.add(new SearchCriteria("race", race, SearchOperation.EQUAL));
        if (profession != null)
            spec.add(new SearchCriteria("profession", profession, SearchOperation.EQUAL));
        if (banned != null)
            spec.add(new SearchCriteria("banned", banned, SearchOperation.EQUAL));
        if (minLevel != null)
            spec.add(new SearchCriteria("level", minLevel, SearchOperation.GREATER_THAN_EQUAL));
        if (maxLevel != null)
            spec.add(new SearchCriteria("level", maxLevel, SearchOperation.LESS_THAN_EQUAL));
        if (minExperience != null)
            spec.add(new SearchCriteria("experience", minExperience, SearchOperation.GREATER_THAN_EQUAL));
        if (maxExperience != null)
            spec.add(new SearchCriteria("experience", maxExperience, SearchOperation.LESS_THAN_EQUAL));
        if (before != null)
            spec.add(new SearchCriteria("birthday", new Date(before), SearchOperation.DATE_LESS_THAN_EQUAL));
        if (after != null)
            spec.add(new SearchCriteria("birthday", new Date(after), SearchOperation.DATE_GREATER_THAN_EQUAL));

        return spec;
    }

    private void enrichPlayer(Player player) {
        int level = (((int) (Math.sqrt(2500 + 200 * player.getExperience())) - 50) / 100);
        int untilNext = (50 * (level + 1) * (level + 2) - player.getExperience());
        player.setLevel(level);
        player.setUntilNextLevel(untilNext);
    }

    private void prepareForUpdate(PlayerDTO newPlayer, Player existingPlayer) {
        boolean worthEnriching = false;

        if (newPlayer.getName() != null && (newPlayer.getName().length() < 13 && newPlayer.getName().length() > 0)) {
            worthEnriching = true;
            existingPlayer.setName(newPlayer.getName());
        }

        if (newPlayer.getTitle() != null && newPlayer.getTitle().length() < 30) {
            existingPlayer.setTitle(newPlayer.getTitle());
            worthEnriching = true;
        }

        if (newPlayer.getRace() != null) {
            existingPlayer.setRace(newPlayer.getRace());
            worthEnriching = true;
        }

        if (newPlayer.getProfession() != null) {
            worthEnriching = true;
            existingPlayer.setProfession(newPlayer.getProfession());
        }

        if (newPlayer.getExperience() != null) {
            if (newPlayer.getExperience() >= 0 && newPlayer.getExperience() <= 10_000_000) {
                worthEnriching = true;
                existingPlayer.setExperience(newPlayer.getExperience());
            } else
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (newPlayer.getBanned() != null) {
            worthEnriching = true;
            existingPlayer.setBanned(newPlayer.getBanned());
        }

        if (newPlayer.getBirthday() != null){
            if (newPlayer.getBirthday() > 946684800000L && newPlayer.getBirthday() < 32503680000000L) {
                worthEnriching = true;
                existingPlayer.setBirthday(new Date(newPlayer.getBirthday()));
            } else
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (worthEnriching)
            enrichPlayer(existingPlayer);
    }
}

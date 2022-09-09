package com.game.controller;

import com.game.dto.PlayerDTO;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayersService;
import com.game.util.PlayerValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/rest/players")
public class PlayersController {
    private final PlayersService playersService;
    private final PlayerValidator validator;
    @Autowired
    public PlayersController(PlayersService playersService, PlayerValidator validator) {
        this.playersService = playersService;
        this.validator = validator;
    }

    @GetMapping()
    public List<Player> getPlayersList(@RequestParam(value = "name", required = false) String name,
                                       @RequestParam(value = "title", required = false) String title,
                                       @RequestParam(value = "race", required = false) Race race,
                                       @RequestParam(value = "profession", required = false) Profession profession,
                                       @RequestParam(value = "after", required = false) Long after,
                                       @RequestParam(value = "before", required = false) Long before,
                                       @RequestParam(value = "banned", required = false) Boolean banned,
                                       @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                       @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                       @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                       @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
                                       @RequestParam(value = "order", required = false) PlayerOrder order,
                                       @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                                       @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize) {

        return playersService.findAll(name, title, race, profession, after, before, banned,
                minExperience, maxExperience, minLevel, maxLevel, order, pageNumber, pageSize);
    }

    @GetMapping("/count")
    public Integer getPlayersCount(@RequestParam(value = "name", required = false) String name,
                                   @RequestParam(value = "title", required = false) String title,
                                   @RequestParam(value = "race", required = false) Race race,
                                   @RequestParam(value = "profession", required = false) Profession profession,
                                   @RequestParam(value = "after", required = false) Long after,
                                   @RequestParam(value = "before", required = false) Long before,
                                   @RequestParam(value = "banned", required = false) Boolean banned,
                                   @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                   @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                   @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                   @RequestParam(value = "maxLevel", required = false) Integer maxLevel) {

        return playersService.findAll(name, title, race, profession, after, before, banned,
                minExperience, maxExperience, minLevel, maxLevel).size();
    }

    @PostMapping()
    @ResponseBody
    public ResponseEntity<Player> createPlayer(@RequestBody Player player,
                                               BindingResult bindingResult) {
        validator.validate(player, bindingResult);
        if (bindingResult.hasErrors())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (player.getBanned() == null) player.setBanned(false);

        playersService.save(player);
        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public Player getPlayer(@PathVariable("id") Long id) {
        validator.validateId(id);
        return playersService.findOne(id);
    }

    @PostMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Player> updatePlayer(@PathVariable(value = "id", required = false) Long id,
                                               @RequestBody PlayerDTO newPlayer) {
        validator.validateId(id);
        return new ResponseEntity<>(playersService.update(id, newPlayer),
                                    HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public void deletePlayer(@PathVariable("id") Long id) {
        validator.validateId(id);
        playersService.delete(id);
    }
}

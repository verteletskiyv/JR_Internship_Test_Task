package com.game.util;

import com.game.entity.Player;
import com.game.service.PlayersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@Component
public class PlayerValidator implements Validator {
    private final PlayersService playersService;

    @Autowired
    public PlayerValidator(PlayersService playersService) {
        this.playersService = playersService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Player.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Player player = (Player) target;
        if (player.getName() == null || player.getName().length() > 12 || player.getName().length() < 1)
            errors.rejectValue("name", "", "Name's length should be between 0 and 12 characters");
        if (player.getTitle() == null || player.getTitle().length() > 30)
            errors.rejectValue("title", "", "Title can not be longer than 30 characters");
        if (player.getExperience() == null || player.getExperience() < 0 || player.getExperience() > 10_000_000)
            errors.rejectValue("experience", "", "Experience should be between 0 and 10.000.000");
        if (player.getBirthday() == null || player.getBirthday().before(new Date(946684800L*1000)) || player.getBirthday().after(new Date(32503680000L *1000)))
            errors.rejectValue("birthday", "", "Birthday should be between 2000 and 3000");
    }

    public void validateId(Long id) {
        if (id != null) {
            if (id <= 0)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID");
            else if (id > playersService.findAll().size())
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        } else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID");

    }
}

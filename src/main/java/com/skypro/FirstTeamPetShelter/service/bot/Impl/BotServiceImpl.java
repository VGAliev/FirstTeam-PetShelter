package com.skypro.FirstTeamPetShelter.service.bot.Impl;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.skypro.FirstTeamPetShelter.model.Adopter;
import com.skypro.FirstTeamPetShelter.model.UserApp;
import com.skypro.FirstTeamPetShelter.model.Volunteer;
import com.skypro.FirstTeamPetShelter.service.AdopterService;
import com.skypro.FirstTeamPetShelter.service.UserService;
import com.skypro.FirstTeamPetShelter.service.VolunteerService;
import com.skypro.FirstTeamPetShelter.service.bot.BotMenuService;
import com.skypro.FirstTeamPetShelter.service.bot.BotService;
import com.skypro.FirstTeamPetShelter.service.InfoService;
import com.skypro.FirstTeamPetShelter.service.bot.helper.Menu;
import com.skypro.FirstTeamPetShelter.service.bot.helper.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BotServiceImpl implements BotService {
    private final Logger logger = LoggerFactory.getLogger(BotService.class);

    private final UserService userService;
    private final AdopterService adopterService;
    private final VolunteerService volunteerService;

    private final BotMenuService botMenuService;
    private final InfoService infoService;

    public BotServiceImpl(UserService userService, AdopterService adopterService, VolunteerService volunteerService, BotMenuService botMenuService, InfoService infoService) {
        this.userService = userService;
        this.adopterService = adopterService;
        this.volunteerService = volunteerService;
        this.botMenuService = botMenuService;
        this.infoService = infoService;
    }

    @Override
    public Role getVisitorRole(Long visitorId) {
        if (userService.getUserByTelegramId(visitorId) != null) {
            return Role.USER;
        } else if (adopterService.getAdopterByTelegramId(visitorId) != null) {
            return Role.ADOPTER;
        } else if (volunteerService.getVolunteerById(visitorId) != null) {
            return Role.VOLUNTEER;
        } else {
            return Role.NEWBIE;
        }
    }

    @Override
    public void sendResponseFromUpdate(TelegramBot telegramBot, Update update, String messageText, Menu menu) {
        messageText = parseMessageText(update, messageText);
        SendMessage sendMessage;
        try {
            sendMessage = new SendMessage(update.message().chat().id(), messageText);
            if (menu != null) {
                sendMessage.replyMarkup(botMenuService.getMenu(menu));
            }
        } catch (RuntimeException e) {
            logger.error(e.getMessage());
            messageText = infoService.getMessage("Error");
            sendMessage = new SendMessage(update.message().chat().id(), messageText);
        }
        telegramBot.execute(sendMessage);
    }

    @Override
    public List<UserApp> getUsersCallingVolunteer() {
        return List.of();
    }

    @Override
    public List<Adopter> getAdoptersCallingVolunteer() {
        return List.of();
    }

    @Override
    public List<Adopter> getAdoptersReportCheck() {
        return List.of();
    }

    @Override
    public List<UserApp> getUsersBecomeAdoptive() {
        return List.of();
    }

    @Override
    public void callVolunteer(TelegramBot telegramBot, Update update) {
        Role role = getVisitorRole(update.message().from().id());
        if (role == Role.NEWBIE) {
            UserApp userApp = new UserApp();
            String messageText = infoService.getMessage("UserNameAndPhoneNumber");
            sendResponseFromUpdate(telegramBot, update, messageText, Menu.USERNAME_PHONE_NUMBER);
            userApp.setUserName(update.message().from().firstName());
            userApp.setUserTelegramId(update.message().from().id());
            userApp.setContacted(false);
            userService.addUser(userApp);
        } else if (role == Role.USER) {
            userService.getUserByTelegramId(update.message().from().id()).setContacted(false);
        } else if (role == Role.ADOPTER) {
            adopterService.getAdopterByTelegramId(update.message().from().id()).setContacted(false);
        } else {
            return;
        }

        sendMessageToRandomVolunteer(telegramBot, update);
    }

    private void sendMessageToRandomVolunteer(TelegramBot telegramBot, Update update) {
        List<Volunteer> volunteers = volunteerService.getAllVolunteer().stream().toList();
        long volunteerId = volunteers.stream()
                .skip((int)(volunteers.size() * Math.random()))
                .findFirst()
                .get()
                .getVolunteerTelegramId();
        String messageToVolunteer = "Вас вызывает " + update.message().from().firstName() + " из бота FirstTeam Pet Shelter.";
        SendMessage sendMessage = new SendMessage(volunteerId, messageToVolunteer);
        telegramBot.execute(sendMessage);
    }

    private String parseMessageText(Update update, String messageText) {
        String result = messageText;
        if (result.contains("{username}")) {
            result = result.replace("{username}", update.message().from().firstName());
        }
        if (result.contains("{usercontact}")) {
            result = result.replace("{usercontact}", userService.getUserByTelegramId(update.message().from().id()).getUserPhoneNumber());
        }
        return result;
    }
}

package com.skypro.FirstTeamPetShelter.service.bot.helper;

public enum Menu {
    START,                          // Стартовое меню (список приютов)
    ANSWER_CONTACTED,               // Вопрос пользователю, связывались ли с ним
    CALLING_USERS,                  // Список пользователей, звавших волонтёра
    CALLING_ADOPTERS,               // Список усыновителей, звавших волонтёра
    CHECK_REPORTS,                  // Отчеты, которые ещё не проверялись
    USERS_BECOME_ADOPTIVE,          // Список потенциальных усыновителей
    ADOPTER_SEND_REPORT,            // Отправить отчёт
    USERNAME_PHONE_NUMBER,          // Запрос имени пользователя и номера телефона
}

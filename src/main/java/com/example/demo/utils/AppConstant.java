package com.example.demo.utils;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface AppConstant {
    String START_MESSAGE = "Хола амигос";
    String BUY_PERMISSION = "Купить доступ";
    String BUY_PERMISSION_DESCRIPTION = "Для покупки вам необходимо указать сколько времени вы хотите купить";
    String ONE_MONTH = "1 Месяц";
    String SIX_MONTH = "6 Месяц";
    String ONE_YEAR = "1 Год";
    String AFTER_BUYING_REQUEST = "Вот ващ код";
    String ACTIVATION_CODE = "Написать код";
    String SEND_CODE = "Отправьте пароля";
    String DONT_HAVE_BOT_PASS = "У вас нет заявления чтобы получить доступ или уже срок изтек";
    String DONT_EQUALS_PASS = "Пароль не совподаеть";
    String SUCCESSFULLY_BUY = "Поздравляю вы покупали доступ";
    String SUCCESSFULLY_LENGTHENED = "Успешно длинилос";
    String MY_GROUPS = "Мои групи";
    String OWNER_NOT_BUY_PERMISSION = "Вы не купили возможность использовать этот бота, поэтому просим купить доступ или выгнать бота из своих каналов или групп.";
    String USER_MUST_BUY = "Здравствуйте вы должны купить доступ чтобы вступать этот канал или группу. прежде чем использовать бота вам следует написать /start";
    String CHANNEL_LIST = "Список каналы запроса";
    String CHANNEL_LIST_SHOW_TEXT = "Вот это который вы отправили запрос что бы вступать и который бот ответе за вступления";
    String PRICE_NULL = "Бесплатно на месяц";
    String MY_MISTAKE = "От вас просим прощения я что то делал не так этот канал не платил что бы за ему бот работал ";
    String WITH_PRICE = "Выберите который вам нужно";
    String DONT_COMPLATED = "Пункт еще не готов к работу";
    String DONT_FREE = "Платный режим еще не готов";
    String DONT_HAVE_GROUPS = "Вы не гл.Админ который бот добавлен или попробуйте после час";
    String SHOW_GROUP_PRICE = "Цена за месця что бы вступать ";
    String SEND_PRICE_FOR_CHANGE = "Отправте цени который вы хотите изменить";
    String EXCEPTION_PRICE = "Что то вы ощиблис вот пример что бы изменить цену\n350000 или 0";
    String PRICE_CHANGED = "Поздравляю вы изменили цену";
    String SEND_CODE_TEXT = "Отправте кода который вы получила за покупку";
    String RESEND_LINK = "Извените за не удобство вы бы не могли бы еще раз раскилинут на линк ";
    String YOUR_CODE_FOR_JOIN = "Ваш код для вступления канал или группу который вы хотели купить ";
    String CODE_FOR_JOIN_CHAT = "Исп. код для вступить канал или группу";
    String DONT_HAVE_CHAT_PASS = "У вас нет заявления чтобы вступить на чат или уже срок изтек";
    String DONT_HAVE_ANY_CODE = "Вы отпровили не ту код ";




    String TEXT_BUY = "Купить доступ ";
    String DATA_BUY = "buy-groupId:";
    String TEXT_ABOUT_PRICE = "Узнать о цене";
    String DATA_ABOUT_PRICE = "aboutPrice-groupId:";
    String TEXT_REMOVE = "Убрать из запроса ";
    String DATA_REMOVE = "remove-groupId:";
    String BACK_TEXT = "Back";
    String BACK_DATA = "back:";
    String DATA_BACK_SHOW_REQUESTS = "showUserRequests";
    String DATA_USER_GROUPS = "userGroups:";
    String TEXT_CHANGE_PRICE = "Изменить цена";
    String DATA_CHANGE_PRICE = "changePrice:";
    String DATA_STOP_MANAGE_GROUP = "stopManageBot:";
    String TEXT_STOP_MANAGE_GROUP = "Ост. бот";
    String STOPED_MANAGE_BOT = " бот для него не работает";
    String TEXT_START_MANAGE_GROUP = "Старт бот";
    String DATA_START_MANAGE_GROUP = "startManageBot:";
    String DATA_SHOW_USER_GROUPS = "showUserGroups";
    String DATA_BUY_JOIN_REQ = "buyJoinReq:";
}

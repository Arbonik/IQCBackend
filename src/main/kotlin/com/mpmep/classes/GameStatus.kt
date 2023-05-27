package com.mpmep.classes

import kotlinx.serialization.Serializable

@Serializable
enum class GameStatus {
    READY, // оба игрока подключились
    FALSE, //  игрок ошибся - не обрабатывается
    GOT_NEW_EXAMPLE, // противник решил очередной пример
    FINISH, // примеры кончились, игрок ждет противника
    WIN, // победитель
    LOSE, // проигравший
    SHUTDOWN, // игра закончилась
    AWAIT, // игрок ожидает противника
    EMPTY // пришел новый пример
}
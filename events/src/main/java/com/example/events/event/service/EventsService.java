package com.example.events.event.service;

import com.example.events.event.dto.EventRequest;
import com.example.events.event.dto.EventResponse;

import java.util.List;

public interface EventsService {

    /**
     * Отправка события в сервис уведомлений
     *
     * @param request - Переданное событие для отправки
     * @return EventResponse - Созданное событие
     */
    EventResponse sendMessage(EventRequest request);

    /**
     * Получение всех событий
     *
     * @return List<EventResponse> - Все события
     */
    List<EventResponse> getAllEvents();

    /**
     * Получение события по id
     *
     * @return EventResponse - Полученное событие по id
     */
    EventResponse getEventById(Long eventId);

    /**
     * Обновление статуса события на DELIVERED
     *
     * @param eventId - id события для обновления статуса
     */
    void markAsDelivered(Long eventId);

    /**
     * Обновление статуса события на FAILED
     *
     * @param eventId - id события для обновления статуса
     */
    void markAsFailed(Long eventId);
}

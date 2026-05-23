package com.example.events.event;

import com.example.events.event.dto.EventRequest;
import com.example.events.event.dto.EventResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventsMapper {
    EventResponse toEventResponse(Events events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statusEvent", constant = "PENDING")
    @Mapping(target = "text", source = "text")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Events toEvents(EventRequest eventRequest);
}

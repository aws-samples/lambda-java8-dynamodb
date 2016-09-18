// Copyright 2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License"). You may
// not use this file except in compliance with the License. A copy of the
// License is located at
//
//	  http://aws.amazon.com/apache2.0/
//
// or in the "license" file accompanying this file. This file is distributed
// on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
// express or implied. See the License for the specific language governing
// permissions and limitations under the License.


package com.amazonaws.serverless.function;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import com.amazonaws.serverless.dao.DynamoDBEventDao;
import com.amazonaws.serverless.pojo.Team;
import com.amazonaws.serverless.util.Consts;
import org.apache.log4j.Logger;

import com.amazonaws.serverless.domain.Event;
import com.amazonaws.serverless.pojo.City;


public class EventFunctions {

    private static final Logger log = Logger.getLogger(EventFunctions.class);

    private static final DynamoDBEventDao eventDao = DynamoDBEventDao.instance();


    public List<Event> getAllEventsHandler() {

        log.info("GetAllEvents invoked to scan table for ALL events");
        List<Event> events = eventDao.findAllEvents();
        log.info("Found " + events.size() + " total events.");
        return events;
    }

    public List<Event> getEventsForTeam(Team team) throws UnsupportedEncodingException {

        if (null == team || team.getTeamName().isEmpty() || team.getTeamName().equals(Consts.UNDEFINED)) {
            log.error("GetEventsForTeam received null or empty team name");
            throw new IllegalArgumentException("Team name cannot be null or empty");
        }

        String name = URLDecoder.decode(team.getTeamName(), "UTF-8");
        log.info("GetEventsForTeam invoked for team with name = " + name);
        List<Event> events = eventDao.findEventsByTeam(name);
        log.info("Found " + events.size() + " events for team = " + name);

        return events;
    }

    public List<Event> getEventsForCity(City city) throws UnsupportedEncodingException {

        if (null == city || city.getCityName().isEmpty() || city.getCityName().equals(Consts.UNDEFINED)) {
            log.error("GetEventsForCity received null or empty city name");
            throw new IllegalArgumentException("City name cannot be null or empty");
        }

        String name = URLDecoder.decode(city.getCityName(), "UTF-8");
        log.info("GetEventsForCity invoked for city with name = " + name);
        List<Event> events = eventDao.findEventsByCity(name);
        log.info("Found " + events.size() + " events for city = " + name);

        return events;
    }

    public void saveOrUpdateEvent(Event event) {

        if (null == event) {
            log.error("SaveEvent received null input");
            throw new IllegalArgumentException("Cannot save null object");
        }

        log.info("Saving or updating event for team = " + event.getHomeTeam() + " , date = " + event.getEventDate());
        eventDao.saveOrUpdateEvent(event);
        log.info("Successfully saved/updated event");
    }

    public void deleteEvent(Event event) {

        if (null == event) {
            log.error("DeleteEvent received null input");
            throw new IllegalArgumentException("Cannot delete null object");
        }

        log.info("Deleting event for team = " + event.getHomeTeam() + " , date = " + event.getEventDate());
        eventDao.deleteEvent(event.getHomeTeam(), event.getEventDate());
        log.info("Successfully deleted event");
    }

}

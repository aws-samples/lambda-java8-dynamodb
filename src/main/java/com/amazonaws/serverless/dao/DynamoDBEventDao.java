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


package com.amazonaws.serverless.dao;


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.serverless.domain.Event;
import com.amazonaws.serverless.manager.DynamoDBManager;
import org.apache.log4j.Logger;

import java.util.*;


public class DynamoDBEventDao implements EventDao {

    private static final Logger log = Logger.getLogger(DynamoDBEventDao.class);

    private static final DynamoDBMapper mapper = DynamoDBManager.mapper();

    private static volatile DynamoDBEventDao instance;


    private DynamoDBEventDao() { }

    public static DynamoDBEventDao instance() {

        if (instance == null) {
            synchronized(DynamoDBEventDao.class) {
                if (instance == null)
                    instance = new DynamoDBEventDao();
            }
        }
        return instance;
    }

    @Override
    public List<Event> findAllEvents() {
        return mapper.scan(Event.class, new DynamoDBScanExpression());
    }

    @Override
    public List<Event> findEventsByCity(String city) {

        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":v1", new AttributeValue().withS(city));

        DynamoDBQueryExpression<Event> query = new DynamoDBQueryExpression<Event>()
                                                    .withIndexName(Event.CITY_INDEX)
                                                    .withConsistentRead(false)
                                                    .withKeyConditionExpression("city = :v1")
                                                    .withExpressionAttributeValues(eav);

        return mapper.query(Event.class, query);


        // NOTE:  without an index, this query would require a full table scan with a filter:
        /*
         DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                                                    .withFilterExpression("city = :val1")
                                                    .withExpressionAttributeValues(eav);

         return mapper.scan(Event.class, scanExpression);
        */

    }

    @Override
    public List<Event> findEventsByTeam(String team) {

        DynamoDBQueryExpression<Event> homeQuery = new DynamoDBQueryExpression<>();
        Event eventKey = new Event();
        eventKey.setHomeTeam(team);
        homeQuery.setHashKeyValues(eventKey);
        List<Event> homeEvents = mapper.query(Event.class, homeQuery);

        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":v1", new AttributeValue().withS(team));
        DynamoDBQueryExpression<Event> awayQuery = new DynamoDBQueryExpression<Event>()
                                                        .withIndexName(Event.AWAY_TEAM_INDEX)
                                                        .withConsistentRead(false)
                                                        .withKeyConditionExpression("awayTeam = :v1")
                                                        .withExpressionAttributeValues(eav);

        List<Event> awayEvents = mapper.query(Event.class, awayQuery);

        // need to create a new list because PaginatedList from query is immutable
        List<Event> allEvents = new LinkedList<>();
        allEvents.addAll(homeEvents);
        allEvents.addAll(awayEvents);
        allEvents.sort( (e1, e2) -> e1.getEventDate() <= e2.getEventDate() ? -1 : 1 );

        return allEvents;
    }

    @Override
    public Optional<Event> findEventByTeamAndDate(String team, Long eventDate) {

        Event event = mapper.load(Event.class, team, eventDate);

        return Optional.ofNullable(event);
    }

    @Override
    public void saveOrUpdateEvent(Event event) {

        mapper.save(event);
    }

    @Override
    public void deleteEvent(String team, Long eventDate) {

        Optional<Event> oEvent = findEventByTeamAndDate(team, eventDate);
        if (oEvent.isPresent()) {
            mapper.delete(oEvent.get());
        }
        else {
            log.error("Could not delete event, no such team and date combination");
            throw new IllegalArgumentException("Delete failed for nonexistent event");
        }
    }
}

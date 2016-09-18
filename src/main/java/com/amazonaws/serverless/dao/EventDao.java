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


import com.amazonaws.serverless.domain.Event;

import java.util.List;
import java.util.Optional;


public interface EventDao {

    List<Event> findAllEvents();

    List<Event> findEventsByCity(String city);

    List<Event> findEventsByTeam(String team);

    Optional<Event> findEventByTeamAndDate(String team, Long eventDate);

    void saveOrUpdateEvent(Event event);

    void deleteEvent(String team, Long eventDate);

}

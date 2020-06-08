// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;

public final class FindMeetingQuery {
  private static boolean anyAttending(Event event, Collection<String> people) {
    for (String event_attendee : event.getAttendees()) {
      for (String person : people) {
          if (person.equals(event_attendee)) {
            return true;
          }
      }
    }

    return false;
  }

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    return queryHelper(events, request, true);
  }

  public Collection<TimeRange> queryHelper(Collection<Event> events, MeetingRequest request, boolean try_optionals) {
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) return Arrays.asList();

    Collection<TimeRange> available = Arrays.asList(TimeRange.WHOLE_DAY);
    Collection<String> request_attendees = new ArrayList(request.getAttendees());
    if (try_optionals) request_attendees.addAll(request.getOptionalAttendees());

    for (Event event : events) {
      if (!anyAttending(event, request_attendees)) continue;

      TimeRange event_time = event.getWhen();
      Collection<TimeRange> new_available = new ArrayList<TimeRange>();
      for (TimeRange time_available : available) {
        if (!event_time.overlaps(time_available)) {
          new_available.add(time_available);
          continue;
        }
        
        if (time_available.start() < event_time.start()) {
          TimeRange difference =
            TimeRange.fromStartEnd(time_available.start(), event_time.start(), false);
          if (difference.duration() >= request.getDuration()) new_available.add(difference);
        }

        if (time_available.end() > event_time.end()) {
          TimeRange difference =
            TimeRange.fromStartEnd(event_time.end(), time_available.end(), false);
          if (difference.duration() >= request.getDuration()) new_available.add(difference);
        }
      }
      available = new_available;

      if (available.size() == 0) {
        if (try_optionals && (request.getAttendees().size() > 0)) {
          return queryHelper(events, request, false); 
        }
        else {
            return Arrays.asList();
        }
      }
    }

    return available;
  }
}

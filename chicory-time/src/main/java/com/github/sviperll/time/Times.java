/*
 * Copyright (c) 2013, Victor Nazarov <asviraspossible@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Victor Nazarov nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.github.sviperll.time;

import static com.github.sviperll.time.Month.APRIL;
import static com.github.sviperll.time.Month.AUGUST;
import static com.github.sviperll.time.Month.DECEMBER;
import static com.github.sviperll.time.Month.FEBRUARY;
import static com.github.sviperll.time.Month.JANUARY;
import static com.github.sviperll.time.Month.JULY;
import static com.github.sviperll.time.Month.JUNE;
import static com.github.sviperll.time.Month.MARCH;
import static com.github.sviperll.time.Month.MAY;
import static com.github.sviperll.time.Month.NOVEMBER;
import static com.github.sviperll.time.Month.OCTOBER;
import static com.github.sviperll.time.Month.SEPTEMBER;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Times {
    public static final TimeZoneOffset GMT_OFFSET = new TimeZoneOffset(0);

    public static HumanTime getHumanTime(UnixTime instant, TimeZoneOffset offset) {
        Calendar calendar = createCalendarInstance(offset);
        calendar.setTimeInMillis(instant.millis());
        return getHumanTime(calendar, offset);
    }

    public static Day createDay(int year, Month month, int day) {
        GregorianCalendar calendar = createCalendarInstanceInitializedWith(GMT_OFFSET, year, month, day);
        WeekDay weekDay = getWeekDay(calendar);
        return new Day(year, new YearDay(month, day), weekDay);
    }

    private static GregorianCalendar createCalendarInstance(TimeZoneOffset offset) {
        long hours = offset.minutes() / 60;
        long minutes = offset.minutes() % 60;
        if (minutes < 0) {
            minutes += 60;
            hours -= 1;
        }
        Formatter formatter = new Formatter();
        formatter.format("GMT%+03d%02d", hours, minutes);
        TimeZone timeZone = TimeZone.getTimeZone(formatter.toString());
        GregorianCalendar calendar = new GregorianCalendar(timeZone);
        calendar.clear();
        return calendar;
    }

    private static HumanTime getHumanTime(Calendar calendar, TimeZoneOffset offset) {
        int millis = calendar.get(Calendar.MILLISECOND);
        int seconds = calendar.get(Calendar.SECOND);
        int minutes = calendar.get(Calendar.MINUTE);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int monthDay = calendar.get(Calendar.DAY_OF_MONTH);
        int monthIndex = calendar.get(Calendar.MONTH);
        Month month;
        switch (monthIndex) {
            case Calendar.JANUARY:
                month = Month.JANUARY;
                break;
            case Calendar.FEBRUARY:
                month = Month.FEBRUARY;
                break;
            case Calendar.MARCH:
                month = Month.MARCH;
                break;
            case Calendar.APRIL:
                month = Month.APRIL;
                break;
            case Calendar.MAY:
                month = Month.MAY;
                break;
            case Calendar.JUNE:
                month = Month.JUNE;
                break;
            case Calendar.JULY:
                month = Month.JULY;
                break;
            case Calendar.AUGUST:
                month = Month.AUGUST;
                break;
            case Calendar.SEPTEMBER:
                month = Month.SEPTEMBER;
                break;
            case Calendar.OCTOBER:
                month = Month.OCTOBER;
                break;
            case Calendar.NOVEMBER:
                month = Month.NOVEMBER;
                break;
            case Calendar.DECEMBER:
                month = Month.DECEMBER;
                break;
            default:
                throw new IllegalStateException("Wrong month value got from GregorianCalendar: " + monthIndex);
        }
        WeekDay weekDay = getWeekDay(calendar);
        int year = calendar.get(Calendar.YEAR);
        ClockTime clockTime = new ClockTime(hours, minutes, seconds, millis);
        YearDay yearDay = new YearDay(month, monthDay);
        Day day = new Day(year, yearDay, weekDay);
        return new HumanTime(day, clockTime, offset);
    }

    private static WeekDay getWeekDay(Calendar calendar) throws IllegalStateException {
        int weekDayIndex = calendar.get(Calendar.DAY_OF_WEEK);
        switch (weekDayIndex) {
            case Calendar.SUNDAY:
                return WeekDay.SUNDAY;
            case Calendar.MONDAY:
                return WeekDay.MONDAY;
            case Calendar.TUESDAY:
                return WeekDay.TUESDAY;
            case Calendar.WEDNESDAY:
                return WeekDay.WEDNESDAY;
            case Calendar.THURSDAY:
                return WeekDay.THURSDAY;
            case Calendar.FRIDAY:
                return WeekDay.FRIDAY;
            case Calendar.SATURDAY:
                return WeekDay.SATURDAY;
            default:
                throw new IllegalStateException("Wrong weekDay value got from GregorianCalendar: " + weekDayIndex);
        }
    }

    private static GregorianCalendar createCalendarInstanceInitializedWith(TimeZoneOffset offset, int year, Month month, int day) {
        GregorianCalendar calendar = createCalendarInstance(offset);
        int monthIndex;
        switch (month) {
            case JANUARY:
                monthIndex = Calendar.JANUARY;
                break;
            case FEBRUARY:
                monthIndex = Calendar.FEBRUARY;
                break;
            case MARCH:
                monthIndex = Calendar.MARCH;
                break;
            case APRIL:
                monthIndex = Calendar.APRIL;
                break;
            case MAY:
                monthIndex = Calendar.MAY;
                break;
            case JUNE:
                monthIndex = Calendar.JUNE;
                break;
            case JULY:
                monthIndex = Calendar.JULY;
                break;
            case AUGUST:
                monthIndex = Calendar.AUGUST;
                break;
            case SEPTEMBER:
                monthIndex = Calendar.SEPTEMBER;
                break;
            case OCTOBER:
                monthIndex = Calendar.OCTOBER;
                break;
            case NOVEMBER:
                monthIndex = Calendar.NOVEMBER;
                break;
            case DECEMBER:
                monthIndex = Calendar.DECEMBER;
                break;
            default:
                throw new IllegalStateException("Unsupported month value: " + month);
        }
        calendar.set(year, monthIndex, day);
        return calendar;
    }

    private static GregorianCalendar createCalendarInstanceInitializedWith(HumanTime humanTime) {
        GregorianCalendar calendar = createCalendarInstanceInitializedWith(humanTime.offset(), humanTime.day().year(), humanTime.day().yearDay().month(), humanTime.day().yearDay().monthDay());
        calendar.set(Calendar.HOUR_OF_DAY, humanTime.clockTime().hour());
        calendar.set(Calendar.MINUTE, humanTime.clockTime().minute());
        calendar.set(Calendar.SECOND, humanTime.clockTime().second());
        calendar.set(Calendar.MILLISECOND, humanTime.clockTime().millis());
        return calendar;
    }

    public static HumanTime endOfHour(HumanTime time) {
        GregorianCalendar calendar = createCalendarInstanceInitializedWith(time);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return getHumanTime(calendar, time.offset());
    }

    public static HumanTime startOfHour(HumanTime time) {
        GregorianCalendar calendar = createCalendarInstanceInitializedWith(time);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return getHumanTime(calendar, time.offset());
    }

    public static HumanTime endOfDay(HumanTime time) {
        GregorianCalendar calendar = createCalendarInstanceInitializedWith(time);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return getHumanTime(calendar, time.offset());
    }
    
    public static HumanTime startOfDay(HumanTime time) {
        GregorianCalendar calendar = createCalendarInstanceInitializedWith(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return getHumanTime(calendar, time.offset());
    }

    public static HumanTime endOfMonth(HumanTime time) {
        GregorianCalendar calendar = createCalendarInstanceInitializedWith(startOfMonth(time));

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);

        return getHumanTime(calendar, time.offset());
    }

    public static HumanTime startOfMonth(HumanTime time) {
        GregorianCalendar calendar = createCalendarInstanceInitializedWith(time);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return getHumanTime(calendar, time.offset());
    }

    public static UnixTime getUnixTime(HumanTime humanTime) {
        GregorianCalendar calendar = createCalendarInstanceInitializedWith(humanTime);
        return new UnixTime(calendar.getTimeInMillis());
    }

    public static UnixTime getCurrentTime() {
        return new UnixTime(System.currentTimeMillis());
    }

    public static TimeZoneOffset defaultCurrentOffset() {
        return new TimeZoneOffset(TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 60000);
    }

    public static TimeResultSet resultSet(ResultSet resultSet) {
        return new TimeResultSet(resultSet);
    }

    public static TimePreparedStatement preparedStatement(PreparedStatement statement) {
        return new TimePreparedStatement(statement);
    }

    private Times() {
    }

    public static class TimeResultSet {
        private final ResultSet resultSet;
        TimeResultSet(ResultSet resultSet) {
            this.resultSet = resultSet;
        }

        public UnixTime getUnixTime(String columnLabel, TimeZoneOffset offset) throws SQLException {
            Calendar calendar = createCalendarInstance(offset);
            Timestamp timestamp = resultSet.getTimestamp(columnLabel, calendar);
            return timestamp == null ? null : new UnixTime(timestamp.getTime());
        }

        public UnixTime getUnixTime(int index, TimeZoneOffset offset) throws SQLException {
            Calendar calendar = createCalendarInstance(offset);
            Timestamp timestamp = resultSet.getTimestamp(index, calendar);
            return timestamp == null ? null : new UnixTime(timestamp.getTime());
        }
    }

    public static class TimePreparedStatement {
        private final PreparedStatement statement;

        private TimePreparedStatement(PreparedStatement statement) {
            this.statement = statement;
        }

        public void setUnixTime(int parameterIndex, UnixTime time, TimeZoneOffset offset) throws SQLException {
            if (time == null)
                statement.setNull(parameterIndex, java.sql.Types.TIMESTAMP);
            else {
                Calendar calendar = createCalendarInstance(offset);
                Timestamp timestamp = new Timestamp(time.millis());
                statement.setTimestamp(parameterIndex, timestamp, calendar);
            }
        }
    }
}

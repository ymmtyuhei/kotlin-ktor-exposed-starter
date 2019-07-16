package web

import com.google.api.client.auth.oauth2.BearerToken
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.http.BasicAuthentication
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventAttendee
import org.junit.jupiter.api.Test
import java.lang.NullPointerException


/**
 *
 *
 * 参考サンプルプロジェクト
 * https://developers.google.com/calendar/quickstart/java
 */
class CalendarAPITest : AuthCodeTest(){

    private var client: com.google.api.services.calendar.Calendar? = null
    private val dataStoreFactory: FileDataStoreFactory? = null

    // private val calendarID = "primart" //primary指定カレンダー
    private val CALENDAR_ID = "xxxx@group.calendar.google.com" // テストカレンダー

    /**
     * リフレッシュトークンからトークンを取得
     */
    @Test
    fun requestCalendars(){
        val tokens = refreshAccessToken(REFRESH_TOKEN)
        val credential = createCredentialWithAccessTokenOnly(
                NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                tokens
        )

        // set up global Calendar instance
        client = com.google.api.services.calendar.Calendar.Builder(
                NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("myApplication").build()

        val mClient= client ?: throw NullPointerException("failed to client initialization.")

        val list = mClient.calendarList()?.list()?.execute()
        println("calendar list : $list")

        val atendees = todaysAtendees(mClient)
        for (it in atendees){
            val events = eventOfAtendee( mClient, it.email )
            for (it in events){
                print("found attendee entry event.\nsummary : ${it.summary} \nstart date : ${it.start.date}")
            }
        }
        /**
         *
        summary : だれだれさんの結婚式
        start date : 2019-07-01found attendee entry event.
        summary : なんとかさんの結婚式
        start date : 2019-07-02found attendee entry event.
        summary : テストイベント
         *
         */

    }

    /**
     * @return 最初に見つかったイベントの参加者一覧
     */
    fun todaysAtendees(mClient: com.google.api.services.calendar.Calendar) : List<EventAttendee> {
        // List the next 10 events from the primary calendar.
        val now = DateTime(System.currentTimeMillis())
        val events = mClient.events().list(CALENDAR_ID)
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute()
        val items = events.items
        if (items.isEmpty()) {
            println("No upcoming events found.")
        } else {
            println("Upcoming events")
            for (event in items) {
                var start = event.getStart().getDateTime()
                if (start == null) {
                    start = event.getStart().getDate()
                }
                println("event : $event")
                val atendees = event.attendees
                for (it in atendees){
                    it.email
                }
                return event.attendees
            }
        }

        throw Error("failed to find event.")
    }


    fun eventOfAtendee( mClient: com.google.api.services.calendar.Calendar, emailAddress:String):List<Event>{
        val events = mClient.events().list(CALENDAR_ID)
                .setMaxResults(10)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setQ(emailAddress)
                .execute()
        return events.items
    }


    fun createCredentialWithAccessTokenOnly(
            transport: HttpTransport, jsonFactory: JsonFactory, tokenResponse: TokenResponse): Credential {
        return Credential(
                BearerToken.authorizationHeaderAccessMethod()
        ).setFromTokenResponse(tokenResponse)
    }

    fun createCredentialWithRefreshToken(
            transport: HttpTransport,
            jsonFactory: JsonFactory,
            tokenResponse: TokenResponse): Credential {
        return Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setTransport(transport)
                .setJsonFactory(jsonFactory)
                .setTokenServerUrl(GenericUrl("https://server.example.com/token"))
                .setClientAuthentication(BasicAuthentication("s6BhdRkqt3", "7Fjfp0ZBr1KtDRbnfVdmIw"))
                .build()
                .setFromTokenResponse(tokenResponse)
    }

}
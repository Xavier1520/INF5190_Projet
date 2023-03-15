package com.inf5190.chat.messages;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.inf5190.chat.auth.model.LoginRequest;
import com.inf5190.chat.auth.model.LoginResponse;
import com.inf5190.chat.messages.model.Message;
import com.inf5190.chat.messages.repository.FirestoreMessage;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@PropertySource("classpath:firebase.properties")
public class ITestMessageController {
    private final FirestoreMessage message1 = new FirestoreMessage("u1", Timestamp.now(), "t1", null);
    private final FirestoreMessage message2 = new FirestoreMessage("u2", Timestamp.now(), "t2", null);


    @Value("${firebase.project.id}")
    private String firebaseProjectId;

    @Value("${firebase.emulator.port}")
    private String emulatorPort;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Firestore firestore;

    private String messagesEndpointUrl;
    private String loginEndpointUrl;

    @BeforeAll
    public static void checkRunAgainstEmulator() {
        checkEmulators();
    }

    @BeforeEach
    public void setup() throws InterruptedException, ExecutionException {
        this.messagesEndpointUrl = "http://localhost:" + port + "/messages";
        this.loginEndpointUrl = "http://localhost:" + port + "/auth/login";

        // Pour ajouter deux message dans firestore au début de chaque test.
        this.firestore.collection("messages").document("1").create(this.message1).get();
        this.firestore.collection("messages").document("2").create(this.message2).get();
    }

    @AfterEach
    public void testDown() {
        // Pour effacer le contenu de l'émulateur entre chaque test.
        this.restTemplate.delete(
                "http://localhost:" + this.emulatorPort + "/emulator/v1/projects/"
                        + this.firebaseProjectId
                        + "/databases/(default)/documents");
    }

    @Test
    public void getMessageNotLoggedIn() {
        ResponseEntity<String> response = this.restTemplate.getForEntity(this.messagesEndpointUrl,
                String.class);

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void getMessages() {
        final String token = this.login();

        final HttpHeaders header = new HttpHeaders();
        header.add("Authorization", "Bearer " + token);

        final HttpEntity<Object> headers = new HttpEntity<Object>(header);
        final ResponseEntity<Message[]> response = this.restTemplate.exchange(this.messagesEndpointUrl,
                HttpMethod.GET, headers, Message[].class);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        
        // Valider les messages...
        final Message[] messages = response.getBody();
        final Timestamp timestamp1 = message1.getTimestamp();
        final Date date1 = timestamp1.toDate();
        final Long time1 = date1.getTime();
        final Timestamp timestamp2 = message2.getTimestamp();
        final Date date2 = timestamp2.toDate();
        final Long time2 = date2.getTime();
        
        assertThat(messages[0].username()).isEqualTo(message1.getUsername());
        assertThat(messages[1].username()).isEqualTo(message2.getUsername());
        assertThat(messages[0].timestamp()).isEqualTo(time1);
        assertThat(messages[1].timestamp()).isEqualTo(time2);
        assertThat(messages[0].text()).isEqualTo(message1.getText());
        assertThat(messages[1].text()).isEqualTo(message2.getText());
        assertThat(messages[0].imageUrl()).isEqualTo(message1.getImageUrl());
        assertThat(messages[1].imageUrl()).isEqualTo(message2.getImageUrl());
    }

    @Test
    public void getMessagesInvalidToken() {
        final String token = "token";
        final HttpHeaders header = new HttpHeaders();
        header.add("Authorization", "Bearer " + token);

        final HttpEntity<Object> headers = new HttpEntity<Object>(header);
        final ResponseEntity<String> response = this.restTemplate.exchange(this.messagesEndpointUrl,
                HttpMethod.GET, headers, String.class);

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void postMessagesInvalidToken() {
        final String token = "token";
        final HttpHeaders header = new HttpHeaders();
        header.add("Authorization", "Bearer " + token);

        final HttpEntity<Object> headers = new HttpEntity<Object>(header);
        final ResponseEntity<String> response = this.restTemplate.exchange(this.messagesEndpointUrl,
                HttpMethod.POST, headers, String.class);

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void getMessagesFromId() {
        final int expectedLength = 1;
        final String token = this.login();

        final HttpHeaders header = new HttpHeaders();
        header.add("Authorization", "Bearer " + token);

        final String request = this.messagesEndpointUrl + "?fromId=1";
        final HttpEntity<Object> headers = new HttpEntity<Object>(header);
        final ResponseEntity<Message[]> response = this.restTemplate.exchange(request,
                HttpMethod.GET, headers, Message[].class);
        
        assertThat(response.getStatusCodeValue()).isEqualTo(200);

        final Message[] messages = response.getBody();

        assertThat(messages.length).isEqualTo(expectedLength);
    }

    @Test
    public void getMessagesOverLimit() throws InterruptedException, ExecutionException {
        final int expectedLength = 20;
        
        final FirestoreMessage message3 = new FirestoreMessage("u3", Timestamp.now(), "t3", null);
        final FirestoreMessage message4 = new FirestoreMessage("u4", Timestamp.now(), "t4", null);
        final FirestoreMessage message5 = new FirestoreMessage("u5", Timestamp.now(), "t5", null);
        final FirestoreMessage message6 = new FirestoreMessage("u6", Timestamp.now(), "t6", null);
        final FirestoreMessage message7 = new FirestoreMessage("u7", Timestamp.now(), "t7", null);
        final FirestoreMessage message8 = new FirestoreMessage("u8", Timestamp.now(), "t8", null);
        final FirestoreMessage message9 = new FirestoreMessage("u9", Timestamp.now(), "t9", null);
        final FirestoreMessage message10 = new FirestoreMessage("u10", Timestamp.now(), "t10", null);
        final FirestoreMessage message11 = new FirestoreMessage("u11", Timestamp.now(), "t11", null);
        final FirestoreMessage message12 = new FirestoreMessage("u12", Timestamp.now(), "t12", null);
        final FirestoreMessage message13 = new FirestoreMessage("u13", Timestamp.now(), "t13", null);
        final FirestoreMessage message14 = new FirestoreMessage("u14", Timestamp.now(), "t14", null);
        final FirestoreMessage message15 = new FirestoreMessage("u15", Timestamp.now(), "t15", null);
        final FirestoreMessage message16 = new FirestoreMessage("u16", Timestamp.now(), "t16", null);
        final FirestoreMessage message17 = new FirestoreMessage("u17", Timestamp.now(), "t17", null);
        final FirestoreMessage message18 = new FirestoreMessage("u18", Timestamp.now(), "t18", null);
        final FirestoreMessage message19 = new FirestoreMessage("u19", Timestamp.now(), "t19", null);
        final FirestoreMessage message20 = new FirestoreMessage("u20", Timestamp.now(), "t20", null);
        final FirestoreMessage message21 = new FirestoreMessage("u21", Timestamp.now(), "t21", null);
        final FirestoreMessage message22 = new FirestoreMessage("u22", Timestamp.now(), "t22", null);
        
        this.firestore.collection("messages").document("3").create(message3).get();
        this.firestore.collection("messages").document("4").create(message4).get();
        this.firestore.collection("messages").document("5").create(message5).get();
        this.firestore.collection("messages").document("6").create(message6).get();
        this.firestore.collection("messages").document("7").create(message7).get();
        this.firestore.collection("messages").document("8").create(message8).get();
        this.firestore.collection("messages").document("9").create(message9).get();
        this.firestore.collection("messages").document("10").create(message10).get();
        this.firestore.collection("messages").document("11").create(message11).get();
        this.firestore.collection("messages").document("12").create(message12).get();
        this.firestore.collection("messages").document("13").create(message13).get();
        this.firestore.collection("messages").document("14").create(message14).get();
        this.firestore.collection("messages").document("15").create(message15).get();
        this.firestore.collection("messages").document("16").create(message16).get();
        this.firestore.collection("messages").document("17").create(message17).get();
        this.firestore.collection("messages").document("18").create(message18).get();
        this.firestore.collection("messages").document("19").create(message19).get();
        this.firestore.collection("messages").document("20").create(message20).get();
        this.firestore.collection("messages").document("21").create(message21).get();
        this.firestore.collection("messages").document("22").create(message22).get();

        final String token = this.login();

        final HttpHeaders header = new HttpHeaders();
        header.add("Authorization", "Bearer " + token);

        final HttpEntity<Object> headers = new HttpEntity<Object>(header);
        final ResponseEntity<Message[]> response = this.restTemplate.exchange(this.messagesEndpointUrl,
                HttpMethod.GET, headers, Message[].class);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        
        final Message[] messages = response.getBody();

        assertThat(messages.length).isEqualTo(expectedLength);
    }

    @Test
    public void getMessagesInvalidFromId() {
        final String token = this.login();

        final HttpHeaders header = new HttpHeaders();
        header.add("Authorization", "Bearer " + token);

        final String id = "invalid";
        final String request = this.messagesEndpointUrl + "?fromId=" + id;
        final HttpEntity<Object> headers = new HttpEntity<Object>(header);
        final ResponseEntity<String> response = this.restTemplate.exchange(request,
                HttpMethod.GET, headers, String.class);
        
        assertThat(response.getStatusCodeValue()).isEqualTo(500);
    }

    private String login() {
        LoginResponse response = this.restTemplate.postForObject(this.loginEndpointUrl,
                new LoginRequest("username", "password"),
                LoginResponse.class);

        return response.token();
    }

    private static void checkEmulators() {
        final String firebaseEmulator = System.getenv().get("FIRESTORE_EMULATOR_HOST");
        if (firebaseEmulator == null || firebaseEmulator.length() == 0) {
            System.err.println(
                    "**********************************************************************************************************");
            System.err.println(
                    "******** You need to set FIRESTORE_EMULATOR_HOST=localhost:8181 in your system properties. ********");
            System.err.println(
                    "**********************************************************************************************************");
        }
        assertThat(firebaseEmulator).as(
                "You need to set FIRESTORE_EMULATOR_HOST=localhost:8181 in your system properties.")
                .isNotEmpty();
        final String storageEmulator = System.getenv().get("FIREBASE_STORAGE_EMULATOR_HOST");
        if (storageEmulator == null || storageEmulator.length() == 0) {
            System.err.println(
                    "**********************************************************************************************************");
            System.err.println(
                    "******** You need to set FIREBASE_STORAGE_EMULATOR_HOST=localhost:9199 in your system properties. ********");
            System.err.println(
                    "**********************************************************************************************************");
        }
        assertThat(storageEmulator).as(
                "You need to set FIREBASE_STORAGE_EMULATOR_HOST=localhost:9199 in your system properties.")
                .isNotEmpty();
    }
}
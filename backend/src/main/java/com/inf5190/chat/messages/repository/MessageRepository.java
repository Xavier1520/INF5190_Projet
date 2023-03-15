package com.inf5190.chat.messages.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Bucket.BlobTargetOption;
import com.google.cloud.storage.Storage.PredefinedAcl;
import com.google.firebase.cloud.StorageClient;
import com.inf5190.chat.messages.model.Message;
import com.inf5190.chat.messages.model.MessageRequest;

import io.jsonwebtoken.io.Decoders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class MessageRepository {
    private static final String COLLECTION_NAME = "messages";
    
    @Autowired
    @Qualifier("storageBucketName")
    private String storageBucketName;
    
    private Firestore firestore;
    private StorageClient storageClient;

    public MessageRepository(Firestore firestore, StorageClient storageClient) {
        this.firestore = firestore;
        this.storageClient = storageClient;
    }

    public List<Message> getMessages(Optional<String> fromId) throws InterruptedException, ExecutionException, TimeoutException {
        List<Message> messages = new ArrayList<Message>();
        ApiFuture<QuerySnapshot> future;
        if (fromId.isPresent()) {
            String id = fromId.get();
            if (id != null) {
                ApiFuture<DocumentSnapshot> futureSnapshot = firestore.collection(COLLECTION_NAME).document(id).get();
                DocumentSnapshot snapshot = futureSnapshot.get(30, TimeUnit.SECONDS);
                snapshot.exists();
                future = firestore.collection(COLLECTION_NAME).orderBy("timestamp").startAfter(snapshot).get();
            } else {
                future = firestore.collection(COLLECTION_NAME).orderBy("timestamp").limitToLast(20).get();
            }
        } else {
            future = firestore.collection(COLLECTION_NAME).orderBy("timestamp").limitToLast(20).get();
        }
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        for (QueryDocumentSnapshot document : documents) {
            String id = document.getId();
            FirestoreMessage firestoreMessage = document.toObject(FirestoreMessage.class);
            String username = firestoreMessage.getUsername();
            Timestamp timestamp = firestoreMessage.getTimestamp();
            Long time = timestamp.toDate().getTime();
            String text = firestoreMessage.getText();
            String imageUrl = firestoreMessage.getImageUrl();
            Message message = new Message(id, username, time, text, imageUrl);
            messages.add(message);
        }
        return messages;
    }

    public Message createMessage(MessageRequest message) {
        DocumentReference messageReference = firestore.collection(COLLECTION_NAME).document();
        
        String username = message.username();
        Timestamp timestamp = Timestamp.now();
        String text = message.text();
        
        String imageUrl = null;
        if (message.imageData() != null) {
            Bucket b = storageClient.bucket(this.storageBucketName);
            String path = String.format("images/%s.%s", messageReference.getId(), message.imageData().type());
            b.create(path, Decoders.BASE64.decode(message.imageData().data()), BlobTargetOption.predefinedAcl(PredefinedAcl.PUBLIC_READ));
            imageUrl = String.format("https://storage.googleapis.com/%s/%s", this.storageBucketName, path);
        }
        
        FirestoreMessage firestoreMessage = new FirestoreMessage(username, timestamp, text, imageUrl);
        messageReference.set(firestoreMessage);

        final String id = messageReference.getId();
        // Long time = timestamp.getSeconds() * 1000;
        Long time = timestamp.toDate().getTime();
        final Message newMessage = new Message(id, username, time, text, imageUrl);
        return newMessage;
    }

}

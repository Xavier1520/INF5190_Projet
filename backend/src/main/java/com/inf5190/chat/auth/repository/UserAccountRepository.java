package com.inf5190.chat.auth.repository;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

@Repository
public class UserAccountRepository {
    private static final String COLLECTION_NAME = "userAccounts";
    private Firestore firestore;
    
    public UserAccountRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public FirestoreUserAccount getUserAccount(String username) throws InterruptedException, ExecutionException {
        if (username != null) {
            DocumentReference reference = firestore.collection(COLLECTION_NAME).document(username);
            ApiFuture<DocumentSnapshot> future = reference.get();
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                FirestoreUserAccount account = document.toObject(FirestoreUserAccount.class);
                return account;
            } else {
                return null;
            }
        } else {
            throw new UnsupportedOperationException("A faire");
        }
    }

    public void setUserAccount(FirestoreUserAccount userAccount) throws InterruptedException, ExecutionException {
        String id = userAccount.getUsername();
        if (id != null) {
            firestore.collection(COLLECTION_NAME).document(id).set(userAccount);
        } else {
            throw new UnsupportedOperationException("A faire");
        }
    }
}
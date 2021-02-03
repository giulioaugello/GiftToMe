package com.LAM.GiftToMe.FCMFirebase;

public interface FirestoreCheckName {

    //usato per controllare se il regalo già esiste e se esiste già una chat
    void onReceiverRetrieve(boolean exist);

}

package com.example.tutora.data;

import com.google.firebase.firestore.FirebaseFirestore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class ChatRepositoryImpl_Factory implements Factory<ChatRepositoryImpl> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  private ChatRepositoryImpl_Factory(Provider<FirebaseFirestore> firestoreProvider) {
    this.firestoreProvider = firestoreProvider;
  }

  @Override
  public ChatRepositoryImpl get() {
    return newInstance(firestoreProvider.get());
  }

  public static ChatRepositoryImpl_Factory create(Provider<FirebaseFirestore> firestoreProvider) {
    return new ChatRepositoryImpl_Factory(firestoreProvider);
  }

  public static ChatRepositoryImpl newInstance(FirebaseFirestore firestore) {
    return new ChatRepositoryImpl(firestore);
  }
}

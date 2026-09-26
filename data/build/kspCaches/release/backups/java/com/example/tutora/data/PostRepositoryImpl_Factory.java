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
public final class PostRepositoryImpl_Factory implements Factory<PostRepositoryImpl> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  private PostRepositoryImpl_Factory(Provider<FirebaseFirestore> firestoreProvider) {
    this.firestoreProvider = firestoreProvider;
  }

  @Override
  public PostRepositoryImpl get() {
    return newInstance(firestoreProvider.get());
  }

  public static PostRepositoryImpl_Factory create(Provider<FirebaseFirestore> firestoreProvider) {
    return new PostRepositoryImpl_Factory(firestoreProvider);
  }

  public static PostRepositoryImpl newInstance(FirebaseFirestore firestore) {
    return new PostRepositoryImpl(firestore);
  }
}

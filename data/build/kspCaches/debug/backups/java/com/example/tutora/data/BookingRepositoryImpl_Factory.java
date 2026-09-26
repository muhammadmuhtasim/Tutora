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
public final class BookingRepositoryImpl_Factory implements Factory<BookingRepositoryImpl> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  private BookingRepositoryImpl_Factory(Provider<FirebaseFirestore> firestoreProvider) {
    this.firestoreProvider = firestoreProvider;
  }

  @Override
  public BookingRepositoryImpl get() {
    return newInstance(firestoreProvider.get());
  }

  public static BookingRepositoryImpl_Factory create(
      Provider<FirebaseFirestore> firestoreProvider) {
    return new BookingRepositoryImpl_Factory(firestoreProvider);
  }

  public static BookingRepositoryImpl newInstance(FirebaseFirestore firestore) {
    return new BookingRepositoryImpl(firestore);
  }
}

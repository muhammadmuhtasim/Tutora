package com.example.tutora.data;

import android.content.Context;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AuthRepositoryImpl_Factory implements Factory<AuthRepositoryImpl> {
  private final Provider<FirebaseAuth> authProvider;

  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<Context> contextProvider;

  private AuthRepositoryImpl_Factory(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseFirestore> firestoreProvider, Provider<Context> contextProvider) {
    this.authProvider = authProvider;
    this.firestoreProvider = firestoreProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public AuthRepositoryImpl get() {
    return newInstance(authProvider.get(), firestoreProvider.get(), contextProvider.get());
  }

  public static AuthRepositoryImpl_Factory create(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseFirestore> firestoreProvider, Provider<Context> contextProvider) {
    return new AuthRepositoryImpl_Factory(authProvider, firestoreProvider, contextProvider);
  }

  public static AuthRepositoryImpl newInstance(FirebaseAuth auth, FirebaseFirestore firestore,
      Context context) {
    return new AuthRepositoryImpl(auth, firestore, context);
  }
}

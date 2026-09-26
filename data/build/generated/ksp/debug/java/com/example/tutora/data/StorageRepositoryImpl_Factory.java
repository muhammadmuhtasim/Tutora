package com.example.tutora.data;

import com.google.firebase.database.FirebaseDatabase;
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
public final class StorageRepositoryImpl_Factory implements Factory<StorageRepositoryImpl> {
  private final Provider<FirebaseDatabase> databaseProvider;

  private StorageRepositoryImpl_Factory(Provider<FirebaseDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public StorageRepositoryImpl get() {
    return newInstance(databaseProvider.get());
  }

  public static StorageRepositoryImpl_Factory create(Provider<FirebaseDatabase> databaseProvider) {
    return new StorageRepositoryImpl_Factory(databaseProvider);
  }

  public static StorageRepositoryImpl newInstance(FirebaseDatabase database) {
    return new StorageRepositoryImpl(database);
  }
}

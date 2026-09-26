package com.example.tutora.data;

import com.google.firebase.storage.FirebaseStorage;
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
  private final Provider<FirebaseStorage> storageProvider;

  private StorageRepositoryImpl_Factory(Provider<FirebaseStorage> storageProvider) {
    this.storageProvider = storageProvider;
  }

  @Override
  public StorageRepositoryImpl get() {
    return newInstance(storageProvider.get());
  }

  public static StorageRepositoryImpl_Factory create(Provider<FirebaseStorage> storageProvider) {
    return new StorageRepositoryImpl_Factory(storageProvider);
  }

  public static StorageRepositoryImpl newInstance(FirebaseStorage storage) {
    return new StorageRepositoryImpl(storage);
  }
}

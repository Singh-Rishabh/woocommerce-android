-dontobfuscate

###### OkHttp - begin
-dontwarn okio.**
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }

-keepattributes Signature
-keepattributes *Annotation*
###### OkHttp - end

###### Event Bus 3
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
###### Event Bus 3 - end

###### Event Bus 2 - begin
-keepclassmembers class ** {
    public void onEvent*(**);
}

# Only required if you use AsyncExecutor
-keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
    ** *(java.lang.Throwable);
}
###### Event Bus 2 - end

##### WooCommerce (this is needed for Json deserializers, but generally, we should keep our own classes) - begin
-keep class com.cataloghub.** { *; }
##### WooCommerce - end

###### FluxC (was needed for Json deserializers) - begin
-keep class org.wordpress.android.fluxc** { *; }
###### FluxC - end

###### FluxC - WellSql (needed for Addon support) - begin
-keep class com.wellsql** { *; }
###### FluxC - end

###### Dagger - begin
-dontwarn com.google.errorprone.annotations.*
###### Dagger - end

-keep class com.google.common.** { *; }
-dontwarn com.google.common.**

###### Zendesk - begin
-keep class com.zendesk.** { *; }
-keep class zendesk.** { *; }
-keep class javax.inject.Provider
-keep class com.squareup.picasso.** { *; }
-keep class com.jakewharton.disklrucache.** { *; }
-keep class com.google.gson.** { *; }
-keep class okio.** { *; }
-keep class retrofit2.** { *; }
-keep class uk.co.senab.photoview.** { *; }
###### Zendesk - end

###### Encrypted Logs - begin
-dontwarn java.awt.*
-keep class com.sun.jna.* { *; }
-keepclassmembers class * extends com.sun.jna.* { public *; }
###### Encrypted Logs - end

###### Glide - begin
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl { *; }
###### Glide - end

###### SavedStateHandleExt - begin
###### We use reflection so we have to keep this method
-keepclassmembers class * extends androidx.navigation.NavArgs {
    fromSavedStateHandle(androidx.lifecycle.SavedStateHandle);
}
###### SavedStateHandleExt - end

# This is generated automatically by the Android Gradle plugin.
-dontwarn java.beans.ConstructorProperties
-dontwarn java.beans.Transient
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.impl.StaticMDCBinder

# Credential Manager
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}

# Azure Storage Blob and related dependencies
-dontwarn io.micrometer.**
-dontwarn io.netty.incubator.**
-dontwarn javax.xml.stream.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.logging.log4j.**
-dontwarn reactor.blockhound.**
-dontwarn aQute.bnd.annotation.spi.**

# Keep Reactor Netty classes
-keep class io.netty.** { *; }
-keep class reactor.netty.** { *; }
-keep class io.micrometer.** { *; }

# Keep Jackson XML classes
-keep class com.fasterxml.jackson.dataformat.xml.** { *; }
-keep class org.codehaus.stax2.** { *; }
-keep class com.ctc.wstx.** { *; }

# Keep XML Stream classes
-keep class javax.xml.stream.** { *; }

# Keep logging classes
-keep class org.apache.log4j.** { *; }
-keep class org.apache.logging.log4j.** { *; }

# Keep Azure Storage Blob classes
-keep class com.azure.storage.blob.** { *; }
-keep class com.azure.core.** { *; }

# Keep BlockHound classes
-keep class reactor.blockhound.** { *; }

# Keep BND annotation classes
-keep class aQute.bnd.annotation.** { *; }

# Additional Netty and compression library exclusions
-dontwarn com.aayushatharva.brotli4j.**
-dontwarn com.github.luben.zstd.**
-dontwarn com.jcraft.jzlib.**
-dontwarn com.ning.compress.**
-dontwarn com.oracle.svm.core.annotate.**
-dontwarn io.netty.handler.codec.haproxy.**
-dontwarn javax.naming.**
-dontwarn lzma.sdk.**
-dontwarn net.jpountz.**
-dontwarn org.eclipse.jetty.alpn.**
-dontwarn org.eclipse.jetty.npn.**
-dontwarn org.jboss.marshalling.**
-dontwarn org.osgi.framework.**
-dontwarn sun.security.x509.**
-dontwarn com.ctc.wstx.shaded.msv_core.**

# Keep all Netty classes but exclude problematic ones
-keep class io.netty.** { *; }
-dontwarn io.netty.handler.codec.compression.Brotli**
-dontwarn io.netty.handler.codec.compression.Zstd**
-dontwarn io.netty.handler.codec.compression.JZlib**
-dontwarn io.netty.handler.codec.compression.Lzf**
-dontwarn io.netty.handler.codec.compression.Lzma**
-dontwarn io.netty.handler.codec.compression.Lz4**
-dontwarn io.netty.handler.codec.haproxy.**
-dontwarn io.netty.handler.ssl.JettyAlpn**
-dontwarn io.netty.handler.ssl.JettyNpn**
-dontwarn io.netty.handler.codec.marshalling.**
-dontwarn io.netty.handler.codec.protobuf.**
-dontwarn io.netty.resolver.dns.DirContextUtils
-dontwarn io.netty.util.NetUtilSubstitutions
-dontwarn io.netty.util.internal.svm.**

# Keep compression libraries if they exist
-keep class com.aayushatharva.brotli4j.** { *; }
-keep class com.github.luben.zstd.** { *; }
-keep class com.jcraft.jzlib.** { *; }
-keep class com.ning.compress.** { *; }
-keep class lzma.sdk.** { *; }
-keep class net.jpountz.** { *; }
-keep class org.eclipse.jetty.alpn.** { *; }
-keep class org.eclipse.jetty.npn.** { *; }
-keep class org.jboss.marshalling.** { *; }

# Keep OSGi and security classes
-keep class org.osgi.framework.** { *; }
-keep class sun.security.x509.** { *; }

# Keep MSV core classes
-keep class com.ctc.wstx.shaded.msv_core.** { *; }

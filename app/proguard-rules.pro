# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\android-sdks/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn com.google.firebase.messaging.**
-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *;}
-dontwarn org.webrtc.**
-dontwarn org.webrtc.app.**
-keep class org.webrtc.** { *; }
-keepnames class com.amazonaws.** { *; }
-dontwarn com.amazonaws.**
-keep public class org.slf4j.** { *; }
-keep public class ch.** { *; }
-dontwarn java.awt.**
-dontwarn java.beans.Beans
-dontwarn javax.security.**
-keep class javamail.** {*;}
-keep class javax.mail.** {*;}
-keep class javax.activation.** {*;}
-keep class com.sun.mail.dsn.** {*;}
-keep class com.sun.mail.handlers.** {*;}
-keep class com.sun.mail.smtp.** {*;}
-keep class com.sun.mail.util.** {*;}
-keep class mailcap.** {*;}
-keep class mimetypes.** {*;}
-keep class myjava.awt.datatransfer.** {*;}
-keep class org.apache.harmony.awt.** {*;}
-keep class org.apache.harmony.misc.** {*;}


-keep class com.spicecomm.app.reciever.**
-keep class com.spicecomm.app.receivers.**
-keep class PromotionalMessageReceiver.**

-keep class com.stripe.** { *; }
-keep class com.nimbusds.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class net.minidev.** { *; }
-keep class org.bouncycastle.** { *; }
-dontwarn com.stripe.**
-dontwarn org.bouncycastle.**
-dontwarn com.nimbusds.**
-dontwarn net.minidev.**
-dontwarn kotlinx.coroutines.**
-dontwarn okhttp3.**
-keep class org.spongycastle.** { *; }
-dontwarn org.spongycastle.**

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*
-dontwarn com.squareup.picasso.**
-keep public class com.squareup.picasso.** { *; }

# This dnsjava class uses old Sun API
-dontnote org.xbill.DNS.spi.DNSJavaNameServiceDescriptor
-dontwarn org.xbill.DNS.spi.DNSJavaNameServiceDescriptor

# See http://stackoverflow.com/questions/5701126, happens in dnsjava
-optimizations !code/allocation/variable
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/qfpay/Downloads/adt-bundle-mac-x86_64-20131030/sdk/tools/proguard/proguard-android.txt
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

-libraryjars /Library/Java/JavaVirtualMachines/jdk1.8.0_45.jdk/Contents/Home/jre/lib/rt.jar
-libraryjars /Users/qfpay/Downloads/adt-bundle-mac-x86_64-20131030/sdk/platforms/android-23/android.jar


# -libraryjars libs/dspread_android_sdk_2.3.9.jar
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

-dontwarn android.support.**

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepattributes *Annotation*

-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}


-keep class * extends java.lang.annotation.Annotation { *; }

-keep class org.json.** {*;}
-dontwarn org.json.**
-keep class com.google.gson.Gson.** {*;}
-dontwarn com.google.gson.Gson.**
##---------------End: proguard configuration common for all Android apps ----------

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

##------------Gson---End: proguard configuration for Gson  --------------------------------------------


#-dontskipnonpubliclibraryclassmembers

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keep class android.net.http.SslError
-dontwarn android.net.http.SslError

#-keepclassmembers class com.qfpay.push.QFPayStatSdk { #保护类中的所有方法名
#    public <methods>;
#}

-dontwarn com.qfpay.push.**

-keep public class com.qfpay.push.R$*{
    public static final int *;
}

-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-keep public class com.qfpay.push.request.** { *; }
-keep public class com.qfpay.push.proxy.BaseHandler.** { *; }
-keep public class com.qfpay.push.proxy.connection.IConnection.** { *; }
-keep public class com.qfpay.push.proxy.connection.PrinterConnection.** { *; }
-keep public class com.qfpay.push.util.Constant.** { *; }
-keep public class com.qfpay.push.util.SPUtil.** { *; }

#不压缩输入的类文件
-dontshrink
#不优化输入的类文件
-dontoptimize


#佳博sdk
-keep class com.gprinter.** { *; }
-dontwarn com.gprinter.**

#-useuniqueclassmembernames
# 类和类成员都使用唯一的名字
# -dontusemixedcaseclassnames
# 不使用大小写混合类名

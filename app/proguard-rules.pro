# Add project specific ProGuard rules here.
-keep class * extends com.google.gson.TypeAdapter

# https://github.com/square/okhttp/pull/6792
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.*
-dontwarn org.openjsse.**



-keep class kr.co.uxn.agms_p_a2rt.api.model.** { *; }


# GSON
### Gson ProGuard and R8 rules which are relevant for all users
### This file is automatically recognized by ProGuard and R8, see https://developer.android.com/build/shrink-code#configuration-files
###
### IMPORTANT:
### - These rules are additive; don't include anything here which is not specific to Gson (such as completely
###   disabling obfuscation for all classes); the user would be unable to disable that then
### - These rules are not complete; users will most likely have to add additional rules for their specific
###   classes, for example to disable obfuscation for certain fields or to keep no-args constructors
###

# Keep generic signatures; needed for correct type resolution
-keepattributes Signature

# Keep Gson annotations
# Note: Cannot perform finer selection here to only cover Gson annotations, see also https://stackoverflow.com/q/47515093
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

### The following rules are needed for R8 in "full mode" which only adheres to `-keepattribtues` if
### the corresponding class or field is matches by a `-keep` rule as well, see
### https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md#r8-full-mode

# Keep class TypeToken (respectively its generic signature) if present
-if class com.google.gson.reflect.TypeToken
-keep,allowobfuscation class com.google.gson.reflect.TypeToken

# Keep any (anonymous) classes extending TypeToken
-keep,allowobfuscation class * extends com.google.gson.reflect.TypeToken

# Keep classes with @JsonAdapter annotation
-keep,allowobfuscation,allowoptimization @com.google.gson.annotations.JsonAdapter class *

# Keep fields with any other Gson annotation
# Also allow obfuscation, assuming that users will additionally use @SerializedName or
# other means to preserve the field names
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.Expose <fields>;
  @com.google.gson.annotations.JsonAdapter <fields>;
  @com.google.gson.annotations.Since <fields>;
  @com.google.gson.annotations.Until <fields>;
}

# Keep no-args constructor of classes which can be used with @JsonAdapter
# By default their no-args constructor is invoked to create an adapter instance
-keepclassmembers class * extends com.google.gson.TypeAdapter {
  <init>();
}
-keepclassmembers class * implements com.google.gson.TypeAdapterFactory {
  <init>();
}
-keepclassmembers class * implements com.google.gson.JsonSerializer {
  <init>();
}
-keepclassmembers class * implements com.google.gson.JsonDeserializer {
  <init>();
}

# Keep fields annotated with @SerializedName for classes which are referenced.
# If classes with fields annotated with @SerializedName have a no-args
# constructor keep that as well. Based on
# https://issuetracker.google.com/issues/150189783#comment11.
# See also https://github.com/google/gson/pull/2420#discussion_r1241813541
# for a more detailed explanation.
-if class *
-keepclasseswithmembers,allowobfuscation class <1> {
  @com.google.gson.annotations.SerializedName <fields>;
}
-if class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers,allowobfuscation,allowoptimization class <1> {
  <init>();
}

# kakao
-keep class com.kakao.sdk.**.model.* { <fields>; }

# https://github.com/square/okhttp/pull/6792
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.*
-dontwarn org.openjsse.**

# refrofit2 (with r8 full mode)
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# --- 1. Retrofit 규칙 ---

# Retrofit이 사용하는 어노테이션(@GET, @POST 등) 정보 유지
-keepattributes *Annotation*

# (필수) 여러분의 API 인터페이스 패키지 (예: com.myapp.network)
# 이 패키지 밑의 모든 인터페이스와 그 메서드들을 유지합니다.
-keep interface kr.co.uxn.agms_p_a2rt.api.** { *; }

# --- 2. Gson 규칙 (Converter-Gson) ---

# (필수) 여러분의 데이터 모델/DTO 패키지 (예: com.myapp.data.dto)
# 이 패키지 밑의 모든 클래스와 그 멤버(필드, 메서드)를 유지합니다.
-keep class kr.co.uxn.agms_p_a2rt.api.model.requestDTO.** { *; }
-keep class kr.co.uxn.agms_p_a2rt.api.model.responseDTO.** { *; }

# Gson의 TypeAdapter를 상속받는 클래스들 유지
-keep class * extends com.google.gson.TypeAdapter

# --- 3. OkHttp 규칙 (Retrofit의 의존성) ---

# Retrofit은 OkHttp를 사용하며, OkHttp는 다양한 암호화 라이브러리를
# 선택적으로 사용합니다. 이로 인한 R8 경고를 무시합니다. (안전함)
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# 릴리즈모드에서, Something went wrong 에러
# Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Google Identity Services
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn com.google.android.libraries.identity.googleid.**

# Credentials API
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**

# Play Core (App Update)
-keep class com.google.android.play.** { *; }
-dontwarn com.google.android.play.**

# Tasks API
-keep class com.google.android.gms.tasks.** { *; }
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
# ─────────────────────────────────────────────────────────────────────
# 배포 빌드에서 android.util.Log 호출을 제거한다.
#
# 침투시험에서 비루팅 기기의 logcat 으로 토큰·이메일·시리얼·MAC·혈당·
# 임피던스·센서 원시 측정값이 수집됐다. 호출 지점이 수십 곳이라 하나씩
# 주석 처리하면 빠뜨리기 쉽고, 새로 추가되는 로그를 막지도 못한다.
# 그래서 배포 빌드에서는 R8 이 Log 호출을 통째로 지우게 한다.
#
# -assumenosideeffects 는 "이 호출은 부작용이 없으니 지워도 된다"는 선언이다.
# R8 이 호출문과 인자 생성 코드까지 함께 제거하므로, 찍을 문자열을 만드는
# 비용도 사라진다. 최적화가 켜져 있어야 동작하며(proguard-android-optimize.txt),
# release 에만 minify 가 걸려 있어 디버그 빌드의 로그는 그대로 남는다.
#
# 주의: 진단이 필요한 비민감 로그까지 함께 사라진다. 배포본에서 남겨야 하는
# 로그가 생기면 Log 를 직접 부르지 말고 별도 래퍼를 만들어 그것만 예외로 둔다.
# OkHttp 의 요청·응답 본문 로그는 Log 를 거치지 않으므로 RetrofitClient 의
# loggingInterceptor() 에서 배포 빌드에 Level.NONE 을 주어 따로 막는다.
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
    public static *** println(...);
}

# 릴리즈에서는 앱 로그를 모두 지운다 (release 만 minify 하므로 debug 로그는 그대로).
# 로그 문자열에 측정값·기기 정보가 섞여 있어 logcat 으로 새지 않게 한다 (CWE-532).
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
    public static int println(...);
}

# Kotlin 에서 Log.d(...) 가 if/when/람다의 마지막 식이면 반환값이 쓰여서 위 규칙만으로는
# 지워지지 않는다. 반환값을 0 으로 못박아 쓰임을 없애야 호출까지 지워진다.
-assumevalues class android.util.Log {
    public static int v(...) return 0;
    public static int d(...) return 0;
    public static int i(...) return 0;
    public static int w(...) return 0;
    public static int e(...) return 0;
    public static int wtf(...) return 0;
    public static int println(...) return 0;
}

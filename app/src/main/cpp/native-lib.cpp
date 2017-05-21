#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_leaf_yeyy_weightcardio_base_WeightcardioApplication_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

/**
 * http api 授权 key ，由服务端提供
 */
const char *AUTH_KEY = "0CC175B9C0F1B6A831C399E269772661";

/**
 * 发布的app 签名,只有和本签名一致的app 才会返回合法的 授权 key
 */
const char *RELEASE_SIGN = "308201dd30820146020101300d06092a864886f70d010105050030373116301406035504030c0d416e64726f69642044656275673110300e060355040a0c07416e64726f6964310b3009060355040613025553301e170d3137303530333038343630355a170d3437303432363038343630355a30373116301406035504030c0d416e64726f69642044656275673110300e060355040a0c07416e64726f6964310b300906035504061302555330819f300d06092a864886f70d010101050003818d0030818902818100813dd1c46f9b91dc37516e04931b4b2025db1f64f82f76cc36216733e305c6abca622102f90ce10f5f18ef9df3ff8b7b759ec66fa3752af2f3d665a535c19331a41109436d24a7ff2ff06148e5c7a27a25620cf9b24197542cca055087f05fcfeaf1e7780dbaa8cee10858c5951ad2e96da4b7ba6930842fa248eb02763a30fd0203010001300d06092a864886f70d0101050500038181002d62fd0e32c9f8f9ca4576d62cdad4065dde52a5692add60597dfb8e2e59c1f40611b44c9f4612759eec274455ef103b1b3d6865b1f5a00df2de02ad7449d1243ff5f7e6a41f8cb684fd162f1b9ca0c968c412d674895d3be58775f9c2d6c27f23248cf5599e610cf195f831de7ba47a7deba7917eee7f0d49c31911877eaa31";

/**
 * 拿到传入的app  的 签名信息，对比合法的app 签名，防止so文件被未知应用盗用
 */
static jclass contextClass;
static jclass signatureClass;
static jclass packageNameClass;
static jclass packageInfoClass;

extern "C"
JNIEXPORT jstring JNICALL Java_com_leaf_yeyy_weightcardio_base_WeightcardioApplication_getAuthKey(
        JNIEnv *env, jobject obj, jobject contextObject) {

    jmethodID getPackageManagerId = (env)->GetMethodID(contextClass, "getPackageManager",
                                                       "()Landroid/content/pm/PackageManager;");
    jmethodID getPackageNameId = (env)->GetMethodID(contextClass, "getPackageName",
                                                    "()Ljava/lang/String;");
    jmethodID signToStringId = (env)->GetMethodID(signatureClass, "toCharsString",
                                                  "()Ljava/lang/String;");
    jmethodID getPackageInfoId = (env)->GetMethodID(packageNameClass, "getPackageInfo",
                                                    "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    jobject packageManagerObject = (env)->CallObjectMethod(contextObject, getPackageManagerId);
    jstring packNameString = (jstring) (env)->CallObjectMethod(contextObject, getPackageNameId);
    jobject packageInfoObject = (env)->CallObjectMethod(packageManagerObject, getPackageInfoId,
                                                        packNameString, 64);
    jfieldID signaturefieldID = (env)->GetFieldID(packageInfoClass, "signatures",
                                                  "[Landroid/content/pm/Signature;");
    jobjectArray signatureArray = (jobjectArray) (env)->GetObjectField(packageInfoObject,
                                                                       signaturefieldID);
    jobject signatureObject = (env)->GetObjectArrayElement(signatureArray, 0);

    const char *signStrng = (env)->GetStringUTFChars(
            (jstring) (env)->CallObjectMethod(signatureObject, signToStringId), 0);
    if (strcmp(signStrng, RELEASE_SIGN) == 0)//签名一致  返回合法的 api key，否则返回错误
    {
        return (env)->NewStringUTF(AUTH_KEY);
    } else {
        return (env)->NewStringUTF("error");
    }
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    JNIEnv *env = NULL;
    jint result = -1;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK)
        return result;

    contextClass = (jclass) env->NewGlobalRef((env)->FindClass("android/content/Context"));
    signatureClass = (jclass) env->NewGlobalRef((env)->FindClass("android/content/pm/Signature"));
    packageNameClass = (jclass) env->NewGlobalRef(
            (env)->FindClass("android/content/pm/PackageManager"));
    packageInfoClass = (jclass) env->NewGlobalRef(
            (env)->FindClass("android/content/pm/PackageInfo"));

    return JNI_VERSION_1_4;
}

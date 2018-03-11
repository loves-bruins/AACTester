#include <jni.h>
#include <string>
#include <android/Log.h>

//#include "JNIHelp.h"

#include "inc/voAAC.h"
#include "inc/cmnMemory.h"
#include "inc/voType.h"

#define LOG_TAG "AACInputStream"


// ----------------------------------------------------------------------------
#define  VO_AAC_E_OUTPUT	  1
#define READ_SIZE	(1024*8)


static AACENC_PARAM             aacParam;
static VO_MEM_OPERATOR          memoryOperator;
static VO_CODEC_INIT_USERDATA   userData;
static VO_AUDIO_CODECAPI        audioAPI;
static VO_HANDLE                hCodec;

#define UNUSED(P) (void)(P);


//using namespace android;

extern "C"
JNIEXPORT jint JNICALL
Java_furtiveops_com_aactester_MainActivity_AACEncoderInitialize
        (JNIEnv *env, jclass /* clazz */) {
    UNUSED(env);
    int result;

    result = voGetAACEncAPI(&audioAPI);
    if(0 != result)
    {
        return (jint)result;
    }

    memset(&aacParam, 0, sizeof(AACENC_PARAM));
    aacParam.adtsUsed = 1;
    aacParam.bitRate = 64000;
    aacParam.nChannels = 1;
    aacParam.sampleRate = 16000;

    memoryOperator.Alloc = cmnMemAlloc;
    memoryOperator.Copy = cmnMemCopy;
    memoryOperator.Free = cmnMemFree;
    memoryOperator.Set = cmnMemSet;
    memoryOperator.Check = cmnMemCheck;
    userData.memflag = VO_IMF_USERMEMOPERATOR;
    userData.memData = (VO_PTR)(&memoryOperator);

    result = audioAPI.Init(&hCodec, VO_AUDIO_CodingAAC, &userData);
    if(0 != result)
    {
        return (jint)result;
    }

    result = audioAPI.SetParam(hCodec, VO_PID_AAC_ENCPARAM, &aacParam);
    if(0 != result)
    {
        return (jint)result;
    }

    return (jint)0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_furtiveops_com_aactester_MainActivity_AACEncoderEncode
        (JNIEnv *env, jclass /* clazz */,
         jbyteArray pcm, jint pcmByteLength, jbyteArray aac) {

    VO_CODECBUFFER           inData;
    VO_CODECBUFFER           outData;
    VO_AUDIO_OUTPUTINFO      outInfo;
    int                      resultCode = -1;

    jbyte inBuf[2*pcmByteLength];
    jbyte outBuf[READ_SIZE];


    env->GetByteArrayRegion(pcm, 0, pcmByteLength, inBuf);
    inData.Buffer = (unsigned char*)inBuf;
    inData.Length = pcmByteLength;  // Use the actual length

    resultCode = audioAPI.SetInputData(hCodec,&inData);
    if(0 != resultCode)
    {
        return (jint)resultCode;
    }

    memset(&outData, 0, sizeof(outData));
    memset(&outInfo, 0, sizeof(outInfo));

    outData.Buffer   = (unsigned char *)outBuf;
    outData.Length   = READ_SIZE;
    resultCode = audioAPI.GetOutputData(hCodec, &outData, &outInfo);
    if(0 != resultCode)
    {
        return (jint)resultCode;
    }
    else
    {
        env->SetByteArrayRegion(aac, 0, outData.Length, outBuf);
    }


    return (jint)outData.Length;
}

extern "C"
JNIEXPORT void JNICALL
Java_furtiveops_com_aactester_MainActivity_AACEncoderCleanup
        (JNIEnv* /* env */, jclass /* clazz */) {
    audioAPI.Uninit(hCodec);
}

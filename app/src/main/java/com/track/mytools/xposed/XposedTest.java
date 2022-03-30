package com.track.mytools.xposed;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedTest implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        //XposedBridge.log("===包名===" + loadPackageParam.packageName);

        if (loadPackageParam.packageName.equals("io.dushu.fandengreader")) {
            XposedBridge.log("开始 hook ");
            //混淆后方法名也变化了
            XposedHelpers.findAndHookMethod("io.dushu.fandengreader.fragment.AudioFragment", loadPackageParam.classLoader, "onClickDownload", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("===123===" + loadPackageParam.packageName);
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("===321===" + loadPackageParam.packageName);
                    super.afterHookedMethod(param);
                    param.setResult(true);
                }

            });
        }
    }
}
